package xin.wenjing.halo.entity;

import lombok.Data;

/**
 * 高级配置数据
 * @author: dreamChaser
 * @date: 2024年07月21日 17:23
 */
@Data
public class AdvanceSettings {

    public static final String GROUP = "advanceSettings";

    private boolean adaptLsdPlugin;
    private boolean commentPathKey;
}
