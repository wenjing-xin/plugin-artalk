package xin.wenjing.halo.artalk;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.PropertyPlaceholderHelper;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
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

    static final PropertyPlaceholderHelper PROPERTY_PLACEHOLDER_HELPER = new PropertyPlaceholderHelper("${", "}");

    private final SettingConfigGetter settingConfigGetter;

    @Override
    public void render(ITemplateContext context, IProcessableElementTag elementTag, IElementTagStructureHandler iElementTagStructureHandler) {

        var siteConfig = settingConfigGetter.getBasicConfig().blockOptional().orElseThrow();

        String siteTitle = String.valueOf(siteConfig.getSiteTitle());
        String artalkUrl = String.valueOf(siteConfig.getArtalkUrl());
        String privacyUrl = String.valueOf(siteConfig.getPrivacyUrl());
        String darkModeAttribute = String.valueOf(siteConfig.getSetDarkModeAttribute());

        final var ldArtalkTmpl = ldTemplateResolve(siteTitle, artalkUrl, privacyUrl, darkModeAttribute);
        final var normalArtalkTmpl = normalTemplateResolve(siteTitle, artalkUrl, privacyUrl);

        if(siteTitle != null && artalkUrl != null) {
            if(siteConfig.isEnableLightDark()){
                iElementTagStructureHandler.replaceWith(ldArtalkTmpl, false);
            }else{
                iElementTagStructureHandler.replaceWith(normalArtalkTmpl, false);
            }
        }
    }

    private String ldTemplateResolve(String siteTitle, String artalkUrl, String privacyUrl, String darkModeAttribute){
        final Properties properties = new Properties();
        properties.setProperty("siteTitle", siteTitle);
        properties.setProperty("artalkUrl", artalkUrl);
        properties.setProperty("privacy", privacyUrl);
        if(darkModeAttribute.equals("auto")){
            properties.setProperty("dataTheme", "prefers-color-scheme");
            properties.setProperty("dataThemeName", "dark");
        }else{
            properties.setProperty("dataTheme", darkModeAttribute.split("=")[0]);
            properties.setProperty("dataThemeName", darkModeAttribute.split("=")[1]);
        }


        // 同时兼容使用pjax的主题和未使用pjax的主题
        final var artalkTmpl = """
                <div id="post-comment">
                    <div class="comment-head">
                        <div class="comment-headline">
                           <button onclick="setLight()" class="switchColor" id="artalk-light-btn">Light</button>
                           <button onclick="setDark()" class="switchColor" id="artalk-dark-btn">Dark</button>
                        </div>
                        <div class="comment-randomInfo">
                            <a href="${privacy}" style="font-size:15px;">隐私政策</a>
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
                            window.artalkItem.on("created", ()=>{
                                console.log("监听事件出发--外层")
                                setDarkMode();
                            })
                        }
                    }
                    document.addEventListener("DOMContentLoaded",()=>{
                        initArtalkComment();
                    });
                    
                    function setDarkMode() {
                        if (typeof window.artalkItem !== 'object') return;
                        let isDark = document.documentElement.getAttribute('${dataTheme}') == '${dataThemeName}'
                        window.artalkItem.setDarkMode(isDark)
                        console.log("监听事件出发", document.documentElement.getAttribute('${dataTheme}'))
                    }
                    
                    function setDark(){
                        if (typeof window.artalkItem !== 'object') return
                        window.artalkItem.setDarkMode(true)
                        document.getElementById("artalk-dark-btn").classList.add("active-artalk-btn");
                        document.getElementById("artalk-light-btn").classList.remove("active-artalk-btn");
                    }
                    
                    function setLight(){
                        if (typeof window.artalkItem !== 'object') return;
                        window.artalkItem.setDarkMode(false)
                        document.getElementById("artalk-light-btn").classList.add("active-artalk-btn");
                        document.getElementById("artalk-dark-btn").classList.remove("active-artalk-btn");
                    }
                    
                    document.addEventListener("pjax:complete",()=>{
                        initArtalkComment();
                    })
                </script>
            """;
        return PROPERTY_PLACEHOLDER_HELPER.replacePlaceholders(artalkTmpl, properties);
    }

    private String normalTemplateResolve(String siteTitle, String artalkUrl, String privacyUrl){

        final Properties properties = new Properties();
        properties.setProperty("siteTitle", siteTitle);
        properties.setProperty("artalkUrl", artalkUrl);
        properties.setProperty("privacy", privacyUrl);

        // 同时兼容使用pjax的主题和未使用pjax的主题
        final var artalkTmpl = """
                <div id="post-comment">
                    <div id="artalk-comment"></div>
                </div>
                <script type="text/javascript" data-pjax>
                    function initArtalk(){
                        if(document.querySelectorAll("#artalk-comment").length){
                            Artalk.init({
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
}
