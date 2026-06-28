#!/usr/bin/env python3
"""
PICKD 로컬 E2E 테스트 스크립트
=================================
사전 조건:
  - BE 서버: http://localhost:8080  (./gradlew bootRun --args='--spring.profiles.active=local')
  - AI 서버: http://localhost:8000  (uvicorn app.main:app --reload)

실행:
  python scripts/e2e_test.py

의존 라이브러리 (표준 라이브러리만 사용, 설치 불필요):
  - urllib.request / urllib.error / json (Python 표준)
"""

import json
import sys
import time
import urllib.error
import urllib.request
from urllib.parse import urlencode

# ──────────────────────────────────────────────────────────────────────────────
# 설정
# ──────────────────────────────────────────────────────────────────────────────

BE = "http://localhost:8080"
AI = "http://localhost:8000"

TEST_EMAIL   = "test@pickd.local"
TEST_NAME    = "E2E테스트유저"
RESUME_URL   = "https://example.com/dummy-resume.pdf"

# 기준 경험 seed (Step2 중복 판정 기준이 될 기존 경험들)
SEED_EXPERIENCES = [
    {
        "title": "금융 AI Agent 프로젝트",
        "experienceType": "PROJECT",
        "experienceGroup": "NARRATIVE",
        "status": "COMPLETED",
        "documentContent": "추천 모델 개선을 담당했습니다. 추천 정확도 15% 향상.",
        "attributes": {"project_name": "금융 AI Agent", "role": "백엔드 개발", "period": "2023.03 ~ 2023.08"},
        "keywords": ["문제 해결", "실행력"],
        "forceCreate": True,
    },
    {
        "title": "TOEIC 930점",
        "experienceType": "LANGUAGE",
        "experienceGroup": "SPEC",
        "status": "COMPLETED",
        "documentContent": None,
        "attributes": {"score": "930", "test_name": "TOEIC"},
        "keywords": ["어학"],
        "forceCreate": True,
    },
    {
        "title": "카카오 서버 인턴십",
        "experienceType": "INTERN",
        "experienceGroup": "NARRATIVE",
        "status": "COMPLETED",
        "documentContent": "Spring Boot 기반 API 개발 및 유지보수를 담당했습니다.",
        "attributes": {"company": "카카오", "role": "서버 개발", "period": "2023.07 ~ 2023.08"},
        "keywords": ["Spring", "Java", "협업"],
        "forceCreate": True,
    },
]

# Step1 → Step2 대상 temp experiences
TEMP_EXPERIENCES = [
    {"name": "금융 AI Agent 프로젝트",  "type": "PROJECT",  "group": "NARRATIVE"},
    {"name": "TOEIC 930점",            "type": "LANGUAGE", "group": "SPEC"},
]

# 중복 판정 재현용 temp (기존 경험과 동일한 제목 재주입)
DUPLICATE_TEMP_EXPERIENCES = [
    {"name": "금융 AI Agent 프로젝트",  "type": "PROJECT",  "group": "NARRATIVE"},
]

# ──────────────────────────────────────────────────────────────────────────────
# 유틸
# ──────────────────────────────────────────────────────────────────────────────

PASSED = []
FAILED = []

def _color(text, code): return f"\033[{code}m{text}\033[0m"
def ok(msg):   print(_color(f"  ✓ {msg}", "32"))
def fail(msg): print(_color(f"  ✗ {msg}", "31"))
def info(msg): print(_color(f"  · {msg}", "36"))
def section(title): print(_color(f"\n[{title}]", "1;33"))


def request(method, url, data=None, headers=None, cookie=None):
    """간단한 HTTP 요청 헬퍼. JSON body + 쿠키 지원."""
    req_headers = {"Content-Type": "application/json", "Accept": "application/json"}
    if headers:
        req_headers.update(headers)
    if cookie:
        req_headers["Cookie"] = f"accessToken={cookie}"

    body = json.dumps(data).encode() if data is not None else None
    req = urllib.request.Request(url, data=body, headers=req_headers, method=method)
    try:
        with urllib.request.urlopen(req) as resp:
            raw = resp.read().decode()
            return resp.status, json.loads(raw) if raw else None
    except urllib.error.HTTPError as e:
        raw = e.read().decode()
        try:
            return e.code, json.loads(raw)
        except Exception:
            return e.code, raw


def check(label, condition, detail=""):
    if condition:
        ok(label)
        PASSED.append(label)
    else:
        fail(f"{label}  {detail}")
        FAILED.append(label)


def abort(msg):
    fail(f"ABORT: {msg}")
    summary()
    sys.exit(1)


def summary():
    print(_color(f"\n{'='*50}", "1"))
    print(_color(f"  PASSED: {len(PASSED)}", "32"))
    if FAILED:
        print(_color(f"  FAILED: {len(FAILED)}", "31"))
        for f in FAILED:
            print(_color(f"    - {f}", "31"))
    print(_color(f"{'='*50}", "1"))


# ──────────────────────────────────────────────────────────────────────────────
# 테스트 단계
# ──────────────────────────────────────────────────────────────────────────────

def step1_health():
    section("STEP 1 — 서버 헬스 체크")

    status, body = request("GET", f"{AI}/health")
    check("AI 서버 응답", status == 200, f"status={status}")
    if status != 200:
        abort("AI 서버가 응답하지 않습니다. 먼저 AI 서버를 실행하세요.")

    status, body = request("GET", f"{BE}/swagger-ui/index.html")
    check("BE 서버 응답", status == 200, f"status={status}")
    if status != 200:
        abort("BE 서버가 응답하지 않습니다. 먼저 BE 서버를 실행하세요.")


def step2_seed_user():
    section("STEP 2 — 테스트 유저 생성 + JWT 획득")

    status, body = request("POST", f"{BE}/internal/seed/user", {
        "email": TEST_EMAIL,
        "name":  TEST_NAME,
    })
    check("POST /internal/seed/user → 200", status == 200, f"status={status} body={body}")
    if status != 200:
        abort("seed/user 실패. BE가 local 프로파일로 실행 중인지 확인하세요.")

    token = body["accessToken"]
    info(f"유저 ID={body['id']}  email={body['email']}")
    info(f"JWT={token[:40]}...")
    return token


def step3_seed_experiences(token):
    section("STEP 3 — 수기 경험 seed (3개)")

    ids = []
    for exp in SEED_EXPERIENCES:
        status, body = request("POST", f"{BE}/api/experiences", exp, cookie=token)
        check(f"POST /api/experiences [{exp['title'][:20]}]", status == 200, f"status={status}")
        if status == 200 and isinstance(body, dict):
            ids.append(body.get("id"))

    status, body = request("GET", f"{BE}/api/experiences", cookie=token)
    check("GET /api/experiences → 3개", status == 200 and isinstance(body, list) and len(body) >= 3,
          f"count={len(body) if isinstance(body, list) else '?'}")
    info(f"저장된 경험 수: {len(body) if isinstance(body, list) else '?'}")
    return ids


def step4_seed_temps(token):
    section("STEP 4 — ExperienceTemp 주입 (Step1 대체)")

    status, body = request("POST", f"{BE}/internal/seed/temp", {
        "email":       TEST_EMAIL,
        "resumeUrl":   RESUME_URL,
        "experiences": TEMP_EXPERIENCES,
    })
    check("POST /internal/seed/temp → 200", status == 200, f"status={status} body={body}")
    if status != 200:
        abort("temp seed 실패")

    temp_ids = body["tempIds"]
    info(f"주입된 temp IDs: {temp_ids}")
    return temp_ids


def step5_extract_step2(token, temp_ids):
    section("STEP 5 — BE Step2 V2 호출 (AI 연동)")

    info("AI 경험 추출 중... (10~30초 소요)")
    status, body = request("POST", f"{BE}/api/v2/experiences/extract/step2",
                           {"selectedTempIds": temp_ids}, cookie=token)
    check("POST /api/v2/experiences/extract/step2 → 200", status == 200, f"status={status}")
    if status != 200:
        info(f"응답: {json.dumps(body, ensure_ascii=False)[:300]}")
        abort("Step2 실패")

    saved = body.get("savedExperiences", [])
    batch_id = body.get("duplicateBatchId")
    dup_groups = body.get("duplicateGroups", [])

    check("savedExperiences 존재", len(saved) > 0, f"count={len(saved)}")
    info(f"저장된 경험: {[e.get('title') for e in saved]}")
    info(f"중복 batchId: {batch_id}")
    info(f"중복 그룹 수: {len(dup_groups)}")
    return body


def step6_verify_state(token, step2_result):
    section("STEP 6 — DB 상태 검증")

    status, body = request("GET", f"{BE}/internal/experience-extraction-test/state", cookie=token)
    check("GET /internal/experience-extraction-test/state → 200", status == 200, f"status={status}")
    if status != 200:
        return

    exp_count  = len(body.get("experiences", []))
    temp_count = len(body.get("temps", []))
    batch_count = len(body.get("batches", []))
    info(f"experiences={exp_count}  temps={temp_count}  batches={batch_count}")

    check("temp가 처리되어 0개", temp_count == 0, f"남은 temp={temp_count}")
    check("experience 3개 이상", exp_count >= 3, f"count={exp_count}")

    batch_id = step2_result.get("duplicateBatchId")
    if batch_id:
        batches = body.get("batches", [])
        pending = [b for b in batches if b.get("status") == "PENDING"]
        check("PENDING batch 존재", len(pending) > 0, f"pending={len(pending)}")


def step7_duplicate_batch(token):
    section("STEP 7 — 중복 batch 생성 검증 (동일 temp 재주입)")

    # 동일 경험 재주입
    status, body = request("POST", f"{BE}/internal/seed/temp", {
        "email":       TEST_EMAIL,
        "resumeUrl":   RESUME_URL,
        "experiences": DUPLICATE_TEMP_EXPERIENCES,
    })
    check("중복 temp 재주입 → 200", status == 200, f"status={status}")
    if status != 200:
        return None

    temp_ids = body["tempIds"]
    info(f"재주입 temp IDs: {temp_ids}")

    info("Step2 재호출 (중복 판정 예상)...")
    status, body = request("POST", f"{BE}/api/v2/experiences/extract/step2",
                           {"selectedTempIds": temp_ids}, cookie=token)
    check("Step2 재호출 → 200", status == 200, f"status={status}")
    if status != 200:
        return None

    batch_id = body.get("duplicateBatchId")
    dup_groups = body.get("duplicateGroups", [])
    check("중복 batchId 생성됨", batch_id is not None, "batchId=None → 중복 미감지")
    info(f"중복 batchId: {batch_id}  groups: {len(dup_groups)}")
    return batch_id, body.get("duplicateGroups", [])


def step8_pending_duplicates(token):
    section("STEP 8 — pending duplicates 조회")

    status, body = request("GET", f"{BE}/api/v2/experiences/extract/duplicates/pending", cookie=token)
    check("GET /duplicates/pending → 200", status == 200, f"status={status}")
    if status != 200:
        return None, None

    batches = body.get("batches", [])
    check("pending batch 목록 존재", len(batches) > 0, f"count={len(batches)}")
    if batches:
        first = batches[0]
        info(f"첫 번째 batch ID: {first.get('duplicateBatchId')}")
        info(f"groups 수: {len(first.get('duplicateGroups', []))}")
        return first.get("duplicateBatchId"), first.get("duplicateGroups", [])
    return None, None


def step9_step3(token, batch_id, dup_groups):
    section("STEP 9 — Step3 중복 선택 처리")

    if not batch_id or not dup_groups:
        info("batch_id 또는 dup_groups 없음 — Step3 스킵")
        return

    # 각 그룹에서 첫 번째 item 선택 (EXISTING 또는 EXTRACTED)
    selections = []
    for group in dup_groups:
        group_id = group.get("groupId")
        items    = group.get("items", [])
        if items:
            selected_id = items[0].get("itemId")
            selections.append({"groupId": group_id, "selectedItemIds": [selected_id]})

    status, body = request("POST", f"{BE}/api/v2/experiences/extract/step3", {
        "duplicateBatchId": batch_id,
        "groups": selections,
    }, cookie=token)
    check("POST /api/v2/experiences/extract/step3 → 200", status == 200, f"status={status}")
    if status != 200:
        info(f"응답: {json.dumps(body, ensure_ascii=False)[:300]}")
        return

    selected = body.get("selectedExperiences", [])
    deleted  = body.get("deletedExperienceIds", [])
    info(f"최종 선택 경험: {[e.get('title') for e in selected]}")
    info(f"삭제된 경험 IDs: {deleted}")


def step10_final_state(token):
    section("STEP 10 — 최종 DB 상태 검증")

    status, body = request("GET", f"{BE}/internal/experience-extraction-test/state", cookie=token)
    check("최종 state 조회 → 200", status == 200, f"status={status}")
    if status != 200:
        return

    exp_count  = len(body.get("experiences", []))
    temp_count = len(body.get("temps", []))
    batches    = body.get("batches", [])
    completed  = [b for b in batches if b.get("status") == "COMPLETED"]

    check("temp 0개 (모두 처리됨)", temp_count == 0, f"남은 temp={temp_count}")
    check("완료된 batch 존재", len(completed) > 0, f"completed={len(completed)}")

    print()
    info(f"최종 상태 — experiences: {exp_count}  temps: {temp_count}  batches: {len(batches)} (완료: {len(completed)})")


# ──────────────────────────────────────────────────────────────────────────────
# 메인
# ──────────────────────────────────────────────────────────────────────────────

def main():
    print(_color("=" * 50, "1;36"))
    print(_color("  PICKD 로컬 E2E 테스트", "1;36"))
    print(_color("=" * 50, "1;36"))
    print(f"  BE: {BE}")
    print(f"  AI: {AI}")
    print(f"  테스트 유저: {TEST_EMAIL}")

    step1_health()
    token = step2_seed_user()
    step3_seed_experiences(token)
    temp_ids = step4_seed_temps(token)
    step2_result = step5_extract_step2(token, temp_ids)
    step6_verify_state(token, step2_result)
    dup_result = step7_duplicate_batch(token)
    if dup_result:
        _batch_id, _groups = dup_result
    else:
        _batch_id, _groups = None, []
    batch_id, dup_groups = step8_pending_duplicates(token)
    step9_step3(token, batch_id or _batch_id, dup_groups or _groups)
    step10_final_state(token)

    summary()
    sys.exit(1 if FAILED else 0)


if __name__ == "__main__":
    main()
