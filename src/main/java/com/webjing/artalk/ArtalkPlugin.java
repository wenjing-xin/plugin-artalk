package xin.wenjing.halo;

import org.springframework.stereotype.Component;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

/**
* 功能描述
* 插件启动入口
* @author: dreamChaser
* @date: 2024/7/3 12:54
*/
@Component
public class ArtalkPlugin extends BasePlugin {

    public ArtalkPlugin(PluginContext pluginContext) {
        super(pluginContext);
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }
}
