# plugin-artalk
为 halo2.0 集成artalk评论区

## 一、插件使用

### 1、注意事项

#### 1.1 使用前注意事项

<font color="#499EFF">使用该插件前确保关闭其他评论插件！！！</font>

>因为该插件是通过扩展官方提供的 `CommentWidget` 接口进行实现的，理论上是不能同时存在多个，请知悉！

#### 1.2 artalk的明暗适配说明

halo的主题繁多，由于作者精力有限，无法对所有的主题明暗模式进行适配，在开发本插件的时候已经将明暗模式设置为自动模式，并且提供了一套默认的中性配色，保证明暗模式下都可以醒目的展示内容，如果您对当前的配色方案不满意，可以自定义 css 样式。后续会慢慢提供一些常用主题的明暗模式配色方案。

#### 1.3 halo-theme-hao 主题使用说明
由于 halo-theme-hao 主题已经加入了 artalk 评论，同时由于评论区引入的逻辑原因，无法直接使用本插件。如果想使用该插件，请修改 halo-theme-hao 主题的评论引入逻辑。后续作者会完善该教程！

### 2、插件功能
- 集成 artalk 评论区至 halo 系统中
- 支持数学公式
- 适配含有 pjax 的主题
- 可自定义 css 样式

### 3、配置说明
<img src="https://github.com/wenjing-xin/plugin-artalk/assets/130843859/0a7c9784-bb80-4904-82c5-8e0944223bfc" width=420px />

根据配置的提示填写即可，注意在线资源是 artalk 官方提供的一些公共 CDN 资源，要确保自己的 artalk 版本是否和其一致，否则请引入自己搭建的 artalk 服务资源。

## 二、其他

### 1、Todo

- [ ] 提供一些常见主题的明暗配色模式
- [ ] 提供对[链接安全跳转中台插件](https://github.com/wenjing-xin/plugins-links-security-detect) 的兼容，使得评论区的链接也可以通过中台进行跳转
- [ ] 评论系统切换（hexo博客支持双评论系统切换，后续视情况而定）

### 2、赞助我
如果你感觉这个插件还不错，请我喝杯咖啡☕️☕️☕️
<div>
&emsp;&emsp;<img src="https://github.com/wenjing-xin/plugin-artalk/assets/130843859/8787ff4d-eee4-4004-8bc9-b25ef266d154" width=150px />
&emsp;&emsp;<img src="https://github.com/wenjing-xin/plugin-artalk/assets/130843859/cfd7bcf2-0689-4df4-83e1-2413cf32e4c6" width=150px />
</div>

### 3、问题反馈
现在群内提问，若问题没有得到解决，则在 GitHub提交 [isssue](https://github.com/wenjing-xin/plugin-artalk/issues)

QQ交流群与QQ频道，加群后管理员自动审核
<div>
&emsp;&emsp;<img src="https://github.com/wenjing-xin/plugin-artalk/assets/130843859/033dc281-500f-4158-b43c-f36729ab4a74" width=150px />
&emsp;&emsp;<img src="https://github.com/wenjing-xin/plugin-artalk/assets/130843859/688d7eb4-530f-433d-a9b0-c8124dc99a3a" width=158px />
</div>

### 4、开发环境

插件开发的详细文档请查阅：<https://docs.halo.run/developer-guide/plugin/introduction>


