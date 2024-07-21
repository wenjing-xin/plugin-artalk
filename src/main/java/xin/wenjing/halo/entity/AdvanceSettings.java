package xin.wenjing.halo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 高级配置数据
 * @author: dreamChaser
 * @date: 2024年07月21日 17:23
 */
@Data
public class AdvanceSettings {

    public static final String GROUP = "advanceSettings";

    private boolean adaptLsdPlugin;
    private boolean adaptMultiComment;
    private List<MultiCommentPages> multiCommentPage;

    @Data
    private static class MultiCommentPages{
        private String templateName;
    }
}
