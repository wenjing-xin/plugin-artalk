package xin.wenjing.halo.artalk;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import run.halo.app.plugin.SettingFetcher;
import run.halo.app.theme.dialect.CommentWidget;

/**
 * 功能描述
 * 通过评论扩展点加入 artalk 评论
 * @author: dreamChaser
 * @date: 2024年06月03日 16:29
 */
@AllArgsConstructor
@Component
public class ArtalkComment implements CommentWidget {

    private final SettingFetcher settingFetcher;

    @Override
    public void render(ITemplateContext context, IProcessableElementTag elementTag, IElementTagStructureHandler iElementTagStructureHandler) {
        SiteConfig siteConfig = settingFetcher.fetch(SiteConfig.GROUP, SiteConfig.class).orElse(new SiteConfig());
        String siteTitle = siteConfig.getSiteTitle();
        String artalkUrl = siteConfig.getArtalkUrl();
        final var artalkTmpl = templateResolve();
        if(siteTitle != null && artalkUrl != null) {
            iElementTagStructureHandler.replaceWith(
                artalkTmpl.formatted(artalkUrl, siteTitle, artalkUrl, siteTitle), false);
        }
    }

    private String templateResolve(){
        // 同时兼容使用pjax的主题和未使用pjax的主题
        final var artalkTmpl = """
            <div id="artalk-comment"></div>
            <script type="text/javascript">
                if(document.querySelectorAll("#artalk-comment").length){
                    Artalk.init({
                        el: '#artalk-comment',
                        pageKey: window.location.pathname.replace(/\\/page\\/\\d$/, ""),
                        pageTitle: "",
                        server: "%s",
                        site:"%s",
                        countEl: '#ArtalkCount',
                        darkMode: 'auto'
                    });
                }
                document.addEventListener("pjax:complete",()=>{
                    if(document.querySelectorAll("#artalk-comment").length){
                        Artalk.init({
                            el: '#artalk-comment',
                            pageKey: window.location.pathname.replace(/\\/page\\/\\d$/, ""),
                            pageTitle: "",
                            server: "%s",
                            site:"%s",
                            countEl: '#ArtalkCount',
                            darkMode: 'auto'
                        })
                    }
                })
            </script>
        """;
        return artalkTmpl;
    }

    @Data
    private static class SiteConfig{
        public static final String GROUP = "baseConf";
        private String siteTitle;
        private String artalkUrl;
    }
}
