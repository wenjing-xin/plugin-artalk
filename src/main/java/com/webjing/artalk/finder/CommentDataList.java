package xin.wenjing.halo.finder;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;

/**
* 功能描述: 为主题提供最新评论 finder 接口
* @author: dreamChaser
* @date: 2024/6/5 14:39
*/
public interface CommentDataList {

    /**
     * 获取全部/最新评论
     * @return
     */
    Mono<JsonNode> listAllComment();

    /**
     * 获取指定页面的评论数据
     * hao 主题有页脚弹幕 因此提供此接口
     * 此外 返回页面评论总条数 为文章的评论统计提供数据
     * @param pageKey
     * @return
     */
    Mono<JsonNode> getPageComment(String pageKey);


}
