package xin.wenjing.halo.finder.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import run.halo.app.theme.finders.Finder;
import xin.wenjing.halo.finder.CommentDataList;
import xin.wenjing.halo.service.ArtalkService;

/**
 * 功能描述: 为主题提供最新评论 finder 接口
 * @author: dreamChaser
 * @date: 2024年06月05日 14:38
 */
@RequiredArgsConstructor
@Finder("artalkFinder")
public class CommentDataListImpl implements CommentDataList {

    private final ArtalkService artalkService;

    @Override
    public Mono<JsonNode> listAllComment() {
        return artalkService.getAllComment();
    }

    @Override
    public Mono<JsonNode> getPageComment(String pageKey) {
        return artalkService.getSpecialPageComment(pageKey);
    }
}
