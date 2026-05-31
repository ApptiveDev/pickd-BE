package back.pickd.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExperienceMergeConflictResponse {
    private final boolean needsMerge;
    private final ExperienceMergeCandidateResponse candidate;
    private final Double similarity;
    private final ExperienceDraftResponse draft;
}
