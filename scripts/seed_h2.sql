-- ============================================================
-- PICKD 로컬 H2 수동 seed SQL
-- ============================================================
-- 용도: 프론트 화면 연결 테스트용 더미 데이터
-- 실행: http://localhost:8080/h2-console
--       JDBC URL: jdbc:h2:mem:pickd
--       User: sa / Password: (비워둠)
-- 주의: BE가 local 프로파일로 실행 중일 때만 유효.
--       서버 재시작 시 H2 인메모리 DB가 초기화됩니다.
--       E2E 스크립트(e2e_test.py)는 /internal/seed/* API를 사용하므로
--       이 SQL과 별개입니다.
-- ============================================================


-- ──────────────────────────────────────────────────────────────
-- 1. 테스트 유저
-- ──────────────────────────────────────────────────────────────
-- JWT는 BE 로그로 확인하거나 /internal/seed/user API로 발급

INSERT INTO users (email, name, nickname, picture, phone, birth_date, is_verified, intro,
                   provider, onboarding_step,
                   service_agreed, privacy_agreed, marketing_agreed, push_agreed,
                   last_login_date)
VALUES (
    'test@pickd.local',
    '테스트유저',
    '테스트닉네임',
    NULL,
    '010-0000-0000',
    '19990101',
    TRUE,
    '프론트 연동 테스트용 계정입니다.',
    'GOOGLE',
    'COMPLETED',
    TRUE, TRUE, FALSE, FALSE,
    CURRENT_TIMESTAMP
);


-- ──────────────────────────────────────────────────────────────
-- 2. 저장된 경험 (UserExperience) — 3개
--    id: UUID 문자열, user_id: 위에서 생성된 ID (기본값 1)
-- ──────────────────────────────────────────────────────────────

-- 2-1. PROJECT / NARRATIVE
INSERT INTO user_experiences (id, user_id, title, experience_type, experience_group,
                              status, document_content, attributes, keywords,
                              created_at, updated_at)
VALUES (
    'aaaaaaaa-0001-0001-0001-000000000001',
    1,
    '금융 AI Agent 프로젝트',
    'PROJECT',
    'NARRATIVE',
    'COMPLETED',
    '추천 모델 개선을 담당했습니다. 추천 정확도를 15% 향상시켰으며 Spring Boot 기반 REST API를 설계하였습니다.',
    '{"project_name":"금융 AI Agent","role":"백엔드 개발","period":"2023.03 ~ 2023.08","organization":"부산대학교 캡스톤팀","achievements":"추천 정확도 15% 향상"}',
    '["문제 해결","실행력","API 설계"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 2-2. LANGUAGE / SPEC
INSERT INTO user_experiences (id, user_id, title, experience_type, experience_group,
                              status, document_content, attributes, keywords,
                              created_at, updated_at)
VALUES (
    'aaaaaaaa-0002-0002-0002-000000000002',
    1,
    'TOEIC 930점',
    'LANGUAGE',
    'SPEC',
    'COMPLETED',
    NULL,
    '{"score":"930","test_name":"TOEIC","acquired_date":"2023-06"}',
    '["어학","영어"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 2-3. INTERN / NARRATIVE
INSERT INTO user_experiences (id, user_id, title, experience_type, experience_group,
                              status, document_content, attributes, keywords,
                              created_at, updated_at)
VALUES (
    'aaaaaaaa-0003-0003-0003-000000000003',
    1,
    '카카오 서버 인턴십',
    'INTERN',
    'NARRATIVE',
    'COMPLETED',
    'Spring Boot 기반 API 개발 및 유지보수를 담당했습니다. 코드 리뷰 문화에 적응하며 팀원과의 협업 역량을 키웠습니다.',
    '{"company":"카카오","role":"서버 개발","period":"2023.07 ~ 2023.08"}',
    '["Spring","Java","협업","코드리뷰"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);


-- ──────────────────────────────────────────────────────────────
-- 3. ExperienceTemp — Step1 완료 후 상태 흉내내기
--    프론트가 "Step2 진입 직후" 화면을 테스트할 수 있도록
-- ──────────────────────────────────────────────────────────────

INSERT INTO experience_temps (user_id, experience_name, experience_group, experience_type, resume_url, created_at)
VALUES (1, '공모전 수상 경험', 'NARRATIVE', 'CONTEST', 'https://example.com/dummy-resume.pdf', CURRENT_TIMESTAMP);

INSERT INTO experience_temps (user_id, experience_name, experience_group, experience_type, resume_url, created_at)
VALUES (1, '정보처리기사 취득', 'SPEC', 'LICENSE', 'https://example.com/dummy-resume.pdf', CURRENT_TIMESTAMP);


-- ──────────────────────────────────────────────────────────────
-- 4. 중복 처리 테스트용 PENDING batch + group + draft
--    프론트가 "Step3 중복 선택 화면"을 바로 테스트할 수 있도록
-- ──────────────────────────────────────────────────────────────

-- 4-1. ExperienceExtractionBatch (PENDING)
INSERT INTO experience_extraction_batches (id, user_id, status, created_at)
VALUES ('bbbbbbbb-batch-0001-0001-000000000001', 1, 'PENDING', CURRENT_TIMESTAMP);

-- 4-2. ExperienceDuplicateGroup
--      existing_experience_id = 위에서 넣은 '금융 AI Agent 프로젝트' ID
INSERT INTO experience_duplicate_groups (id, batch_id, existing_experience_id)
VALUES (
    'cccccccc-group-0001-0001-000000000001',
    'bbbbbbbb-batch-0001-0001-000000000001',
    'aaaaaaaa-0001-0001-0001-000000000001'
);

-- 4-3. ExperienceExtractionDraft (추출 후보)
INSERT INTO experience_extraction_drafts (id, duplicate_group_id,
    title, experience_type, experience_group, status,
    document_content, attributes, keywords, resume_url, similarity)
VALUES (
    'dddddddd-draft-0001-0001-000000000001',
    'cccccccc-group-0001-0001-000000000001',
    '금융 AI Agent 프로젝트 (재추출)',
    'PROJECT',
    'NARRATIVE',
    'COMPLETED',
    '추천 시스템 개선을 통해 정확도를 15% 향상시킨 경험입니다.',
    '{"project_name":"금융 AI Agent","role":"백엔드 개발"}',
    '["문제 해결","실행력"]',
    'https://example.com/dummy-resume.pdf',
    0.92
);


-- ──────────────────────────────────────────────────────────────
-- 5. COMPLETED batch 샘플 (Step3 완료 상태 확인용)
-- ──────────────────────────────────────────────────────────────

INSERT INTO experience_extraction_batches (id, user_id, status, created_at, processed_at)
VALUES (
    'eeeeeeee-batch-done-0001-000000000002',
    1,
    'COMPLETED',
    DATEADD(DAY, -1, CURRENT_TIMESTAMP),
    CURRENT_TIMESTAMP
);


-- ──────────────────────────────────────────────────────────────
-- 확인 쿼리
-- ──────────────────────────────────────────────────────────────
-- SELECT * FROM users;
-- SELECT id, title, experience_type, status FROM user_experiences;
-- SELECT id, experience_name, experience_type FROM experience_temps;
-- SELECT id, status FROM experience_extraction_batches;
-- SELECT id, batch_id, existing_experience_id FROM experience_duplicate_groups;
-- SELECT id, duplicate_group_id, title, similarity FROM experience_extraction_drafts;
