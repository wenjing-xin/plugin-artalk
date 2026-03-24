package xin.wenjing.halo.artalk;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.PropertyPlaceholderHelper;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IAttribute;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import run.halo.app.plugin.PluginContext;
import run.halo.app.theme.dialect.CommentWidget;
import xin.wenjing.halo.service.SettingConfigGetter;
import java.util.Properties;

/**
 * 通过评论扩展点加入 artalk 评论
 * @author: dreamChaser
 * @date: 2024年06月03日 16:29
 */
@RequiredArgsConstructor
@Component
public class ArtalkComment implements CommentWidget {

    private static final String TEMPLATE_ID_VARIABLE = "_templateId";

    private final PluginContext pluginContext;

    static final PropertyPlaceholderHelper PROPERTY_PLACEHOLDER_HELPER = new PropertyPlaceholderHelper("${", "}");

    private final SettingConfigGetter settingConfigGetter;

    @Override
    public void render(ITemplateContext context, IProcessableElementTag elementTag, IElementTagStructureHandler iElementTagStructureHandler) {

        var siteConfig = settingConfigGetter.getBasicConfig().blockOptional().orElseThrow();
        var advanceConfig = settingConfigGetter.getAdvanceConfig().blockOptional().orElseThrow();
        String commentDomId = multiCommentDomId(elementTag, context, advanceConfig.isCommentPathKey());
        String siteTitle = String.valueOf(siteConfig.getSiteTitle());
        String artalkUrl = String.valueOf(siteConfig.getArtalkUrl());

        String linkJump = "";
        if(Boolean.valueOf(advanceConfig.isAdaptLsdPlugin())){
            linkJump = adaptLinkJump();
        }

        if(siteTitle != null && artalkUrl != null) {
            if(siteConfig.getEnableLightDark().equals("attribute") || siteConfig.getEnableLightDark().equals("elementClassName")){
                // 明暗属性选择器切换
                String darkModeAttribute = String.valueOf(siteConfig.getDarkModeAttribute());
                String finalTmpl = "";
                if(siteConfig.getEnableLightDark().equals("elementClassName")){
                    finalTmpl = normalTemplateResolve(siteTitle, artalkUrl, darkModeAttribute, "className", commentDomId, context);
                }else{
                    finalTmpl = normalTemplateResolve(siteTitle, artalkUrl, darkModeAttribute, "attribute", commentDomId, context);
                }
                iElementTagStructureHandler.replaceWith(finalTmpl + linkJump, false);
            }else if(siteConfig.getEnableLightDark().equals("single")){
                // 独立明暗模式切换
                final var ldArtalkTmpl = ldTemplateResolve(siteTitle, artalkUrl, commentDomId, context);
                iElementTagStructureHandler.replaceWith(ldArtalkTmpl + linkJump, false);
            }else{
                iElementTagStructureHandler.replaceWith(moderateTemplateResolve(siteTitle, artalkUrl, commentDomId , context) + linkJump, false);
            }
        }
    }

    /**
     * 独立暗黑模式切换
     * @param siteTitle
     * @param artalkUrl
     * @return
     */
    private String ldTemplateResolve(String siteTitle, String artalkUrl, String pathKeyDom, ITemplateContext context){

        final Properties properties = new Properties();
        properties.setProperty("siteTitle", siteTitle);
        properties.setProperty("artalkUrl", artalkUrl);

        if(pathKeyDom != null){
            String curTemplateId = getTemplateId(context);
            properties.setProperty("mountedDomId", pathKeyDom);
            properties.setProperty("pageKey", "/" + curTemplateId + "/" + pathKeyDom);
            properties.setProperty("pageKeyType", "normalStr");
        }else{
            properties.setProperty("mountedDomId", "artalk-comment");
            properties.setProperty("pageKeyType", "functionStr");
        }

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
                    <div id="${mountedDomId}"></div>
                </div>
                <script type="text/javascript" data-pjax defer>
                    function initArtalkComment(){
                        let pageKeyResolve = "";
                        if("${pageKeyType}" == "functionStr"){
                            pageKeyResolve = window.location.pathname.replace(/\\/page\\/\\d$/, "");
                        }else if("${pageKeyType}" == "normalStr"){
                            pageKeyResolve = "${pageKey}";
                        }
                        window.artalkItem = Artalk.init({
                            el: '#${mountedDomId}',
                            pageKey: pageKeyResolve,
                            pageTitle: "",
                            server: "${artalkUrl}",
                            site:"${siteTitle}",
                            countEl: '#ArtalkCount',
                            dark: "auto"
                        })
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

    private String normalTemplateResolve(String siteTitle, String artalkUrl, String darkModeAttribute, String switchType, String commentDomId, ITemplateContext context){

        final Properties properties = new Properties();
        properties.setProperty("siteTitle", siteTitle);
        properties.setProperty("artalkUrl", artalkUrl);
        properties.setProperty("dataTheme", darkModeAttribute.split("=")[0]);
        properties.setProperty("dataThemeName", darkModeAttribute.split("=")[1]);
        properties.setProperty("switchType", switchType);

        if(commentDomId != null){
            String curTemplateId = getTemplateId(context);
            properties.setProperty("mountedDomId", commentDomId);
            properties.setProperty("pageKey", "/" + curTemplateId + "/" + commentDomId);
            properties.setProperty("pageKeyType", "normalStr");
        }else{
            properties.setProperty("mountedDomId", "artalk-comment");
            properties.setProperty("pageKeyType", "functionStr");
        }

        // 同时兼容使用pjax的主题和未使用pjax的主题
        final var artalkTmpl = """
                <div id="post-comment">
                    <div id="${mountedDomId}"></div>
                </div>
                <script type="text/javascript" defer>
                    function initArtalk(){
                        let pageKeyResolve = "";
                        if("${pageKeyType}" == "functionStr"){
                            pageKeyResolve = window.location.pathname.replace(/\\/page\\/\\d$/, "");
                        }else if("${pageKeyType}" == "normalStr"){
                            pageKeyResolve = "${pageKey}";
                        }
                        window.artalkItem = Artalk.init({
                            el: '#${mountedDomId}',
                            pageKey: pageKeyResolve,
                            pageTitle: "",
                            server: "${artalkUrl}",
                            site: "${siteTitle}",
                            countEl: '#ArtalkCount',
                            darkMode: 'auto'
                        });
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
                        });
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

    private String moderateTemplateResolve(String siteTitle, String artalkUrl, String commentPathKey, ITemplateContext context){

        final Properties properties = new Properties();
        properties.setProperty("siteTitle", siteTitle);
        properties.setProperty("artalkUrl", artalkUrl);

        if(commentPathKey != null){
            String curTemplateId = getTemplateId(context);
            properties.setProperty("mountedDomId", commentPathKey);
            properties.setProperty("pageKey", "/" + curTemplateId + "/" + commentPathKey);
            properties.setProperty("pageKeyType", "normalStr");
        }else{
            properties.setProperty("mountedDomId", "artalk-comment");
            properties.setProperty("pageKeyType", "functionStr");
        }
        // 同时兼容使用pjax的主题和未使用pjax的主题
        final var artalkTmpl = """
                <div id="post-comment">
                    <div id="${mountedDomId}"></div>
                </div>
                <script type="text/javascript" data-pjax>
                    function initArtalk(){
                        let pageKeyResolve = "";
                        if("${pageKeyType}" == "functionStr"){
                            pageKeyResolve = window.location.pathname.replace(/\\/page\\/\\d$/, "");
                        }else if("${pageKeyType}" == "normalStr"){
                            pageKeyResolve = "${pageKey}";
                        }
                        window.artalkItem = Artalk.init({
                            el: '#${mountedDomId}',
                            pageKey: pageKeyResolve,
                            pageTitle: "",
                            server: "${artalkUrl}",
                            site:"${siteTitle}",
                            countEl: '#ArtalkCount',
                            darkMode: 'auto'
                        });
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

    /**
     * 评论唯一标识生成
     * @param tag
     * @return
     */
    private String multiCommentDomId(IProcessableElementTag tag, ITemplateContext context, boolean isCommentPathKey){

        if(!isCommentPathKey){
            return null;
        }
        IAttribute groupAttribute = tag.getAttribute("group");
        IAttribute kindAttribute = tag.getAttribute("kind");
        IAttribute nameAttribute = tag.getAttribute("name");

        String group = groupAttribute.getValue() == null ? "" : StringUtils.defaultString(groupAttribute.getValue());
        String name = kindAttribute.getValue();
        String kind = nameAttribute.getValue();
        Assert.notNull(name, "The name must not be null.");
        Assert.notNull(kind, "The kind must not be null.");
        String groupKindNameAsDomId = String.join("-", group, kind, name);
        String commentId = "artalk-" + groupKindNameAsDomId.replaceAll("[^\\-_a-zA-Z0-9\\s]", "-")
            .replaceAll("(-)+", "-");
        return commentId;
    }

    /**
     * 返回模版名称
     * @param context
     * @return
     */
    public static String getTemplateId(ITemplateContext context) {
        try {
            String  templateName = context.getVariable(TEMPLATE_ID_VARIABLE).toString();
            return templateName != null && templateName.length() > 0 ? templateName : "";
        }catch (Exception e){
            return "";
        }
    }

}
