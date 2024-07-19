# plugin-artalk
为 halo2.0 集成 artalk 评论区

## 一、插件使用

### 1、注意事项

#### 1.1 使用前注意事项
Halo-V2.17.0及其以上版本使用该插件的时候，请勾选后台中的扩展配置的评论组件部分配置，如下图所示：

![](https://dogecloud.wenjing.xin/image/artalk-plugin-extension.png)

如果勾选了此选项不生效，请关闭其他评论组件！

**Halo-V2.7.0一下版本使用该插件前确保关闭其他评论插件！！！**

>因为该插件是通过扩展官方提供的 `CommentWidget` 接口进行实现的，理论上是不能同时存在多个，请知悉！

#### 1.2 artalk的明暗适配说明

> V1.3.0 版本之前是无法对所有的主题明暗模式进行适配，在开发本插件的时候已经将明暗模式设置为自动模式，并且提供了一套默认的中性配色，保证明暗模式下都可以醒目的展示内容，如果您对当前的配色方案不满意，可以自定义 css 样式，也可以打开独立的明暗切换模式。后续会慢慢提供一些常用主题的明暗模式配色方案。

> V1.3.0 版本后加入了明暗适配选择器如果主题的明暗模式是通过 data-theme=dark 之类的属性变换进行明暗切换时，可以自动跟随主题的明暗切换自动切换评论区的明暗模式（通过 MutationObserver 对象实现）

明暗属性选择器获取方法：

打开调试模式，选择 html 或者是 body标签，切换明暗模式，然后找到暗色模式下的 css 样式选择器，对应复制到插件中即可。

部分主题的明暗属性选择器如下：

|        主题名称         |                   暗色模式选择器                    |
|:-------------------:|:--------------------------------------------:|
|   halo-theme-hao    |               data-theme=dark                |
|   halo-theme-Joe3   |                data-mode=dark                |
|       walker        | data-theme=dracula 或者 data-color-scheme=dark |
|      Butterfly      |               data-theme=dark                |
|     Theme-Stack     |               data-scheme=dark               |
|        默认主题         |                  html=dark                   |
|       Sakura        |                  body=dark                  |
| halo-theme-dream2.0 |   html=night    |


#### 1.3 halo-theme-hao 主题使用说明
由于 halo-theme-hao 主题已经加入了 artalk 评论，同时由于评论区引入的逻辑原因，无法直接使用本插件。如果想使用该插件，请修改 halo-theme-hao 主题的评论引入逻辑。后续作者会完善该教程！

### 2、插件功能
- 集成 artalk 评论区至 halo 系统中
- 支持数学公式
- 适配含有 pjax 的主题
- 可自定义 css 样式

### 3、配置说明
> 进行配置前请确保已经部署好 [artalk](https://artalk.js.org/guide/deploy.html)

#### 推荐部署方式
1、单独使用 docker-compose 方式
```docker
version: '3.8'
services:
  artalk:
    container_name: artalk
    image: artalk/artalk-go
    restart: unless-stopped
    ports:
      - 8080:23366
    volumes:
      - ./data:/data
    environment:
      - TZ=Asia/Shanghai
      - ATK_LOCALE=zh-CN
      - ATK_SITE_DEFAULT=Artalk 的博客
      - ATK_TRUSTED_DOMAINS=https://your_domain
```
2、如果halo是使用docker-compsoe 方式部署的，可在其后加入 artalk 容器，使其编排在一起
```docker
version: "3"

services:
  halo:
    image: halohub/halo:2.17.0-beta.1
    container_name: halo
    restart: on-failure:3
    depends_on:
      - meilisearch
    networks:
      - halo_network
    volumes:
      - ./halo2:/root/.halo2
    ports:
      - 8090:8090
    command:
      # 修改为自己已有的 MySQL 配置
      - --spring.r2dbc.url=r2dbc:pool:mysql://localhost:3306/halo
      - --spring.r2dbc.username=root
      - --spring.r2dbc.password=
      - --spring.sql.init.platform=mysql
      # 外部访问地址，请根据实际需要修改
      - --halo.external-url=http://localhost:8090/
      - --server.port=8090

  # 这部分为新增内容，创建一个 artalk 容器
  artalk:
    container_name: artalk
    image: artalk/artalk-go:2.8.7
    restart: on-failure:3
    ports:
      - 8080:23366
    volumes:
      - ./artalk:/data
    environment:
      - TZ=Asia/Shanghai
      - ATK_LOCALE=zh-CN
      - ATK_SITE_DEFAULT=默认站点名称
      - ATK_TRUSTED_DOMAINS=https://your_domain
networks:
    halo_network:
```
如果无法自行解决配置的一些相关问题，请参考文章：https://blog.wenjing.xin/archives/DV1kKzJX

![](https://api.minio.yyds.pink/bbs/2024-06-04/1717467151-408267-artalk-config.png)
根据配置的提示填写即可，注意在线资源是 artalk 官方提供的一些公共 CDN 资源，要确保自己的 artalk 版本是否和其一致，否则请引入自己搭建的 artalk 服务资源。

V1.3.0 之后只需要填写 artalk 的部署地址即可自动获取 js 和 css 资源地址。

### 4、主题适配
目前此插件为主题端提供了 /new_comment 路由，模板为 `new_comment.html`，也提供了 Finder API，可以将评论渲染到任何地方。
* 模板路径：/templates/new_comment.html
* 访问路径：/new_comment

#### Finder API
>`listAllComment()`  获取最近所有评论 

返回一组评论数组，格式如下
```json
{
    "data": [
        {
            "id": 5,
            "content": "φ(￣∇￣o)",
            "content_marked": "<p>φ(￣∇￣o)</p>\n",
            "user_id": 1,
            "nick": "admin",
            "email_encrypted": "47b2526e562c30463bc62391cb355d71",
            "link": "",
            "ua": "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_5_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36",
            "date": "2024-06-03 19:02:28",
            "is_collapsed": false,
            "is_pending": false,
            "is_pinned": false,
            "is_allow_reply": true,
            "is_verified": true,
            "rid": 0,
            "badge_name": "Admin",
            "badge_color": "#0083FF",
            "visible": true,
            "vote_up": 0,
            "vote_down": 0,
            "page_key": "/archives/hello-halo",
            "page_url": "http://localhost:8090/archives/hello-halo",
            "site_name": "artalk插件测试站点"
        },
        {
            ...
        }
    ]
}
```
>`getPageComment(String pageKey)`  获取指定页面的所有评论；
> 
> 参数pageKey 为指定页面的唯一标识，详情参考 artalk官方文档的 page_key 描述；

```json
{
    "comments": [
        {
            "id": 4,
            "content": "$$\nP(A|B_1, B_2, \\ldots, B_n) = \\frac{P(B_1, B_2, \\ldots, B_n|A) \\cdot P(A)}{P(B_1, B_2, \\ldots, B_n)}\n$$",
            "content_marked": "<p>$$<br/>\nP(A|B_1, B_2, \\ldots, B_n) = \\frac{P(B_1, B_2, \\ldots, B_n|A) \\cdot P(A)}{P(B_1, B_2, \\ldots, B_n)}<br/>\n$$</p>\n",
            "user_id": 3,
            "nick": "test",
            "email_encrypted": "9ee8b383b932c573f6f501b561246ed7",
            "link": "",
            "ua": "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_5_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36",
            "date": "2024-06-03 18:40:51",
            "is_collapsed": false,
            "is_pending": false,
            "is_pinned": false,
            "is_allow_reply": true,
            "is_verified": false,
            "rid": 0,
            "badge_name": "",
            "badge_color": "",
            "visible": true,
            "vote_up": 0,
            "vote_down": 0,
            "page_key": "/archives/hello-halo",
            "page_url": "http://localhost:8090/archives/hello-halo",
            "site_name": "artalk插件测试站点"
        },
        {
            ...
        }
    ],
    "count": 5,
    "roots_count": 4,
    "page": {
        "id": 2,
        "admin_only": false,
        "key": "/archives/hello-halo",
        "url": "http://localhost:8090/archives/hello-halo",
        "title": "Hello Halo - Halo",
        "site_name": "artalk插件测试站点",
        "vote_up": 0,
        "vote_down": 0,
        "pv": 3692
    }
}
```

#### webAPI
##### 1、获取指定页面的评论
url: `/apis/halo.wenjing.xin/v1alpha1/artalk/pageKeyComments`;

queryParam: `pageKey=/archives/hello-halo`

result： 返回结果同findersAPI

示例：
```js
fetch("/apis/halo.wenjing.xin/v1alpha1/artalk/pageKeyComments?pageKey=/archives/hello-halo")
        .then(res => res.json()).then(d => {
            const artalk = d.data.map(function (e) {
                return {
                    'comment': changeContents(e.content_marked),
                    'avatar': 'https://cravatar.cn/avatar/' + e.email_encrypted + '?d=mp&s=240',
                    'nick': e.nick,
                    'url': e.page_key,
                    'barrageBlogger': e.email_encrypted,
                    'id': 'atk-comment-' + e.id,
                    'created': e.date,
                }
            })
            renderer(artalk)})
        .catch((err) => { console.log(err); })
```
##### 2、获取最近所有的评论

url: `/apis/halo.wenjing.xin/v1alpha1/artalk/listAllComments`;

result： 返回结果同findersAPI

示例：
```js
fetch("/apis/halo.wenjing.xin/v1alpha1/artalk/listAllComments").then(res => res.json()).then(d => {
                // ......
     }).catch((err) => { console.log(err); })
```

## 二、其他

### 1、Todo
- [x] 为主题提供finders
- [X] 提供一些常见主题的明暗配色模式
- [X] 提供对[链接安全跳转中台插件](https://github.com/wenjing-xin/plugins-links-security-detect) 的兼容，使得评论区的链接也可以通过中台进行跳转
- [ ] 评论系统切换（hexo博客支持双评论系统切换，后续视情况而定）

### 2、赞助我
如果你感觉这个插件还不错，请我喝杯咖啡☕️☕️☕️
<div>
&emsp;&emsp;<img src="https://api.minio.yyds.pink/bbs/2024-06-05/1717550152-502697-wxpay.png" width=150px />
</div>

### 3、问题反馈
现在群内提问，若问题没有得到解决，则在 GitHub提交 [isssue](https://github.com/wenjing-xin/plugin-artalk/issues)

QQ交流群与QQ频道，加群后管理员自动审核
<div>
&emsp;&emsp;<img src="https://api.minio.yyds.pink/bbs/2024-06-04/1717467713-802505-qq.png" width=150px />
&emsp;&emsp;<img src="https://api.minio.yyds.pink/bbs/2024-06-04/1717467714-226493-qq.jpg" width=158px />
</div>

### 4、开发环境

插件开发的详细文档请查阅：<https://docs.halo.run/developer-guide/plugin/introduction>

### 5、站点

[个人站点](https://blog.wenjing.xin)

[问题反馈论坛](https://support.qq.com/product/651063)
