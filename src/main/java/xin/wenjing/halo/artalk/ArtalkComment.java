package xin.wenjing.halo.artalk;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.PropertyPlaceholderHelper;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import run.halo.app.plugin.SettingFetcher;
import run.halo.app.theme.dialect.CommentWidget;
import xin.wenjing.halo.entity.Settings;
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

    private final SettingFetcher settingFetcher;

    static final PropertyPlaceholderHelper PROPERTY_PLACEHOLDER_HELPER = new PropertyPlaceholderHelper("${", "}");

    @Override
    public void render(ITemplateContext context, IProcessableElementTag elementTag, IElementTagStructureHandler iElementTagStructureHandler) {

        Settings siteConfig = settingFetcher.fetch(Settings.GROUP, Settings.class).orElse(new Settings());
        String siteTitle = siteConfig.getSiteTitle();
        String artalkUrl = siteConfig.getArtalkUrl();
        String privacyUrl = siteConfig.getPrivacyUrl();

        final var ldArtalkTmpl = ldTemplateResolve(siteTitle, artalkUrl, privacyUrl);
        final var normalArtalkTmpl = normalTemplateResolve(siteTitle, artalkUrl, privacyUrl);

        if(siteTitle != null && artalkUrl != null) {
            if(siteConfig.isEnableLightDark()){
                iElementTagStructureHandler.replaceWith(ldArtalkTmpl, false);
            }else{
                iElementTagStructureHandler.replaceWith(normalArtalkTmpl, false);
            }
        }
    }

    private String ldTemplateResolve(String siteTitle, String artalkUrl, String privacyUrl){
        final Properties properties = new Properties();
        properties.setProperty("siteTitle", siteTitle);
        properties.setProperty("artalkUrl", artalkUrl);
        properties.setProperty("privacy", privacyUrl);

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
                <script type="text/javascript" data-pjax>
                    document.addEventListener("DOMContentLoaded",()=>{
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
                    });
                    function setDark(){
                        Artalk.init({
                            el: '#artalk-comment',
                            pageKey: window.location.pathname.replace(/\\/page\\/\\d$/, ""),
                            pageTitle: "",
                            server: "${artalkUrl}",
                            site:"${siteTitle}",
                            countEl: '#ArtalkCount'
                        }).setDarkMode(true)
                        document.getElementById("artalk-dark-btn").classList.add("active-artalk-btn");
                        document.getElementById("artalk-light-btn").classList.remove("active-artalk-btn");
                    }
                    function setLight(){
                        Artalk.init({
                            el: '#artalk-comment',
                            pageKey: window.location.pathname.replace(/\\/page\\/\\d$/, ""),
                            pageTitle: "",
                            server: "${artalkUrl}",
                            site:"${siteTitle}",
                            countEl: '#ArtalkCount'
                        }).setDarkMode(false)
                        document.getElementById("artalk-light-btn").classList.add("active-artalk-btn");
                        document.getElementById("artalk-dark-btn").classList.remove("active-artalk-btn");
                    }
                    document.addEventListener("pjax:complete",()=>{
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
                    document.addEventListener("DOMContentLoaded",()=>{
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
                    });
                    document.addEventListener("pjax:complete",()=>{
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
                    })
                </script>
            """;
        return PROPERTY_PLACEHOLDER_HELPER.replacePlaceholders(artalkTmpl, properties);
    }
}
