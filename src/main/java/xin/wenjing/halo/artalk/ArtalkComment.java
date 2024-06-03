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

        BaseConf baseConf = settingFetcher.fetch(BaseConf.GROUP, BaseConf.class).orElse(new BaseConf());

        var settings = settingFetcher.get("baseConf");
        final var siteTitle = settings.get("siteTitle").asText();
        final var artalkUrl = settings.get("artalkUrl").asText();
        final var jsUrl = settings.get("jsUrl").asText();
        final var cssUrl = settings.get("cssUrl").asText();

        final var artalkTmpl = templateResolve(baseConf.isEnableLatex());

        iElementTagStructureHandler.replaceWith(artalkTmpl.formatted(cssUrl, jsUrl, artalkUrl, siteTitle), false);
    }

    /**
     * 是否引入 latex
     * @param enableLatex
     * @return
     */
    private static String templateResolve(boolean enableLatex){
        if(enableLatex) {
            final var artalkTmplLatex = """
                <div id="artalk-comment"></div>
                <link rel="stylesheet" href="https://unpkg.com/katex@0.16.7/dist/katex.min.css" />
                <script src="https://unpkg.com/katex@0.16.7/dist/katex.min.js"></script>
                <link rel="stylesheet" href="%s" />
                <script src="%s"></script>
                <script src="https://unpkg.com/@artalk/plugin-katex/dist/artalk-plugin-katex.js"></script>
                <script>
                    Artalk.init({
                        el: '#artalk-comment',
                        pageKey: window.location.pathname.replace(/\\/page\\/\\d$/, ""),
                        pageTitle: "",
                        server: "%s",
                        site:"%s",
                        countEl: '#ArtalkCount',
                        darkMode: 'auto'
                    })
                </script>
                """;
            return artalkTmplLatex;
        }else{
             final var artalkTmpl = """
                <div id="artalk-comment"></div>
                <link rel="stylesheet" href="%s">
                <script src="%s"></script>
                <script>
                    Artalk.init({
                        el: '#artalk-comment',
                        pageKey: window.location.pathname.replace(/\\/page\\/\\d$/, ""),
                        pageTitle: "",
                        server: "%s",
                        site:"%s",
                        countEl: '#ArtalkCount',
                        darkMode: 'auto'
                    })
                </script>
                """;
            return artalkTmpl;
        }
    }

    @Data
    private static class BaseConf{
        public static final String GROUP = "baseConf";
        private boolean enableLatex;
    }
}
