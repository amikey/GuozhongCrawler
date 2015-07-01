guozhongCrawler的是一个无须配置、便于二次开发的爬虫开源框架，它提供简单灵活的API，只需少量代码即可实现一个爬虫。模块化设计完全面向业务提供接口，功能覆盖整个爬虫的生命周期(链接提取、页面下载、内容抽取、持久化)，支持多线程抓取，分布式抓取，并支持自动重试，定制执行js、自定义cookie等功能。在处理网站抓取多次后被封IP的问题上，guozhongCrawler采用动态轮换IP机制有效防止IP被封。另外，源码中的注释及Log输出全部采用通俗易懂的中文。让初学者能有更加深刻的理解

guozhongCrawler特性：
1、可轻松定制不同URL优先级，可完成更为复杂的遍历业务，例如分页、AJAX
2、直接支持多线程多代理并发抓取
3、支持多任务（CrawlerTask）执行
4、支持多个请求的事务处理
5、内置3大网页下载内核HttpClient、WebDriver、谷歌浏览器
6、集成jsoup、xpath解析器
7、支持文件下载
8、定制http请求post、get方式，模拟header，注入cookie、params等参数
9、日志输出及源码注解采用中文，适合中国开发者学习和使用


GuozhongCrawler基础教程地址：
http://blog.csdn.net/u012572945/article/