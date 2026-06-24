# Archived: NoticeCompanyInfo & NoticeGuideline

> **이슈 #61** 에서 일시 제거됨 (2024-06)
> AI `/analyze-and-place` 응답에 해당 필드가 없어 현재 populate 불가능 → dead entity 판정
> 추후 AI 응답에 해당 데이터가 포함될 경우 복구 필요

---

## NoticeCompanyInfo

**테이블:** `notice_company_infos`  
**관계:** Notice 와 1:1 (notice_id FK, CASCADE DELETE)  
**복구 위치:** `notice/entity/NoticeCompanyInfo.java`

### 필드

| 필드명 | 컬럼 | 설명 |
|---|---|---|
| `id` | PK | 기본키 |
| `notice` | `notice_id` FK | 연결된 공고 |
| `companyIntroduction` | `company_introduction` TEXT | 기업 소개 |
| `mission` | `mission` TEXT | 미션 |
| `vision` | `vision` TEXT | 비전 |
| `idealCandidate` | `ideal_candidate` TEXT | 인재상 |
| `businessOverview` | `business_overview` TEXT | 사업 개요 |
| `workingConditions` | `working_conditions` TEXT | 근무 조건 |
| `compensation` | `compensation` TEXT | 급여/보상 체계 |
| `benefits` | `benefits` TEXT | 복리후생 |

### 복구 코드

```java
package back.pickd.notice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "notice_company_infos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeCompanyInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Notice notice;

    @Column(name = "company_introduction", columnDefinition = "TEXT")
    private String companyIntroduction;

    @Column(columnDefinition = "TEXT")
    private String mission;

    @Column(columnDefinition = "TEXT")
    private String vision;

    @Column(name = "ideal_candidate", columnDefinition = "TEXT")
    private String idealCandidate;

    @Column(name = "business_overview", columnDefinition = "TEXT")
    private String businessOverview;

    @Column(name = "working_conditions", columnDefinition = "TEXT")
    private String workingConditions;

    @Column(columnDefinition = "TEXT")
    private String compensation;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void assignNotice(Notice notice) {
        this.notice = notice;
    }

    @Builder
    public NoticeCompanyInfo(Notice notice, String companyIntroduction, String mission,
                             String vision, String idealCandidate, String businessOverview,
                             String workingConditions, String compensation, String benefits) {
        this.notice = notice;
        this.companyIntroduction = companyIntroduction;
        this.mission = mission;
        this.vision = vision;
        this.idealCandidate = idealCandidate;
        this.businessOverview = businessOverview;
        this.workingConditions = workingConditions;
        this.compensation = compensation;
        this.benefits = benefits;
    }
}
```

---

## NoticeGuideline

**테이블:** `notice_guidelines`  
**관계:** Notice 와 1:1 (notice_id FK, CASCADE DELETE)  
**복구 위치:** `notice/entity/NoticeGuideline.java`

### 필드

| 필드명 | 컬럼 | 설명 |
|---|---|---|
| `id` | PK | 기본키 |
| `notice` | `notice_id` FK | 연결된 공고 |
| `generalNotes` | `general_notes` TEXT | 일반 유의사항 |
| `duplicateApplyRestriction` | `duplicate_apply_restriction` TEXT | 중복 지원 제한 |
| `falseInfoWarning` | `false_info_warning` TEXT | 허위 기재 경고 |
| `cancellationConditions` | `cancellation_conditions` TEXT | 합격 취소 조건 |
| `recruitmentCancelPossibility` | `recruitment_cancel_possibility` TEXT | 채용 취소 가능성 |
| `reserveCandidateGuide` | `reserve_candidate_guide` TEXT | 예비 합격자 안내 |
| `contactInfo` | `contact_info` TEXT | 문의처 |
| `otherGuides` | `other_guides` TEXT | 기타 안내사항 |

### 복구 코드

```java
package back.pickd.notice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "notice_guidelines")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeGuideline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Notice notice;

    @Column(name = "general_notes", columnDefinition = "TEXT")
    private String generalNotes;

    @Column(name = "duplicate_apply_restriction", columnDefinition = "TEXT")
    private String duplicateApplyRestriction;

    @Column(name = "false_info_warning", columnDefinition = "TEXT")
    private String falseInfoWarning;

    @Column(name = "cancellation_conditions", columnDefinition = "TEXT")
    private String cancellationConditions;

    @Column(name = "recruitment_cancel_possibility", columnDefinition = "TEXT")
    private String recruitmentCancelPossibility;

    @Column(name = "reserve_candidate_guide", columnDefinition = "TEXT")
    private String reserveCandidateGuide;

    @Column(name = "contact_info", columnDefinition = "TEXT")
    private String contactInfo;

    @Column(name = "other_guides", columnDefinition = "TEXT")
    private String otherGuides;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void assignNotice(Notice notice) {
        this.notice = notice;
    }

    @Builder
    public NoticeGuideline(Notice notice, String generalNotes, String duplicateApplyRestriction,
                           String falseInfoWarning, String cancellationConditions,
                           String recruitmentCancelPossibility, String reserveCandidateGuide,
                           String contactInfo, String otherGuides) {
        this.notice = notice;
        this.generalNotes = generalNotes;
        this.duplicateApplyRestriction = duplicateApplyRestriction;
        this.falseInfoWarning = falseInfoWarning;
        this.cancellationConditions = cancellationConditions;
        this.recruitmentCancelPossibility = recruitmentCancelPossibility;
        this.reserveCandidateGuide = reserveCandidateGuide;
        this.contactInfo = contactInfo;
        this.otherGuides = otherGuides;
    }
}
```

---

## 복구 시 체크리스트

1. `notice/entity/` 에 위 코드 파일 추가
2. `Notice.java` 에 연관관계 추가 (필요 시):
   ```java
   @OneToOne(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
   private NoticeCompanyInfo companyInfo;

   @OneToOne(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
   private NoticeGuideline guideline;
   ```
3. AI `/analyze-and-place` 응답 DTO에 해당 필드 추가
4. `NoticeService.saveNotice()` 에서 저장 로직 추가
5. DB 마이그레이션 스크립트 작성 (`notice_company_infos`, `notice_guidelines` 테이블)
