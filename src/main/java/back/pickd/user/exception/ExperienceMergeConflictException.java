package back.pickd.user.exception;

import back.pickd.user.dto.ExperienceMergeConflictResponse;
import lombok.Getter;

@Getter
public class ExperienceMergeConflictException extends RuntimeException {
    private final ExperienceMergeConflictResponse response;

    public ExperienceMergeConflictException(ExperienceMergeConflictResponse response) {
        super("유사한 기존 경험이 있어 병합 확인이 필요합니다.");
        this.response = response;
    }
}
