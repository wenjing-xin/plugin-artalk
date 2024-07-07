package xin.wenjing.halo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.theme.TemplateNameResolver;
import java.util.HashMap;
import java.util.List;

/**
 * 功能描述
 * 提供所有评论展示的路由
 * @author: dreamChaser
 * @date: 2024年06月05日 14:54
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class ArtalkRouter {


    private final TemplateNameResolver templateNameResolver;

    @Bean
    RouterFunction<ServerResponse> artalkRouterFunction() {
        return RouterFunctions.route().GET("/new_comment",this::renderCommentPage).build();
    }

    Mono<ServerResponse> renderCommentPage(ServerRequest request) {
        var model = new HashMap<String, Object>();
        model.put("new_comment", List.of());
        return templateNameResolver.resolveTemplateNameOrDefault(request.exchange(), "new_comment")
            .flatMap(templateName -> ServerResponse.ok().render(templateName, model));
    }
}
