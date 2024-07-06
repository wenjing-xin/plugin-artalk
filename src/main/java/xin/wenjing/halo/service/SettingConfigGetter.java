package xin.wenjing.halo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;
import xin.wenjing.halo.entity.Settings;

/**
 * 获取插件配置信息
 * @author: dreamChaser
 * @date: 2024年07月05日 20:55
 */
@Component
@RequiredArgsConstructor
public class SettingConfigGetter {

    private final ReactiveSettingFetcher settingFetcher;

    public Mono<Settings> getBasicConfig() {
        return settingFetcher.fetch(Settings.GROUP, Settings.class)
            .defaultIfEmpty(new Settings());
    }
}
