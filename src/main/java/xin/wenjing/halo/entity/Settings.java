package xin.wenjing.halo.entity;

import lombok.Data;

/**
 * 功能描述
 *
 * @author: dreamChaser
 * @date: 2024年06月05日 15:00
 */
@Data
public class Settings {

    public static final String GROUP = "baseConf";
    private String siteTitle;
    private String authDomain;
    private String artalkUrl;
    private String jsUrl;
    private String cssUrl;
    private boolean enableLatex;
    private boolean enableCustomCss;
    private String customCss;

}
