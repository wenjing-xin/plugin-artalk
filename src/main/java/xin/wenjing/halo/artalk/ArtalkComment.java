package xin.wenjing.halo.artalk;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.PropertyPlaceholderHelper;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import run.halo.app.plugin.PluginContext;
import run.halo.app.theme.dialect.CommentWidget;
import xin.wenjing.halo.service.SettingConfigGetter;
import java.util.Properties;

/**
 * 功能描述
 * 通过评论扩展点加入 artalk 评论
 * @author: dreamChaser
 * @date: 2024年06月03日 16:29
 */
@RequiredArgsConstructor
@Component
public class ArtalkComment implements CommentWidget {

    private final PluginContext pluginContext;

    static final PropertyPlaceholderHelper PROPERTY_PLACEHOLDER_HELPER = new PropertyPlaceholderHelper("${", "}");

    private final SettingConfigGetter settingConfigGetter;

    @Override
    public void render(ITemplateContext context, IProcessableElementTag elementTag, IElementTagStructureHandler iElementTagStructureHandler) {

        var siteConfig = settingConfigGetter.getBasicConfig().blockOptional().orElseThrow();

        String siteTitle = String.valueOf(siteConfig.getSiteTitle());
        String artalkUrl = String.valueOf(siteConfig.getArtalkUrl());

        String linkJump = "";
        if(Boolean.valueOf(siteConfig.isAdaptLsdPlugin())){
            linkJump = adaptLinkJump();
        }

        if(siteTitle != null && artalkUrl != null) {
            if(siteConfig.getEnableLightDark().equals("attribute") || siteConfig.getEnableLightDark().equals("elementClassName")){
                // 明暗属性选择器切换
                String darkModeAttribute = String.valueOf(siteConfig.getDarkModeAttribute());
                String finalTmpl = "";
                if(siteConfig.getEnableLightDark().equals("elementClassName")){
                    finalTmpl = normalTemplateResolve(siteTitle, artalkUrl, darkModeAttribute, "className");
                }else{
                    finalTmpl = normalTemplateResolve(siteTitle, artalkUrl, darkModeAttribute, "attribute");
                }
                iElementTagStructureHandler.replaceWith(finalTmpl + linkJump, false);
            }else if(siteConfig.getEnableLightDark().equals("single")){
                // 独立明暗模式切换
                final var ldArtalkTmpl = ldTemplateResolve(siteTitle, artalkUrl);
                iElementTagStructureHandler.replaceWith(ldArtalkTmpl + linkJump, false);
            }else{
                iElementTagStructureHandler.replaceWith(moderateTemplateResolve(siteTitle, artalkUrl) + linkJump, false);
            }
        }
    }

    /**
     * 独立暗黑模式切换
     * @param siteTitle
     * @param artalkUrl
     * @return
     */
    private String ldTemplateResolve(String siteTitle, String artalkUrl){

        final Properties properties = new Properties();
        properties.setProperty("siteTitle", siteTitle);
        properties.setProperty("artalkUrl", artalkUrl);

        // 同时兼容使用pjax的主题和未使用pjax的主题
        final var artalkTmpl = """
                <div id="post-comment">
                    <div class="comment-head">
                        <div class="comment-headline">
                            <label class="artalk-switch">
                                <input type="checkbox" onchange="checkboxDarkMode(this)" id="toggle-artalk-theme">
                                <span class="slider"></span>
                            </label>
                        </div>
                    </div>
                    <div id="artalk-comment"></div>
                </div>
                <script type="text/javascript" data-pjax defer>
                    function initArtalkComment(){
                        if(document.querySelectorAll("#artalk-comment").length){
                            window.artalkItem = Artalk.init({
                                el: '#artalk-comment',
                                pageKey: window.location.pathname.replace(/\\/page\\/\\d$/, ""),
                                pageTitle: "",
                                server: "${artalkUrl}",
                                site:"${siteTitle}",
                                countEl: '#ArtalkCount',
                                dark: "auto"
                            })
                        }
                    }
                    function checkboxDarkMode(checkbox){
                        if(checkbox.checked){
                            if (typeof window.artalkItem !== 'object') return
                            window.artalkItem.setDarkMode(true)
                        }else{
                            if (typeof window.artalkItem !== 'object') return;
                            window.artalkItem.setDarkMode(false)
                        }
                    }
                    document.addEventListener("DOMContentLoaded",()=>{
                        initArtalkComment();
                    });
                    document.addEventListener("pjax:complete",()=>{
                        initArtalkComment();
                    });
                </script>
            """;
        return PROPERTY_PLACEHOLDER_HELPER.replacePlaceholders(artalkTmpl, properties);
    }

    private String normalTemplateResolve(String siteTitle, String artalkUrl, String darkModeAttribute, String switchType){

        final Properties properties = new Properties();
        properties.setProperty("siteTitle", siteTitle);
        properties.setProperty("artalkUrl", artalkUrl);
        properties.setProperty("dataTheme", darkModeAttribute.split("=")[0]);
        properties.setProperty("dataThemeName", darkModeAttribute.split("=")[1]);
        properties.setProperty("switchType", switchType);

        // 同时兼容使用pjax的主题和未使用pjax的主题
        final var artalkTmpl = """
                <div id="post-comment">
                    <div id="artalk-comment"></div>
                </div>
                <script type="text/javascript" data-pjax>
                    function initArtalk(){
                        if(document.querySelectorAll("#artalk-comment").length){
                            window.artalkItem = Artalk.init({
                                el: '#artalk-comment',
                                pageKey: window.location.pathname.replace(/\\/page\\/\\d$/, ""),
                                pageTitle: "",
                                server: "${artalkUrl}",
                                site:"${siteTitle}",
                                countEl: '#ArtalkCount',
                                darkMode: 'auto'
                            })
                            window.artalkItem.on("created", ()=>{
                                setDarkMode();
                                // 创建一个观察器实例并传入回调函数
                                const observer = new MutationObserver((mutationsList, observer) => {
                                    for (let mutation of mutationsList) {
                                        if (mutation.type === 'attributes' && mutation.attributeName === '${dataTheme}') {
                                            setDarkMode();
                                        }else{
                                            setDarkMode();
                                        }
                                    }
                                });
                                let targetNode = null;
                                if('${switchType}' == "className"){
                                    targetNode = document.querySelector('${dataTheme}');
                                }else{
                                    targetNode = document.querySelector('[${dataTheme}]');
                                }
                                
                                if(targetNode){
                                    const config = { attributes: true, childList: false, subtree: false };
                                    observer.observe(targetNode, config);
                                }
                            })
                        }
                    }
                    function setDarkMode() {
                        if (typeof window.artalkItem !== 'object') return;
                        if('${switchType}' == "className"){
                            let isDark = document.querySelector('${dataTheme}').classList.contains("${dataThemeName}");
                            window.artalkItem.setDarkMode(isDark);
                        }else{
                            let isDark = document.documentElement.getAttribute('${dataTheme}') == '${dataThemeName}'
                            window.artalkItem.setDarkMode(isDark);
                        }
                    }
                    document.addEventListener("DOMContentLoaded",()=>{
                        initArtalk();
                    });
                    document.addEventListener("pjax:complete",()=>{
                        initArtalk();
                    });
                </script>
            """;
        return PROPERTY_PLACEHOLDER_HELPER.replacePlaceholders(artalkTmpl, properties);
    }

    private String moderateTemplateResolve(String siteTitle, String artalkUrl){

        final Properties properties = new Properties();
        properties.setProperty("siteTitle", siteTitle);
        properties.setProperty("artalkUrl", artalkUrl);

        // 同时兼容使用pjax的主题和未使用pjax的主题
        final var artalkTmpl = """
                <div id="post-comment">
                    <div id="artalk-comment"></div>
                </div>
                <script type="text/javascript" data-pjax>
                    function initArtalk(){
                        if(document.querySelectorAll("#artalk-comment").length){
                            window.artalkItem = Artalk.init({
                                el: '#artalk-comment',
                                pageKey: window.location.pathname.replace(/\\/page\\/\\d$/, ""),
                                pageTitle: "",
                                server: "${artalkUrl}",
                                site:"${siteTitle}",
                                countEl: '#ArtalkCount',
                                darkMode: 'auto'
                            })
                        }
                    }
                    document.addEventListener("DOMContentLoaded",()=>{
                        initArtalk();
                    });
                    document.addEventListener("pjax:complete",()=>{
                        initArtalk();
                    })
                </script>
            """;
        return PROPERTY_PLACEHOLDER_HELPER.replacePlaceholders(artalkTmpl, properties);
    }

    public String adaptLinkJump(){
        String version = pluginContext.getVersion();
        //请求链接管理插件数据
        final Properties properties = new Properties();
        properties.setProperty("version", version);
        final var script = """
            <script data-pjax src="/plugins/plugin-artalk/assets/static/commentLinkScan.js?v=${version}"></script>
            <script type="text/javascript" data-pjax>
                document.addEventListener("DOMContentLoaded",()=>{
                     new commentLinkScan();
                });
                document.addEventListener("pjax:complete",()=>{
                    new commentLinkScan();
                })
            </script>
            """;
        return PROPERTY_PLACEHOLDER_HELPER.replacePlaceholders(script, properties);
    }

}
