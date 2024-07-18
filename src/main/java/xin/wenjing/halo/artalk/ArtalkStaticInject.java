package xin.wenjing.halo.artalk;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.PluginContext;
import run.halo.app.plugin.ReactiveSettingFetcher;
import run.halo.app.theme.dialect.TemplateHeadProcessor;
import xin.wenjing.halo.entity.Settings;

/**
 * 功能描述
 * 注入一些css和js文件用于适配明暗主题以及引入数学公式插件
 * @author: dreamChaser
 * @date: 2024年06月03日 18:02
 */
@Component
@RequiredArgsConstructor
public class ArtalkStaticInject implements TemplateHeadProcessor {

    private final ReactiveSettingFetcher settingFetcher;

    private final PluginContext pluginContext;

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model, IElementModelStructureHandler structureHandler) {
        return settingFetcher.fetch(Settings.GROUP, Settings.class)
            .doOnNext( baseConf ->{
                String injectContent = "";

                // 开启明暗模式(独立切换)后且有自定义加入自定义css
                if(baseConf.isEnableCustomCss()){
                    injectContent = customCssResolve(baseConf.getCustomCss());
                }
                // 明暗模式关闭且没有自定义Css的时候，注入默认样式
                if(baseConf.getEnableLightDark().equals("disable") && !baseConf.isEnableCustomCss()){
                    injectContent = normalStatic();
                }
                String jsUrl = baseConf.getArtalkUrl() + "/dist/Artalk.js";
                String cssUrl = baseConf.getArtalkUrl() + "/dist/Artalk.css";
                String pubInjectContent = pubScriptInject(baseConf.isEnableLatex(), cssUrl, jsUrl);
                final IModelFactory modelFactory = context.getModelFactory();
                model.add(modelFactory.createText(injectContent + pubInjectContent));
            }).then();
    }

    /**
     * 判断是否开启数学公式支持
     * @param enableLatex
     * @return
     */
    private String pubScriptInject(boolean enableLatex, String cssUrl, String jsUrl){
        String version = pluginContext.getVersion();
        if (enableLatex) {
            return
                """
                    <link rel="stylesheet" href="https://unpkg.com/katex@0.16.7/dist/katex.min.css" />
                    <link rel="stylesheet" href="/plugins/plugin-artalk/assets/static/artalkBeautify.css?version=%s" />
                    <script data-pjax src="/plugins/plugin-artalk/assets/static/katex.min.js"></script>
                    <link rel="stylesheet" href="%s" />
                    <script data-pjax src="%s"></script>
                    <script defer src="/plugins/plugin-artalk/assets/static/artalk-plugin-katex.js"></script>
                """.formatted(version, cssUrl, jsUrl);
        } else {
            return
                """
                   <link rel="stylesheet" href="/plugins/plugin-artalk/assets/static/artalkBeautify.css?version=%s" />
                   <link rel="stylesheet" href="%s">
                   <script data-pjax src="%s"></script>
                """.formatted(version, cssUrl, jsUrl);
        }
    }

    private static String customCssResolve(String cssContent){
        return """
            <style>
                %s
            </style>
            """.formatted(cssContent);
    }

    /**
     * 无明暗模式下的中性样式
     * @return
     */
    private String normalStatic() {
        String version = pluginContext.getVersion();
        return
            """
                <link rel="stylesheet" href="/plugins/plugin-artalk/assets/static/normalStatic.css?version=%s" />
            """.formatted(version);
    }

}
