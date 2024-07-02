package xin.wenjing.halo.artalk;

import lombok.RequiredArgsConstructor;
import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
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

    private final PluginWrapper pluginWrapper;

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model, IElementModelStructureHandler structureHandler) {
        return settingFetcher.fetch(Settings.GROUP, Settings.class)
            .doOnNext( baseConf ->{
                String injectContent = "";

                // 开启明暗模式后自定义css加入
                if(baseConf.isEnableCustomCss() && baseConf.isEnableLightDark()){
                    injectContent = customCssResolve(baseConf.getCustomCss());
                }
                // 明暗模式关闭且没有自定义Css的时候，注入默认样式
                if(!baseConf.isEnableLightDark() && !baseConf.isEnableCustomCss()){
                    injectContent = normalStatic();
                }

                String pubInjectContent = pubScriptInject(baseConf.isEnableLatex(), baseConf.getCssUrl(), baseConf.getJsUrl());
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
        if(jsUrl != null && cssUrl !=null) {

            String version = pluginWrapper.getDescriptor().getVersion();
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
        }else{
            return null;
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

        return
            """
                <style>
                   .atk-main-editor{
                        border: none !important;
                   }
                   .atk-grp-switcher span{
                       padding: 0 .4rem !important;
                       text-align: center;
                   }
                   .atk-main-editor {
                       background: #DFE3EB !important;
                   }
                   .atk-main-editor>.atk-textarea-wrap {
                       background: #DFE3EB !important;
                   }
                   .atk-main-editor>.atk-textarea-wrap .atk-textarea{
                       background: #DFE3EB !important;
                       color: #606266 !important;
                   }
                   .atk-editor-plug-emoticons>.atk-grp-switcher{
                        background: #DFE3EB !important;
                   }
                   .artalk>.atk-list>.atk-list-header .atk-comment-count{
                        color: #909399 !important;
                   }
                   .atk-comment>.atk-main>.atk-body>.atk-content{
                        color: #606266;
                   }
                   .atk-comment>.atk-main>.atk-footer .atk-actions>span {
                        color: #909399 !important;
                   }
                   .atk-comment>.atk-main>.atk-header .atk-item{
                        color: #606266 !important;
                   }
                   .atk-main-editor>.atk-plug-panel-wrap{
                        border: none !important;
                   }
                   .atk-editor-plug-emoticons>.atk-grp-switcher{
                         border: none !important;
                   }
                   .atk-editor-plug-emoticons>.atk-grp-wrap>.atk-grp{
                        color: #606266 !important;
                   }
                   .atk-main-editor>.atk-header{
                        color: #606266 !important;
                   }
                   .artalk>.atk-list>.atk-list-footer .atk-copyright{
                        color: #606266 !important;
                   }
                   .atk-editor-plug-preview{
                        color: #606266;
                   }
                   .artalk>.atk-list>.atk-list-header .atk-right-action>span{
                        color: #F56C6C !important;
                   }
                </style>
            """;
    }

}
