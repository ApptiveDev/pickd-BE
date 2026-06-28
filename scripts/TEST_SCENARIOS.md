# PICKD API 수동 테스트 시나리오

> 작성일: 2026-06-26  
> 테스트 환경: 로컬 (BE `localhost:8080` / AI `localhost:8000`)  
> 도구: Swagger UI, curl, 또는 Postman

---

## 사전 준비

### 1. 서버 기동

```bash
# 터미널 1 — BE
cd pickd-BE
./gradlew bootRun --args='--spring.profiles.active=local'

# 터미널 2 — AI
cd pickd-AI/myeongsung
source .venv/bin/activate
uvicorn app.main:app --reload --port 8000
```

### 2. 테스트 계정 + JWT 발급

```bash
curl -s -X POST http://localhost:8080/internal/seed/user \
  -H "Content-Type: application/json" \
  -d '{"email":"test@pickd.local","name":"테스트유저"}'
```

응답에서 `accessToken` 복사 → 브라우저 쿠키에 수동 설정

```
이름:  accessToken
값:    eyJhbGciOi...  (복사한 토큰)
도메인: localhost
경로:  /
```

> Swagger UI 사용 시: `http://localhost:8080/swagger-ui/index.html` → 우상단 **Authorize** → `accessToken` 쿠키 값 붙여넣기

### 3. 변수 메모 공간 (테스트 진행하며 채워나가기)

```
JWT_TOKEN      = 
NOTICE_ID      = 
APPLICATION_ID = 
TODO_ID        = 
DOCUMENT_ID    = 
COVER_LETTER_ID = 
EXPERIENCE_ID_1 =
EXPERIENCE_ID_2 =
EXPERIENCE_ID_3 =
FILE_ID        =
CALENDAR_EVENT_ID =
TEMP_ID_1      =
TEMP_ID_2      =
DUPLICATE_BATCH_ID =
DUPLICATE_GROUP_ID =
```

---

## 도메인 1 — User

### T-01 내 프로필 조회

```
GET /api/user
```

**확인 포인트**
- [ ] 200 반환
- [ ] `email: "test@pickd.local"` 확인
- [ ] `onboardingStep: "COMPLETED"` 확인 (seed로 생성했으므로)

---

### T-02 프로필 이미지 업로드

```
POST /api/user/profile-image
Content-Type: multipart/form-data

file: (아무 JPG/PNG 파일)
```

**확인 포인트**
- [ ] 200 반환
- [ ] 응답에 `profileImageUrl` 포함
- [ ] URL이 `https://` 로 시작

---

### T-03 프로필 이미지 조회

```
GET /api/user/profile-image
```

**확인 포인트**
- [ ] 200 반환
- [ ] T-02에서 업로드한 URL과 동일

---

## 도메인 2 — Onboarding

### T-04 온보딩 상태 조회

```
GET /api/onboarding/status
```

**확인 포인트**
- [ ] 200 반환
- [ ] `onboardingStep` 값 확인 (seed: `COMPLETED`)

---

### T-05 온보딩 초기화

```
POST /api/onboarding/reset
```

**확인 포인트**
- [ ] 200 반환, `"Reset complete"` 응답

---

### T-06 온보딩 Step1 — 약관 동의

```json
POST /api/onboarding

{
  "serviceAgreed": true,
  "privacyAgreed": true,
  "marketingAgreed": false,
  "pushAgreed": true
}
```

**확인 포인트**
- [ ] 200 반환
- [ ] `onboardingStep: "TERMS"` 또는 다음 단계 확인

---

### T-07 온보딩 Step3 — 기본 정보

```json
POST /api/onboarding

{
  "nickname": "명성",
  "intro": "백엔드 개발을 공부하는 부산대학교 4학년입니다.",
  "currentResidence": "부산",
  "desiredLocations": ["서울", "부산"],
  "detailedAddress": "부산광역시 금정구"
}
```

**확인 포인트**
- [ ] 200 반환
- [ ] `nickname: "명성"` 확인
- [ ] `onboardingStep` 진행됨

---

### T-08 온보딩 Step4 — 학력

```json
POST /api/onboarding

{
  "schoolName": "부산대학교",
  "department": "정보컴퓨터공학부",
  "degreeType": "BACHELOR",
  "enrollmentStatus": "GRADUATED",
  "graduationDate": "2026-02",
  "gpa": 3.9,
  "campus": "부산 캠퍼스"
}
```

---

### T-09 온보딩 Step5 — 관심 분야

```json
POST /api/onboarding

{
  "industries": ["IT/소프트웨어", "핀테크"],
  "jobGroups": ["백엔드 개발", "서버 개발"],
  "employmentType": "정규직",
  "companyTypes": ["대기업", "스타트업"],
  "keywords": ["Spring Boot", "Java", "MSA"],
  "targetCompany": "카카오",
  "salaryRange": "4000만원 이상"
}
```

---

### T-10 온보딩 Step6 — 준비 현황 (COMPLETED)

```json
POST /api/onboarding

{
  "targetPeriod": "2026 하반기",
  "currentStage": "서류 준비",
  "focusItems": ["자기소개서", "포트폴리오"],
  "hasResume": true,
  "hasBaseEssay": true,
  "hasPortfolio": false,
  "experiences": [
    { "type": "인턴", "title": "카카오 서버 인턴", "startDate": "2025-07", "endDate": "2025-08" }
  ],
  "certifications": [
    { "name": "정보처리기사", "score": null, "acquisitionDate": "2025-11" }
  ]
}
```

**확인 포인트**
- [ ] 200 반환
- [ ] `onboardingStep: "COMPLETED"`

---

## 도메인 3 — Notice (채용공고 분석)

> **주의**: 이 API는 BE → AI → OpenAI 호출로 20~40초 소요됩니다.

### T-11 URL 기반 채용공고 분석

```json
POST /api/notices/analyze/url

{
  "url": "여기에_찾은_채용공고_URL_입력"
}
```

**추천 사이트 (공고 URL 직접 복사)**
- 사람인: https://www.saramin.co.kr → 공고 클릭 → URL 복사
- 잡코리아: https://www.jobkorea.co.kr → 공고 클릭 → URL 복사
- 카카오 채용: https://careers.kakao.com/jobs
- 원티드: https://www.wanted.co.kr/jobsfeed

**확인 포인트**
- [ ] 200 반환
- [ ] 응답에 `noticeId` 포함 → **NOTICE_ID 메모**
- [ ] Application이 자동 생성되었는지 GET /api/application 으로 확인

---

### T-12 PDF 기반 채용공고 분석

```
POST /api/notices/analyze/pdf
Content-Type: multipart/form-data

file: (채용공고 PDF 파일)
```

> 채용공고 인쇄 → PDF 저장 또는 채용 사이트에서 공고 PDF 다운로드

**확인 포인트**
- [ ] 200 반환
- [ ] 새로운 `noticeId` 반환

---

## 도메인 4 — Application (지원 공고)

> T-11/T-12에서 공고 분석 시 Application이 자동 생성됩니다.  
> 아래는 **추가** 수기 입력 테스트입니다.

### T-13 수기 지원 공고 추가

```json
POST /api/application

{
  "company": "네이버",
  "jobTitle": "2026 상반기 서버 개발자 신입공채",
  "position": "백엔드 개발",
  "industry": "IT/소프트웨어",
  "status": "PREPARING",
  "memo": "네이버 클라우드 팀 관심 있음",
  "deadlineDate": "2026-07-31T23:59:59",
  "important": true
}
```

**확인 포인트**
- [ ] 200 반환
- [ ] 응답에서 `id` 확인 → **APPLICATION_ID 메모**
- [ ] `status`가 한국어로 직렬화되는지 확인 (예: `"서류 준비"`)

---

### T-14 지원 공고 목록 조회

```
GET /api/application
```

**확인 포인트**
- [ ] 200 반환, 배열 반환
- [ ] T-11/T-12 자동 생성 + T-13 수기 입력 포함 확인
- [ ] `todos`, `documents` 배열 확인

---

### T-15 지원 공고 수정

```json
PUT /api/application/{APPLICATION_ID}

{
  "status": "APPLIED",
  "memo": "서류 제출 완료 (2026-06-27)",
  "applyDate": "2026-06-27T10:00:00",
  "important": true
}
```

**확인 포인트**
- [ ] 200 반환
- [ ] `status` 변경 확인
- [ ] `updatedAt` 변경 확인

---

### T-16 지원 공고 삭제

> 새로 추가 공고 하나 더 만들고 삭제 테스트

```json
POST /api/application
{
  "company": "삭제테스트회사",
  "jobTitle": "삭제용 공고",
  "status": "PREPARING"
}
```

```
DELETE /api/application/{삭제용_APPLICATION_ID}
```

**확인 포인트**
- [ ] 204 반환
- [ ] GET /api/application 재조회 시 해당 항목 없어짐

---

## 도메인 5 — Todo

### T-17 할 일 추가 (공고 연결)

```json
POST /api/todo

{
  "title": "네이버 자기소개서 1차 작성",
  "dueDateTime": "2026-07-10T23:59:59",
  "memo": "직무 역량 키워드 중심으로 작성",
  "applicationId": {APPLICATION_ID},
  "company": "네이버",
  "jobTitle": "2026 상반기 서버 개발자 신입공채"
}
```

**확인 포인트**
- [ ] 200 반환
- [ ] `id` 확인 → **TODO_ID 메모**
- [ ] `calendarEventId` 값 확인 (Google Calendar 연동 시 non-null)

---

### T-18 할 일 두 번째 추가

```json
POST /api/todo

{
  "title": "포트폴리오 PDF 변환",
  "dueDateTime": "2026-07-05T18:00:00",
  "memo": "Notion → PDF 내보내기",
  "applicationId": {APPLICATION_ID},
  "company": "네이버",
  "jobTitle": "2026 상반기 서버 개발자 신입공채"
}
```

---

### T-19 전체 할 일 목록 조회

```
GET /api/todo
```

**확인 포인트**
- [ ] 200 반환
- [ ] T-17, T-18 포함 확인

---

### T-20 공고별 할 일 조회

```
GET /api/todo/application/{APPLICATION_ID}
```

**확인 포인트**
- [ ] 200 반환
- [ ] 해당 공고의 할 일만 반환

---

### T-21 할 일 수정

```json
PUT /api/todo/{TODO_ID}

{
  "title": "네이버 자기소개서 1차 완료",
  "dueDateTime": "2026-07-12T23:59:59",
  "memo": "멘토 피드백 반영 예정"
}
```

**확인 포인트**
- [ ] 200 반환
- [ ] `title` 변경 확인

---

### T-22 할 일 삭제

```
DELETE /api/todo/{TODO_ID_2번}
```

**확인 포인트**
- [ ] 204 반환
- [ ] GET /api/todo 재조회 시 없어짐

---

## 도메인 6 — Document (서류)

### T-23 서류 작성

```json
POST /api/document/{APPLICATION_ID}

{
  "title": "네이버 2026 상반기 이력서",
  "company": "네이버",
  "type": "RESUME",
  "status": "IN_PROGRESS",
  "progress": 20,
  "content": "# 이력서\n\n## 인적사항\n- 이름: 홍길동\n- 학교: 부산대학교 정보컴퓨터공학부\n\n## 경력\n- 카카오 서버 인턴 (2025.07 ~ 2025.08)\n"
}
```

**확인 포인트**
- [ ] 200 반환
- [ ] `id` 확인 → **DOCUMENT_ID 메모**
- [ ] `type`, `status` 확인

---

### T-24 포트폴리오 서류 추가

```json
POST /api/document/{APPLICATION_ID}

{
  "title": "포트폴리오 v1",
  "company": "네이버",
  "type": "PORTFOLIO",
  "status": "IN_PROGRESS",
  "progress": 0,
  "content": "# 포트폴리오\n\n준비 중...\n"
}
```

---

### T-25 공고별 서류 조회

```
GET /api/document/{APPLICATION_ID}
```

**확인 포인트**
- [ ] T-23, T-24 둘 다 반환

---

### T-26 전체 서류 조회

```
GET /api/document
```

---

### T-27 서류 수정

```json
PUT /api/document/{DOCUMENT_ID}

{
  "title": "네이버 2026 상반기 이력서 v2",
  "type": "RESUME",
  "status": "COMPLETED",
  "progress": 90,
  "content": "# 이력서\n\n## 인적사항\n- 이름: 홍길동\n- 학교: 부산대학교 정보컴퓨터공학부 4학년\n\n## 경력\n- 카카오 서버 인턴 (2025.07 ~ 2025.08)\n  - Spring Boot REST API 개발\n  - 코드 리뷰 문화 경험\n"
}
```

**확인 포인트**
- [ ] 200 반환
- [ ] `status: "COMPLETED"`, `progress: 90` 확인

---

### T-28 서류 삭제

> T-24에서 만든 포트폴리오 서류 삭제

```
DELETE /api/document/{PORTFOLIO_DOCUMENT_ID}
```

**확인 포인트**
- [ ] 204 반환

---

## 도메인 7 — CoverLetter (자기소개서)

### T-29 자기소개서 문항 생성

```json
POST /api/cover-letter

{
  "question": "지원 동기 및 입사 후 목표를 서술하세요.",
  "answer": "저는 대규모 트래픽을 처리하는 백엔드 시스템에 관심이 많습니다. 네이버의 기술력을 배우며 성장하고 싶습니다.",
  "maxLength": 500,
  "orderIndex": 1,
  "aiGenerated": false,
  "applicationId": {APPLICATION_ID}
}
```

**확인 포인트**
- [ ] 200 반환
- [ ] `id` 확인 → **COVER_LETTER_ID 메모**

---

### T-30 자기소개서 두 번째 문항

```json
POST /api/cover-letter

{
  "question": "본인의 강점과 이를 발휘한 경험을 서술하세요.",
  "answer": "저의 강점은 문제 해결 능력입니다. 캡스톤 프로젝트에서 추천 시스템 정확도를 15% 향상시킨 경험이 있습니다.",
  "maxLength": 800,
  "orderIndex": 2,
  "aiGenerated": false,
  "applicationId": {APPLICATION_ID}
}
```

---

### T-31 자기소개서 문항 조회

```
GET /api/cover-letter
```

**확인 포인트**
- [ ] T-29, T-30 둘 다 반환

---

### T-32 자기소개서 수정

```json
PUT /api/cover-letter/{COVER_LETTER_ID}

{
  "question": "지원 동기 및 입사 후 목표를 서술하세요.",
  "answer": "저는 대규모 트래픽을 처리하는 백엔드 시스템에 깊은 관심이 있습니다. 네이버의 검색 인프라와 클라우드 기술을 배우며, 5년 내 시스템 아키텍트로 성장하고 싶습니다.",
  "maxLength": 500,
  "orderIndex": 1,
  "aiGenerated": false
}
```

---

### T-33 자기소개서 문항 삭제

```
DELETE /api/cover-letter/{COVER_LETTER_ID_2번}
```

**확인 포인트**
- [ ] 204 반환

---

## 도메인 8 — File

### T-34 파일 업로드

```
POST /api/files/upload
Content-Type: multipart/form-data

file: (이력서 PDF 파일)
type: RESUME
```

**확인 포인트**
- [ ] 200 반환
- [ ] `id`, `fileName`, `fileUrl`, `uploadType: "RESUME"` 확인 → **FILE_ID 메모**
- [ ] `fileUrl`이 CDN URL인지 확인

---

### T-35 파일 목록 조회 (전체)

```
GET /api/files
```

---

### T-36 파일 목록 조회 (타입 필터)

```
GET /api/files?type=RESUME
```

**확인 포인트**
- [ ] RESUME 타입만 반환

---

## 도메인 9 — Calendar

> Google Calendar가 연동된 계정이어야 실제 동작합니다.  
> 로컬 테스트 계정(test@pickd.local)은 Google OAuth가 없으므로 **401 또는 500 예상**.  
> 실제 Google 로그인 계정으로 테스트하거나 오류 응답 코드만 확인합니다.

### T-37 내 캘린더 이메일 확인

```
GET /api/calendar/me
```

**확인 포인트**
- [ ] Google OAuth 미연동 시: 500 또는 오류
- [ ] 연동 시: 이메일 문자열 반환

---

### T-38 일정 등록

```json
POST /api/calendar/events

{
  "summary": "네이버 서류 마감",
  "location": "온라인 지원",
  "description": "네이버 2026 상반기 서버 개발자 신입공채 서류 마감",
  "start": {
    "dateTime": "2026-07-31T23:59:59",
    "timeZone": "Asia/Seoul"
  },
  "end": {
    "dateTime": "2026-07-31T23:59:59",
    "timeZone": "Asia/Seoul"
  }
}
```

**확인 포인트**
- [ ] 200 반환 (Google OAuth 연동 시)
- [ ] `id` 확인 → **CALENDAR_EVENT_ID 메모**

---

### T-39 일정 목록 조회

```
GET /api/calendar/events
```

---

### T-40 일정 수정

```json
PUT /api/calendar/events/{CALENDAR_EVENT_ID}

{
  "summary": "네이버 서류 마감 ⚠️",
  "description": "마감 하루 전 재확인 필요"
}
```

---

### T-41 일정 삭제

```
DELETE /api/calendar/events/{CALENDAR_EVENT_ID}
```

---

## 도메인 10 — Experience (수기 CRUD)

### T-42 수기 경험 생성 — PROJECT

```json
POST /api/experiences

{
  "title": "금융 AI Agent 추천 시스템 프로젝트",
  "experienceType": "PROJECT",
  "experienceGroup": "NARRATIVE",
  "status": "COMPLETED",
  "documentContent": "LangGraph와 Spring Boot를 활용해 금융 상품 추천 AI Agent를 개발했습니다. 추천 정확도를 15% 향상시켰으며, 팀 내 기술 리드로 API 설계를 담당했습니다.",
  "attributes": {
    "project_name": "금융 AI Agent 추천 시스템",
    "role": "백엔드 개발 및 AI 연동",
    "period": "2025.03 ~ 2025.08",
    "organization": "부산대학교 캡스톤 디자인팀",
    "achievements": "추천 정확도 15% 향상, 4인 팀 기술 리드"
  },
  "keywords": ["문제 해결", "실행력", "API 설계", "LangGraph", "Spring Boot"],
  "links": [
    {
      "title": "GitHub 저장소",
      "url": "https://github.com/example/finance-ai-agent",
      "materialType": "GITHUB"
    }
  ],
  "forceCreate": false
}
```

**확인 포인트**
- [ ] 200 반환
- [ ] `id` 확인 → **EXPERIENCE_ID_1 메모** (UUID 형태)
- [ ] `attributes`, `keywords`, `links` 저장 확인

---

### T-43 수기 경험 생성 — LANGUAGE

```json
POST /api/experiences

{
  "title": "TOEIC 930점",
  "experienceType": "LANGUAGE",
  "experienceGroup": "SPEC",
  "status": "COMPLETED",
  "documentContent": null,
  "attributes": {
    "score": "930",
    "test_name": "TOEIC",
    "acquired_date": "2025-06"
  },
  "keywords": ["어학", "영어"],
  "forceCreate": true
}
```

**확인 포인트**
- [ ] 200 반환
- [ ] `id` 확인 → **EXPERIENCE_ID_2 메모**

---

### T-44 수기 경험 생성 — INTERN

```json
POST /api/experiences

{
  "title": "카카오 서버 개발 인턴십",
  "experienceType": "INTERN",
  "experienceGroup": "NARRATIVE",
  "status": "COMPLETED",
  "documentContent": "Spring Boot 기반 API 개발 및 유지보수를 담당했습니다. 코드 리뷰 문화를 경험하며 팀워크와 커뮤니케이션 역량을 키웠습니다. 일평균 처리 요청 10만 건 이상의 실서버 환경에서 개발하였습니다.",
  "attributes": {
    "company": "카카오",
    "role": "서버 개발",
    "period": "2025.07 ~ 2025.08",
    "department": "카카오 클라우드 팀"
  },
  "keywords": ["협업", "코드리뷰", "Spring Boot", "Java", "실서버 경험"],
  "forceCreate": true
}
```

**확인 포인트**
- [ ] 200 반환
- [ ] `id` 확인 → **EXPERIENCE_ID_3 메모**

---

### T-45 경험 목록 조회

```
GET /api/experiences
```

**확인 포인트**
- [ ] 200 반환, 3개 포함
- [ ] 각 경험의 `attributes`, `keywords` 포함 확인

---

### T-46 경험 단일 조회

```
GET /api/experiences/{EXPERIENCE_ID_1}
```

**확인 포인트**
- [ ] 200 반환
- [ ] `links` 배열 (GitHub URL) 포함 확인
- [ ] `files` 배열 확인

---

### T-47 경험 수정

```json
PUT /api/experiences/{EXPERIENCE_ID_1}

{
  "title": "금융 AI Agent 추천 시스템 프로젝트 (수정)",
  "experienceType": "PROJECT",
  "experienceGroup": "NARRATIVE",
  "status": "COMPLETED",
  "documentContent": "LangGraph와 Spring Boot를 활용해 금융 상품 추천 AI Agent를 개발했습니다. 추천 정확도를 15% 향상시키고, REST API 응답 시간을 30% 단축하는 성과를 냈습니다.",
  "attributes": {
    "project_name": "금융 AI Agent 추천 시스템",
    "role": "백엔드 개발 및 AI 연동",
    "period": "2025.03 ~ 2025.08",
    "organization": "부산대학교 캡스톤 디자인팀",
    "achievements": "추천 정확도 15% 향상, 응답시간 30% 단축"
  },
  "keywords": ["문제 해결", "실행력", "성능 최적화", "API 설계"]
}
```

**확인 포인트**
- [ ] 200 반환
- [ ] `documentContent`, `keywords` 변경 확인
- [ ] `updatedAt` 변경 확인

---

### T-48 경험 삭제

> 별도로 하나 더 만들어 삭제 테스트

```json
POST /api/experiences
{
  "title": "삭제 테스트용 경험",
  "experienceType": "ALBA",
  "experienceGroup": "NARRATIVE",
  "status": "IN_PROGRESS",
  "forceCreate": true
}
```

```
DELETE /api/experiences/{삭제용_EXPERIENCE_ID}
```

**확인 포인트**
- [ ] 204 반환
- [ ] GET /api/experiences 재조회 시 없어짐

---

## 도메인 11 — Experience Extraction V1

> **Step1 → Step2 → (Step3)** 순서 필수

### T-49 Step1 — 자소서 PDF 업로드 (경험 후보 추출)

```
POST /api/experiences/extract/step1
Content-Type: multipart/form-data

file: (본인 자소서 PDF)
```

> 소요 시간: 15~30초

**확인 포인트**
- [ ] 200 반환
- [ ] 경험 후보 배열 반환 (경험명, 유형 확인)
- [ ] 각 `id` 메모 → **TEMP_ID_1, TEMP_ID_2 메모**
- [ ] 자소서에 언급한 경험이 올바르게 추출됐는지 내용 검토
- [ ] `experienceGroup` 이 NARRATIVE/SPEC 으로 올바르게 분류됐는지 확인

---

### T-50 Step2 V1 — 선택 경험 상세 추출

```json
POST /api/experiences/extract/step2

{
  "selectedTempIds": [TEMP_ID_1, TEMP_ID_2]
}
```

> 소요 시간: 30~60초 (경험 수에 비례)

**확인 포인트**
- [ ] 200 반환
- [ ] `savedExperiences`: 즉시 저장된 경험 확인
- [ ] `mergeCandidates`: 기존 경험과 중복 여부 확인
- [ ] 저장된 경험의 `documentContent` 내용이 자소서 내용 반영했는지 확인
- [ ] `attributes` 필드에 프로젝트명, 역할, 기간 등 추출됐는지 확인

---

### T-51 Step3 V1 — 중복 경험 처리 (mergeCandidates 있을 경우)

> T-50 응답의 `mergeCandidates` 가 비어있으면 스킵

```json
POST /api/experiences/extract/step3

{
  "decisions": [
    {
      "action": "CREATE_NEW",
      "draft": {
        "title": "중복으로 감지된 경험 제목",
        "experienceType": "PROJECT",
        "experienceGroup": "NARRATIVE",
        "status": "COMPLETED",
        "documentContent": "내용...",
        "attributes": {},
        "keywords": []
      }
    }
  ]
}
```

> `action`: `CREATE_NEW` (새로 저장) 또는 `SKIP` (건너뜀)

---

## 도메인 12 — Experience Extraction V2 ⭐

> 핵심 플로우. **Step1(V1) → Step2 V2 → Step3 V2** 순서  
> Step1은 V1과 동일 엔드포인트 사용

### T-52 Step1 — 자소서 PDF 업로드 (기존 temp 초기화 후 재추출)

```
POST /api/experiences/extract/step1
Content-Type: multipart/form-data

file: (본인 자소서 PDF)
```

> **주의**: Step1 호출 시 이전 temp가 초기화됩니다.

**확인 포인트**
- [ ] 200 반환
- [ ] temp ID 목록 메모
- [ ] 경험 후보 내용 검토 (V1과 동일하나 V2 step2와 연동할 ID 확인)

---

### T-53 Step2 V2 — 상세 추출 + 중복 판정

```json
POST /api/v2/experiences/extract/step2

{
  "selectedTempIds": [TEMP_ID_1, TEMP_ID_2]
}
```

> 소요 시간: 30~60초

**확인 포인트**
- [ ] 200 반환
- [ ] `savedExperiences` 확인 (중복 아닌 경험 즉시 저장됨)
- [ ] `duplicateBatchId` 확인
  - **null**: 중복 없음 → 플로우 종료
  - **non-null**: → **DUPLICATE_BATCH_ID 메모**, Step3 진행
- [ ] `duplicateGroups` 배열 확인
  - 각 그룹의 `groupId` 메모 → **DUPLICATE_GROUP_ID 메모**
  - `source: "EXISTING"` 항목 = 기존 저장 경험
  - `source: "EXTRACTED"` 항목 = 이번 추출 후보
  - `similarity` 값 확인 (0.8 이상이면 중복 판정)

---

### T-54 pending duplicates 조회 (앱 재시작 복원 시뮬레이션)

```
GET /api/v2/experiences/extract/duplicates/pending
```

**확인 포인트**
- [ ] 200 반환
- [ ] T-53에서 생성된 batch 포함 확인
- [ ] `duplicateBatchId`, `duplicateGroups` 구조 확인

---

### T-55 Step3 V2 — 중복 최종 선택 (기존 경험 유지)

> 기존 경험(`source: "EXISTING"`)을 선택하는 시나리오

```json
POST /api/v2/experiences/extract/step3

{
  "duplicateBatchId": "{DUPLICATE_BATCH_ID}",
  "groups": [
    {
      "groupId": "{DUPLICATE_GROUP_ID}",
      "selectedItemIds": ["{EXISTING_ITEM_ID}"]
    }
  ]
}
```

**확인 포인트**
- [ ] 200 반환
- [ ] `selectedExperiences`: 선택된 경험 확인
- [ ] `deletedExperienceIds`: 삭제된 경험 ID 확인
- [ ] GET /api/experiences 재조회 → 선택된 경험만 남아있는지 확인
- [ ] GET /api/v2/experiences/extract/duplicates/pending → batch 없어짐 확인

---

### T-56 Step3 V2 — 중복 최종 선택 (추출 후보 사용) — 재테스트

> 이번에는 추출 후보(`source: "EXTRACTED"`)를 선택하는 시나리오  
> Step1 → Step2를 다시 실행해서 중복 batch 재생성 후 테스트

```json
POST /api/v2/experiences/extract/step3

{
  "duplicateBatchId": "{NEW_DUPLICATE_BATCH_ID}",
  "groups": [
    {
      "groupId": "{GROUP_ID}",
      "selectedItemIds": ["{EXTRACTED_ITEM_ID}"]
    }
  ]
}
```

**확인 포인트**
- [ ] 기존 경험이 추출 후보로 교체됐는지 확인
- [ ] `deletedExperienceIds` 에 기존 경험 ID 포함

---

## 에러 케이스 테스트

### E-01 인증 없이 API 호출

```bash
curl -s http://localhost:8080/api/user
```

**기대**: `401 Unauthorized`

---

### E-02 잘못된 JWT

```bash
curl -s http://localhost:8080/api/user \
  -H "Cookie: accessToken=invalidtoken123"
```

**기대**: `401 Unauthorized`

---

### E-03 없는 리소스 조회

```
GET /api/experiences/00000000-0000-0000-0000-000000000000
```

**기대**: `400` 또는 `404`

---

### E-04 Step2 — temp ID 없이 호출

```json
POST /api/v2/experiences/extract/step2

{
  "selectedTempIds": []
}
```

**기대**: `400 Bad Request`

---

### E-05 이미 처리된 batch로 Step3 재호출

> T-55 완료 후 동일한 batchId로 Step3 재호출

```json
POST /api/v2/experiences/extract/step3

{
  "duplicateBatchId": "{COMPLETED_BATCH_ID}",
  "groups": [ ... ]
}
```

**기대**: `400 Bad Request` + 에러 메시지 확인

---

### E-06 Notice URL 분석 — 잘못된 URL

```json
POST /api/notices/analyze/url

{
  "url": ""
}
```

**기대**: `400 Bad Request` (validation)

---

### E-07 Application 필수 필드 누락

```json
POST /api/application

{
  "company": "테스트"
}
```

> `status` 누락

**기대**: `400 Bad Request`

---

## 테스트 완료 체크리스트

### User / Onboarding
- [ ] T-01 프로필 조회
- [ ] T-02 이미지 업로드
- [ ] T-03 이미지 조회
- [ ] T-04 온보딩 상태 조회
- [ ] T-05 온보딩 초기화
- [ ] T-06~10 온보딩 단계별 저장

### Notice
- [ ] T-11 URL 분석 _(URL 직접 찾아 입력)_
- [ ] T-12 PDF 분석

### Application
- [ ] T-13 수기 추가
- [ ] T-14 목록 조회
- [ ] T-15 수정
- [ ] T-16 삭제

### Todo
- [ ] T-17~18 추가
- [ ] T-19~20 조회
- [ ] T-21 수정
- [ ] T-22 삭제

### Document
- [ ] T-23~24 작성
- [ ] T-25~26 조회
- [ ] T-27 수정
- [ ] T-28 삭제

### CoverLetter
- [ ] T-29~30 생성
- [ ] T-31 조회
- [ ] T-32 수정
- [ ] T-33 삭제

### File
- [ ] T-34 업로드
- [ ] T-35~36 조회

### Calendar
- [ ] T-37~41 _(Google OAuth 연동 필요)_

### Experience CRUD
- [ ] T-42~44 수기 생성 3개
- [ ] T-45~46 조회
- [ ] T-47 수정
- [ ] T-48 삭제

### Experience Extraction V1
- [ ] T-49 Step1 (자소서 PDF 업로드)
- [ ] T-50 Step2
- [ ] T-51 Step3 _(중복 있을 경우)_

### Experience Extraction V2 ⭐
- [ ] T-52 Step1 (자소서 PDF 업로드)
- [ ] T-53 Step2 V2 (중복 판정)
- [ ] T-54 pending 조회
- [ ] T-55 Step3 V2 (기존 유지)
- [ ] T-56 Step3 V2 (추출 후보 선택)

### 에러 케이스
- [ ] E-01 ~ E-07

---

## 빠른 상태 확인 명령

```bash
# 내 현재 경험/temp/batch 상태 한번에 확인
curl -s "http://localhost:8080/internal/experience-extraction-test/state" \
  -H "Cookie: accessToken={JWT_TOKEN}" | python3 -m json.tool
```

출력 예시:

```json
{
  "email": "test@pickd.local",
  "experiences": [ ... ],   // 저장된 경험
  "temps": [ ... ],         // Step1 후보 (처리 전)
  "batches": [ ... ]        // 중복 batch 목록 + 상태
}
```
