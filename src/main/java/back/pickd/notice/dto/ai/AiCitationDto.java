package back.pickd.notice.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiCitationDto {
    private String field;
    private Integer page;
    private String content;

    @JsonProperty("source_url")
    private String sourceUrl;

    private List<Float> bbox;

    @JsonProperty("element_id")
    private Integer elementId;

    @JsonProperty("page_width")
    private Float pageWidth;

    @JsonProperty("page_height")
    private Float pageHeight;

    @Builder
    public AiCitationDto(String field, Integer page, String content, String sourceUrl, List<Float> bbox,
                         Integer elementId, Float pageWidth, Float pageHeight) {
        this.field = field;
        this.page = page;
        this.content = content;
        this.sourceUrl = sourceUrl;
        this.bbox = bbox;
        this.elementId = elementId;
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
    }
}
