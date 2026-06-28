# PICKD 프론트엔드 연동 가이드

> 작성일: 2026-06-26  
> 대상: 프론트엔드 팀  
> 현재 BE 브랜치: `main` / AI 브랜치: `main`

---

## 목차

1. [로컬 서버 기동](#1-로컬-서버-기동)
2. [인증 방식](#2-인증-방식)
3. [로컬 테스트 계정 발급](#3-로컬-테스트-계정-발급)
4. [API 호출 순서 — 경험 추출 플로우](#4-api-호출-순서--경험-추출-플로우)
5. [요청/응답 샘플 JSON](#5-요청응답-샘플-json)
6. [에러 케이스 정리](#6-에러-케이스-정리)
7. [상태 전이 다이어그램](#7-상태-전이-다이어그램)
8. [검증 완료 / 미완료 범위](#8-검증-완료--미완료-범위)
9. [로컬 DB seed 방법](#9-로컬-db-seed-방법)

---

## 1. 로컬 서버 기동

### BE (Spring Boot)

```bash
cd pickd-BE
./gradlew bootRun --args='--spring.profiles.active=local'
```

- 주소: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- H2 콘솔: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:pickd` / User: `sa` / Password: (비워둠)
- 테스트 화면: `http://localhost:8080/experience-extraction-test`

### AI (FastAPI)

```bash
cd pickd-AI/myeongsung
source .venv/bin/activate
uvicorn app.main:app --reload --port 8000
```

- 주소: `http://localhost:8000`
- 헬스체크: `GET http://localhost:8000/health` → `{"status":"ok"}`
- Docs: `http://localhost:8000/docs`

### CORS 허용 origin

BE는 아래 두 origin에서 자격증명(쿠키)을 포함한 요청을 허용합니다:

```
http://localhost:3000
http://localhost:5173
```

---

## 2. 인증 방식

### 운영 / 개발: Google OAuth2

1. 프론트에서 `http://localhost:8080/oauth2/authorization/google` 로 리다이렉트
2. 구글 로그인 성공 시 BE가 `accessToken` 쿠키를 발급 후 프론트로 리다이렉트
3. 이후 모든 API 요청에 **쿠키가 자동으로 포함**됨

### 로컬 테스트: 시드 API로 JWT 직접 발급

```
POST http://localhost:8080/internal/seed/user
```

응답의 `accessToken`을 쿠키로 설정:

```javascript
// Axios 설정 예시
axios.defaults.withCredentials = true;

// 또는 수동 쿠키 설정 (개발 도구 → Application → Cookies)
// Name: accessToken
// Value: <응답의 accessToken 값>
// Domain: localhost
```

> `withCredentials: true` 또는 `credentials: 'include'` 를 **반드시** 설정해야 쿠키가 전송됩니다.

---

## 3. 로컬 테스트 계정 발급

```bash
curl -s -X POST http://localhost:8080/internal/seed/user \
  -H "Content-Type: application/json" \
  -d '{"email":"test@pickd.local","name":"테스트유저"}' | python3 -m json.tool
```

응답:

```json
{
  "id": 1,
  "email": "test@pickd.local",
  "name": "테스트유저",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

발급받은 `accessToken`을 브라우저 쿠키로 설정하면 로그인 상태가 됩니다.

---

## 4. API 호출 순서 — 경험 추출 플로우

### 전체 플로우 개요

```
[사용자] 자소서 PDF 업로드
        ↓
Step1: POST /api/experiences/extract/step1
        → ExperienceTemp 목록 반환 (후보 리스트)
        ↓
[사용자] 원하는 경험 선택 (checkbox)
        ↓
Step2 V2: POST /api/v2/experiences/extract/step2
        → savedExperiences: 즉시 저장된 경험
        → duplicateBatchId: 중복 있을 경우 non-null
        → duplicateGroups:  중복 후보 그룹 목록
        ↓
    ┌───────────────────────────────────┐
    │ duplicateBatchId == null?         │
    │  YES → 플로우 종료 (모두 저장됨)  │
    │  NO  → Step3로 진행               │
    └───────────────────────────────────┘
        ↓ (중복 있을 경우)
[사용자] 각 그룹에서 남길 경험 선택
        ↓
Step3 V2: POST /api/v2/experiences/extract/step3
        → 선택된 경험 최종 저장, 나머지 삭제
        ↓
완료
```

### 앱 재시작 / 화면 새로고침 시 미처리 중복 복원

```
GET /api/v2/experiences/extract/duplicates/pending
→ 처리되지 않은 batch 목록 반환
→ 각 batch를 Step3에 전달하여 선택 화면 복원
```

### 엔드포인트 요약

| 순서 | Method | Path | 설명 |
|------|--------|------|------|
| 1 | POST | `/api/experiences/extract/step1` | PDF 업로드 → temp 경험 후보 추출 |
| 2 | POST | `/api/v2/experiences/extract/step2` | 선택 temp ID → 상세 추출 + 중복 판정 |
| 2.5 | GET | `/api/v2/experiences/extract/duplicates/pending` | 미처리 중복 batch 복원 |
| 3 | POST | `/api/v2/experiences/extract/step3` | 중복 그룹 최종 선택 반영 |

---

## 5. 요청/응답 샘플 JSON

### Step1 — 경험 후보 1차 추출

**Request** (`multipart/form-data`)

```
POST /api/experiences/extract/step1
Content-Type: multipart/form-data

file: <PDF 파일 바이너리>
```

**Response** `200`

```json
[
  {
    "id": 101,
    "userId": 1,
    "experienceName": "금융 AI Agent 프로젝트",
    "experienceGroup": "NARRATIVE",
    "experienceType": "PROJECT",
    "createdAt": "2024-09-01T09:00:00"
  },
  {
    "id": 102,
    "userId": 1,
    "experienceName": "TOEIC 930점",
    "experienceGroup": "SPEC",
    "experienceType": "LANGUAGE",
    "createdAt": "2024-09-01T09:00:00"
  }
]
```

> 반환된 `id`를 Step2의 `selectedTempIds`에 사용하세요.

---

### Step2 V2 — 선택 경험 상세 추출

**Request**

```json
POST /api/v2/experiences/extract/step2

{
  "selectedTempIds": [101, 102]
}
```

**Response** `200` — 중복 없는 경우

```json
{
  "savedExperiences": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "title": "금융 AI Agent 프로젝트",
      "experienceType": "PROJECT",
      "experienceGroup": "NARRATIVE",
      "status": "COMPLETED",
      "documentContent": "추천 모델 개선을 담당했습니다.",
      "attributes": { "project_name": "금융 AI Agent", "role": "백엔드 개발" },
      "keywords": ["문제 해결", "실행력"],
      "files": [],
      "links": []
    }
  ],
  "duplicateBatchId": null,
  "duplicateGroups": []
}
```

**Response** `200` — 중복 있는 경우

```json
{
  "savedExperiences": [],
  "duplicateBatchId": "f128a54c-0525-4d89-96bb-ce8f7eb602b3",
  "duplicateGroups": [
    {
      "groupId": "9f83540e-5d78-4a08-a284-78cecfdf2e3d",
      "items": [
        {
          "itemId": "aaaaaaaa-0001-0001-0001-000000000001",
          "source": "EXISTING",
          "similarity": null,
          "experience": {
            "title": "금융 AI Agent 프로젝트",
            "experienceType": "PROJECT",
            "experienceGroup": "NARRATIVE",
            "status": "COMPLETED",
            "documentContent": "추천 모델 개선을 담당했습니다.",
            "attributes": { "project_name": "금융 AI Agent" },
            "keywords": ["문제 해결"]
          }
        },
        {
          "itemId": "dddddddd-draft-0001-0001-000000000001",
          "source": "EXTRACTED",
          "similarity": 0.91,
          "experience": {
            "title": "금융 AI Agent 프로젝트 (재추출)",
            "experienceType": "PROJECT",
            "experienceGroup": "NARRATIVE",
            "status": "COMPLETED",
            "documentContent": "추천 시스템 개선을 통해 정확도를 15% 향상시킨 경험입니다.",
            "attributes": { "project_name": "금융 AI Agent" },
            "keywords": ["문제 해결", "실행력"]
          }
        }
      ]
    }
  ]
}
```

> **`duplicateBatchId`가 non-null이면 Step3 화면을 보여주세요.**  
> `source: "EXISTING"` = 기존에 저장된 경험 / `source: "EXTRACTED"` = 이번에 추출한 후보

---

### pending duplicates — 미처리 중복 batch 복원

**Request**

```
GET /api/v2/experiences/extract/duplicates/pending
```

**Response** `200`

```json
{
  "batches": [
    {
      "duplicateBatchId": "f128a54c-0525-4d89-96bb-ce8f7eb602b3",
      "createdAt": "2024-09-01T09:00:00+09:00",
      "duplicateGroups": [ /* Step2와 동일한 구조 */ ]
    }
  ]
}
```

> `batches`가 비어 있으면 처리할 중복 없음.

---

### Step3 V2 — 중복 최종 선택 반영

**Request**

```json
POST /api/v2/experiences/extract/step3

{
  "duplicateBatchId": "f128a54c-0525-4d89-96bb-ce8f7eb602b3",
  "groups": [
    {
      "groupId": "9f83540e-5d78-4a08-a284-78cecfdf2e3d",
      "selectedItemIds": ["dddddddd-draft-0001-0001-000000000001"]
    }
  ]
}
```

> - `selectedItemIds`: 남길 경험의 `itemId` 목록
> - 기존 경험과 추출 draft를 함께 선택할 수 있음
> - **모든 그룹에 대해 선택을 제공해야 합니다** (그룹 누락 시 400)

**Response** `200`

```json
{
  "selectedExperiences": [
    {
      "id": "dddddddd-draft-0001-0001-000000000001",
      "title": "금융 AI Agent 프로젝트 (재추출)",
      "experienceType": "PROJECT",
      "experienceGroup": "NARRATIVE",
      "status": "COMPLETED"
    }
  ],
  "deletedExperienceIds": ["aaaaaaaa-0001-0001-0001-000000000001"]
}
```

---

## 6. 에러 케이스 정리

| HTTP | 발생 조건 | 응답 예시 |
|------|-----------|-----------|
| `401` | 쿠키 없음 또는 JWT 만료 | `{"status":401,"message":"인증이 필요합니다."}` |
| `400` | `selectedTempIds` 비어 있음 | `{"status":400,"message":"처리할 임시 경험이 없습니다."}` |
| `400` | 다른 사용자 소유의 temp ID 포함 | `{"status":400,"message":"해당 임시 경험에 접근 권한이 없습니다."}` |
| `400` | 이미 처리된 batch ID로 Step3 요청 | `{"status":400,"message":"이미 처리된 batch입니다."}` |
| `400` | Step3에서 그룹 일부 누락 | `{"status":400,"message":"모든 중복 그룹에 대한 선택이 필요합니다."}` |
| `400` | Step3의 `selectedItemIds`가 비어 있음 | `{"status":400,"message":"그룹마다 하나 이상의 경험을 선택해야 합니다."}` |
| `500` | AI 서버 미응답 / OpenAI quota 초과 | `{"status":500,"message":"AI 서버 호출 실패"}` |

---

## 7. 상태 전이 다이어그램

### ExperienceExtractionBatch 상태

```
Step2 호출 (중복 감지)
        ↓
    PENDING
        ↓ Step3 호출 (선택 완료)
    COMPLETED
```

### 경험 추출 전체 화면 전이

```
[홈 / 경험 목록]
        ↓ "자소서로 경험 추출" 버튼
[Step1: PDF 업로드]
        ↓ 후보 목록 반환
[Step2: 경험 선택 화면] — 체크박스로 원하는 경험 선택
        ↓ Step2 API 호출
    ┌─────────────────────────────────────────┐
    │ duplicateBatchId == null?               │
    │  YES → [경험 목록]으로 이동 (저장 완료) │
    │  NO  ↓                                  │
    └──────[Step3: 중복 선택 화면]────────────┘
                ↓ 각 그룹에서 선택
            Step3 API 호출
                ↓
        [경험 목록]으로 이동
```

### 앱 재시작 시 중복 복원

```
앱 로드 시 GET /duplicates/pending 자동 호출
        ↓
    batches.length > 0?
      YES → [Step3: 중복 선택 화면] 표시
      NO  → 정상 흐름 진행
```

---

## 8. 검증 완료 / 미완료 범위

### ✅ 검증 완료

| 항목 | 검증 방법 |
|------|-----------|
| BE 자동 테스트 34개 | `./gradlew test` 전부 통과 |
| AI 자동 테스트 34개 | `pytest -q tests` 전부 통과 |
| AI Step1 경험 후보 추출 | 실제 더미 PDF로 검증 |
| AI Step2 V2 상세 경험 추출 | 실제 더미 데이터로 검증 |
| AI merge-check 중복 판정 | 동일 경험 재추출로 중복 batch 생성 확인 |
| BE Step2 V2 전체 경로 (BE→AI→OpenAI→DB 저장) | 더미 temp 주입 후 API 호출로 검증 |
| BE Step3 V2 기존 경험 선택 처리 | 중복 batch 생성 후 Step3 호출 검증 |
| pending duplicate 조회 | batch 생성 후 pending API 확인 |
| BE OpenAPI 경로 등록 | Swagger UI에서 전 경로 확인 |
| 수기 경험 저장 API | POST /api/experiences 직접 호출 |

### ⚠️ 미완료 / 주의 사항

| 항목 | 현황 | 권고 |
|------|------|------|
| BE Step1 실제 S3 업로드 포함 전체 경로 | S3를 우회하여 temp 직접 주입으로 Step2/3 검증만 완료 | 로컬 S3(LocalStack) 또는 실제 버킷으로 Step1 E2E 필요 |
| Gemini vision 이미지 분석 | quota 429로 실패 → GPT 텍스트 fallback으로 성공 | Gemini quota 확인 후 재검증 |
| 실제 프론트 브라우저 클릭 플로우 | 미검증 | 프론트 연동 후 E2E 브라우저 테스트 필요 |
| 운영 DB seed | H2 인메모리 전용 seed만 준비됨 | PostgreSQL 운영 DB용 seed 별도 준비 필요 |

---

## 9. 로컬 DB seed 방법

### 방법 A: API로 seed (권장)

```bash
# 1. 테스트 유저 생성 + JWT 발급
TOKEN=$(curl -s -X POST http://localhost:8080/internal/seed/user \
  -H "Content-Type: application/json" \
  -d '{"email":"test@pickd.local","name":"테스트유저"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")

# 2. 수기 경험 3개 저장
curl -s -X POST http://localhost:8080/api/experiences \
  -H "Content-Type: application/json" -H "Cookie: accessToken=$TOKEN" \
  -d '{"title":"금융 AI Agent 프로젝트","experienceType":"PROJECT","experienceGroup":"NARRATIVE","status":"COMPLETED","documentContent":"추천 정확도 15% 향상","attributes":{"project_name":"금융 AI Agent","role":"백엔드"},"keywords":["문제 해결"],"forceCreate":true}' | python3 -m json.tool

# 3. temp 주입 (Step1 대체)
curl -s -X POST http://localhost:8080/internal/seed/temp \
  -H "Content-Type: application/json" \
  -d '{"email":"test@pickd.local","experiences":[{"name":"금융 AI Agent 프로젝트","type":"PROJECT","group":"NARRATIVE"},{"name":"TOEIC 930점","type":"LANGUAGE","group":"SPEC"}]}' | python3 -m json.tool

# 4. E2E 전체 자동 실행 (위 단계 모두 포함)
python3 scripts/e2e_test.py
```

### 방법 B: H2 콘솔에서 SQL 직접 실행

1. `http://localhost:8080/h2-console` 접속
2. JDBC URL: `jdbc:h2:mem:pickd` / User: `sa`
3. `scripts/seed_h2.sql` 파일 내용을 붙여넣고 실행

### 방법 C: E2E 스크립트 일괄 실행

```bash
# BE + AI 서버가 모두 기동된 상태에서
cd pickd-BE
python3 scripts/e2e_test.py
```

스크립트가 자동으로:
1. 서버 헬스 확인
2. 테스트 유저 생성 + JWT 발급
3. 수기 경험 3개 seed
4. temp experience 주입 (Step1 대체)
5. Step2 V2 호출 + 결과 검증
6. 중복 batch 생성 검증
7. pending duplicates 조회
8. Step3 선택 처리
9. 최종 DB 상태 검증

실행 결과:

```
==================================================
  PICKD 로컬 E2E 테스트
==================================================

[STEP 1 — 서버 헬스 체크]
  ✓ AI 서버 응답
  ✓ BE 서버 응답

[STEP 2 — 테스트 유저 생성 + JWT 획득]
  ✓ POST /internal/seed/user → 200
  ...

[STEP 10 — 최종 DB 상태 검증]
  ✓ temp 0개 (모두 처리됨)
  ✓ 완료된 batch 존재

==================================================
  PASSED: 18
==================================================
```

---

*문의: 명성 (dreamkms2014@pusan.ac.kr)*
