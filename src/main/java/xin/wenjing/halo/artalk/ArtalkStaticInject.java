package xin.wenjing.halo.artalk;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.SettingFetcher;
import run.halo.app.theme.dialect.TemplateHeadProcessor;

/**
 * 功能描述
 * 注入一些css和js文件用于适配明暗主题以及引入数学公式插件
 * @author: dreamChaser
 * @date: 2024年06月03日 18:02
 */
@Component
@AllArgsConstructor
public class ArtalkStaticInject implements TemplateHeadProcessor {

    private final SettingFetcher settingFetcher;

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model, IElementModelStructureHandler structureHandler) {
        BaseConfig baseConf = settingFetcher.fetch(BaseConfig.GROUP, BaseConfig.class).orElse(new BaseConfig());
        String injectContent = baseConf.isEnableCustomCss() ? customCssResolve(baseConf.getCustomCss()) : normalStatic();
        final IModelFactory modelFactory = context.getModelFactory();
        return Mono.just(modelFactory.createText(injectContent))
            .doOnNext(model::add)
            .then();
    }

    private static String customCssResolve(String cssContent){
        return """
            <style>
                %s
            </style>
            """.formatted(cssContent);
    }

    private static String normalStatic() {
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

    @Data
    private static class BaseConfig{
        public static final String GROUP = "baseConf";
        private boolean enableCustomCss;
        private String customCss;
    }

}
