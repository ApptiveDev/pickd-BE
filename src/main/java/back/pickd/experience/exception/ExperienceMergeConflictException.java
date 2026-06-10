package back.pickd.experience.exception;

import back.pickd.experience.dto.ExperienceMergeDto.Conflict;
import lombok.Getter;

@Getter
public class ExperienceMergeConflictException extends RuntimeException {
    private final Conflict response;

    public ExperienceMergeConflictException(Conflict response) {
        super("유사한 기존 경험이 있어 병합 확인이 필요합니다.");
        this.response = response;
    }
}
