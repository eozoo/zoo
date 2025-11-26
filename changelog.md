## [2.7.6](https://github.com/eozoo/zoo/-/tags/2.7.6) (2025-11-26)



    ### Features

  -  [<font style="font-family: monospace;">[`90351f0f6bb933a`](https://github.com/eozoo/zoo/commit/90351f0f6bb933a)</font>] (**oauth**)   定义一些OAuth配置
  -  [<font style="font-family: monospace;">[`cc64d50ecdf6601`](https://github.com/eozoo/zoo/commit/cc64d50ecdf6601)</font>] (**tenant**)   支持租户
  -  [<font style="font-family: monospace;">[`b75fb06b28d3400`](https://github.com/eozoo/zoo/commit/b75fb06b28d3400)</font>] (**nacos**)   配置中心依赖
  -  [<font style="font-family: monospace;">[`a7d5c14f442764c`](https://github.com/eozoo/zoo/commit/a7d5c14f442764c)</font>] (**tools**)   枚举接口EnumVal
  -  [<font style="font-family: monospace;">[`74cfc2af8873113`](https://github.com/eozoo/zoo/commit/74cfc2af8873113)</font>] (**validator**)   枚举参数校验@IsEnum


    ### Bug Fixes

  -  [<font style="font-family: monospace;">[`f3840c6163cbb2b`](https://github.com/eozoo/zoo/commit/f3840c6163cbb2b)</font>] (**hutool**)   5.8.21 -> 5.7.22 jakarta不兼容
  -  [<font style="font-family: monospace;">[`ad779ab40a5f1f2`](https://github.com/eozoo/zoo/commit/ad779ab40a5f1f2)</font>] (**redis**)   Jedis和Lettuce的注入冲突问题
  -  [<font style="font-family: monospace;">[`1c51598fc55780d`](https://github.com/eozoo/zoo/commit/1c51598fc55780d)</font>] (**asserts**)   isBlank判断写错
  -  [<font style="font-family: monospace;">[`d6bf9f1df67c5fc`](https://github.com/eozoo/zoo/commit/d6bf9f1df67c5fc)</font>] (**HttpResponse**)   覆盖getStatusCode，避免不存在的status报错问题
  -  [<font style="font-family: monospace;">[`e3cf1d6b5c10f05`](https://github.com/eozoo/zoo/commit/e3cf1d6b5c10f05)</font>] (**operation**)   taskExecutor注入失败问题
  -  [<font style="font-family: monospace;">[`2539783710f4aca`](https://github.com/eozoo/zoo/commit/2539783710f4aca)</font>] (**build**)   jar构建配置错误
  -  [<font style="font-family: monospace;">[`5df6fcdf0f0a2e0`](https://github.com/eozoo/zoo/commit/5df6fcdf0f0a2e0)</font>] (**socket-io**)   连接处理判断问题
  -  [<font style="font-family: monospace;">[`998b9865d495a3a`](https://github.com/eozoo/zoo/commit/998b9865d495a3a)</font>] (**DateUtils**)   方法没有定义成static
  -  [<font style="font-family: monospace;">[`6ebf60fdf8f3a26`](https://github.com/eozoo/zoo/commit/6ebf60fdf8f3a26)</font>] (**token**)   accessToken漏填ip问题
  -  [<font style="font-family: monospace;">[`e8f0b32ec0f5782`](https://github.com/eozoo/zoo/commit/e8f0b32ec0f5782)</font>] (**mybatis**)   DatabaseProvider获取修改
  -  [<font style="font-family: monospace;">[`941c6175389f623`](https://github.com/eozoo/zoo/commit/941c6175389f623)</font>] (**access**)   检查空判断

## [2.7.5](https://github.com/eozoo/zoo/-/tags/2.7.5) (2024-11-09)



    ### Features

  -  [<font style="font-family: monospace;">[`b67dc8ca8d1b396`](https://github.com/eozoo/zoo/commit/b67dc8ca8d1b396)</font>] (**favicon**)   打包默认提供一个favicon.ico


    ### Bug Fixes

  -  interceptor注入问题
  -  [<font style="font-family: monospace;">[`89543a97e82189c`](https://github.com/eozoo/zoo/commit/89543a97e82189c)</font>] (**access**)   AccessFilter警告级别在响应中打印下请求信息

## [2.7.4](https://github.com/eozoo/zoo/-/tags/2.7.4) (2024-10-28)





    ### Bug Fixes

  -  [<font style="font-family: monospace;">[`469884df6d32e3a`](https://github.com/eozoo/zoo/commit/469884df6d32e3a)</font>] (**access**)   Access.page(int defaultSize)设置错误
  -  [<font style="font-family: monospace;">[`558115211f6c8a7`](https://github.com/eozoo/zoo/commit/558115211f6c8a7)</font>] (**Async**)   解决默认类型被Async定义覆盖了问题

## [2.7.3](https://github.com/eozoo/zoo/-/tags/2.7.3) (2024-10-19)



    ### Features

  -  [<font style="font-family: monospace;">[`e8b08f47533de29`](https://github.com/eozoo/zoo/commit/e8b08f47533de29)</font>] (**archetype**)   ddd工程模板
  -  [<font style="font-family: monospace;">[`a0f9da984345212`](https://github.com/eozoo/zoo/commit/a0f9da984345212)</font>] (**archetype**)   mvc工程模板



## [2.7.2](https://github.com/eozoo/zoo/-/tags/2.7.2) (2024-10-17)





    ### Bug Fixes

  -  [<font style="font-family: monospace;">[`2e180943263fb6c`](https://github.com/eozoo/zoo/commit/2e180943263fb6c)</font>] (**asyn**)   asyn注入失败

## [2.7.1](https://github.com/eozoo/zoo/-/tags/2.7.1) (2024-10-16)



    ### Features

  -  [<font style="font-family: monospace;">[`a78d41eca10aa6c`](https://github.com/eozoo/zoo/commit/a78d41eca10aa6c)</font>] (**ReteLimiter**)   merge方法调用频率限制
  -  [<font style="font-family: monospace;">[`8abe66771cb98fc`](https://github.com/eozoo/zoo/commit/8abe66771cb98fc)</font>] (**Access**)   优化配置类AccessConfiguration
  -  [<font style="font-family: monospace;">[`518a1f8f6a5bf8c`](https://github.com/eozoo/zoo/commit/518a1f8f6a5bf8c)</font>] (**redisson**)   添加@RedissonLock
  -  [<font style="font-family: monospace;">[`4b5ae3444c675ad`](https://github.com/eozoo/zoo/commit/4b5ae3444c675ad)</font>] (**i18n**)   优化国际化实现
  -  [<font style="font-family: monospace;">[`b99e952bd0db452`](https://github.com/eozoo/zoo/commit/b99e952bd0db452)</font>] (**kafka**)   定义common配置，与spring平级
  -  [<font style="font-family: monospace;">[`8fafc0d8585347e`](https://github.com/eozoo/zoo/commit/8fafc0d8585347e)</font>] (**redis**)   定义common配置，与spring平级
  -  [<font style="font-family: monospace;">[`29d4655770e5cfc`](https://github.com/eozoo/zoo/commit/29d4655770e5cfc)</font>] (**build**)   maven构建调整



