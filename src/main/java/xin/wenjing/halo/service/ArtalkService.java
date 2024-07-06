package xin.wenjing.halo.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import xin.wenjing.halo.entity.Settings;

/**
 * 功能描述
 * 从 artalk 服务端获取相关数据
 * @author: dreamChaser
 * @date: 2024年06月05日 14:56
 */
@Service
@Slf4j
public class ArtalkService {

    private WebClient webClient;

    private Settings pluginSetting;

    private static final String LATEST_COMMENT = "/api/v2/stats/latest_comments";

    private static final String PAGE_COMMENT = "/api/v2/comments";

    private final SettingConfigGetter settingConfigGetter;

    public ArtalkService(SettingConfigGetter settingConfigGetter) {
        this.settingConfigGetter = settingConfigGetter;
        this.pluginSetting = this.settingConfigGetter.getBasicConfig().blockOptional().orElseThrow();
        this.webClient = WebClient.builder().baseUrl(pluginSetting.getArtalkUrl())
            .defaultHeaders( headers ->{
                    headers.set("Origin", this.pluginSetting.getAuthDomain());
                    headers.setBasicAuth("Content-Type","application/x-www-form-urlencoded");
            }).build();
    }

    /**
     * 获取最近所有评论
     * @return
     */
    public Mono<JsonNode> getAllComment(){
        Mono<JsonNode> jsonNodeMono = webClient.get().uri(LATEST_COMMENT + "?site_name={siteName}&limit={size}",
                pluginSetting.getSiteTitle(), 100)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve() //获取响应体
            .onStatus(HttpStatusCode::is4xxClientError, response ->
                response.bodyToMono(JsonNode.class).flatMap(error -> Mono.empty()))
            .onStatus(HttpStatusCode::is5xxServerError, response ->
                response.bodyToMono(JsonNode.class).flatMap(error -> Mono.empty()))
            .bodyToMono(JsonNode.class);
        return jsonNodeMono;
    }

    /**
     * 获取指定页面的所有评论
     * @param pageKey 页面唯一标识
     * @return
     */
    public Mono<JsonNode> getSpecialPageComment(String pageKey){
        Mono<JsonNode> jsonNodeMono = webClient.get().uri(PAGE_COMMENT +
                    "?site_name={siteName}&page_key={page}&limit={size}",
                pluginSetting.getSiteTitle(), pageKey, 100)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve() //获取响应体
            .onStatus(HttpStatusCode::is4xxClientError, response ->
                response.bodyToMono(JsonNode.class).flatMap(error -> Mono.empty()))
            .onStatus(HttpStatusCode::is5xxServerError, response ->
                response.bodyToMono(JsonNode.class).flatMap(error -> Mono.empty()))
            .onStatus(HttpStatusCode::is5xxServerError, response ->
                    response.bodyToMono(JsonNode.class).flatMap(error -> Mono.empty()))
            .bodyToMono(JsonNode.class);
        return jsonNodeMono;
    }

}
