package back.pickd.global.infra.s3;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileUploadType {
    PROFILE("user/profile"),
    LICENSE("experience/license"),
    EDUCATION("experience/education"),
    LANGUAGE("experience/language"),
    AWARD("experience/award"),
    TEMP_RESUME("temp/resume"),
    GENERAL("experience/general");

    private final String directoryPath;

    /**
     * 유저 ID와 고유 파일명을 조합하여 S3 Key(경로)를 생성합니다.
     * 예: experience/license/{userId}/{uuid}_{filename}
     */
    public String getFullPath(Long userId, String uniqueFileName) {
        return String.format("%s/%d/%s", this.directoryPath, userId, uniqueFileName);
    }
}
