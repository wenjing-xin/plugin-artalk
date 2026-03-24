package xin.wenjing.halo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ApiVersion;
import xin.wenjing.halo.service.ArtalkService;

/**
 * 功能描述
 * 提供评论区的 web 接口
 * @author: dreamChaser
 * @date: 2024年06月05日 20:04
 */
@ApiVersion("halo.wenjing.xin/v1alpha1")
@RequestMapping("/artalk")
@RestController
@Slf4j
@RequiredArgsConstructor
public class ArtalkController {

    private final ArtalkService artalkService;

    @GetMapping("/listAllComments")
    public Mono<JsonNode> listAllComments(){
        return artalkService.getAllComment();
    }

    @GetMapping("/pageKeyComments")
    public Mono<JsonNode> listPageKeyComments(@RequestParam(name = "pageKey") String pageKey){
        return artalkService.getSpecialPageComment(pageKey);
    }

}
