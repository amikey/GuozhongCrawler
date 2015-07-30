package com.guozhong;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import redis.clients.jedis.JedisPoolConfig;

import com.guozhong.component.DynamicEntrance;
import com.guozhong.component.PageProcessor;
import com.guozhong.component.Pipeline;
import com.guozhong.component.listener.ChromeDriverLifeListener;
import com.guozhong.component.listener.HttpClientLifeListener;
import com.guozhong.component.listener.TaskLifeListener;
import com.guozhong.component.listener.WebDriverLifeListener;
import com.guozhong.downloader.PageDownloader;
import com.guozhong.downloader.impl.ChromeDriverDownloader;
import com.guozhong.downloader.impl.DefaultPageDownloader;
import com.guozhong.downloader.impl.WebDriverDownloader;
import com.guozhong.listener.SetCookieHttpClientLifeListener;
import com.guozhong.listener.SetCookieWebDriverLifeListener;
import com.guozhong.proxy.ProxyIpPool;
import com.guozhong.queue.DelayedBlockingQueue;
import com.guozhong.queue.DelayedPriorityBlockingQueue;
import com.guozhong.queue.RedisRequestBlockingQueue;
import com.guozhong.queue.RequestPriorityBlockingQueue;
import com.guozhong.queue.SimpleBlockingQueue;
import com.guozhong.request.Cookie;
import com.guozhong.request.PageRequest.PageEncoding;
import com.guozhong.thread.CountableThreadPool;
import com.guozhong.timer.CrawlTimerTask;
/**
 * GuozhongCrawler是分层架构。要快速学习CrawlTask独立的配置多少要了解框架的源代码。所以CrawTaskBuilder提供要更加扁平且易于理解的的方式创建CrawTask
 * @author 郭钟 
 * @QQ群  202568714
 *
 */
public class CrawTaskBuilder {
	
	/**
	 * 默认的线程数
	 */
	public static final int DEFAULT_TASK_THREADPOOL = 5;
	
	private CrawlTask crawlTask;
	
	protected PageDownloader downloader;
	
	protected CountableThreadPool threadPool;
	
	
	/**
	 * 通过给予一个CrawlTask的Name来创建一个CrawTaskBuilder
	 * @param taskName
	 */
	protected CrawTaskBuilder(String taskName){
		crawlTask = new CrawlTask(taskName);
	}
	
	/**
	 * 将PageDownloader设置为爬虫的网页下载器
	 * @param cls 给予一个PageDownloader网页下载器Class
	 */
	protected void newDownloadInstance(Class<? extends PageDownloader> cls)  {
		try {
			downloader = cls.newInstance();
			crawlTask.setDownloader(downloader);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 取得网页下载器
	 * @return PageDownloader
	 */
	protected PageDownloader getPageDownloader(){
		return downloader;
	}

	/**
	 * 
	 * @return 返回CrawlTask
	 */
	protected CrawlTask getCrawlTask() {
		return crawlTask;
	}
	
	/**
	 * 设置CrawlTask下载处理Request的线程数量
	 * @param threadNum
	 * @return CrawTaskBuilder
	 */
	public CrawTaskBuilder useThread(int threadNum){
		if (threadPool != null && !threadPool.isShutdown()) {
			threadPool.shutdown();
        }
		threadPool = new CountableThreadPool(threadNum);
		crawlTask.setThreadPool(threadPool);
		return this;
	}
	
	
	/**
	 * 设置实现好的Pipeline类Class
	 * @param pipelineCls  持久化处理类
	 * @return CrawTaskBuilder
	 */
	public CrawTaskBuilder usePipeline(Class<? extends Pipeline> pipelineCls){
		Pipeline pipeLine;
		try {
			pipeLine = pipelineCls.newInstance();
			crawlTask.setPipeline(pipeLine);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	
	/**
	 * 如果由于网络问题，请求url时可能会出现失败的情况。那么你设置最大重新请求的次数默认重新请求1次
	 * @param retryCount
	 * @return CrawTaskBuilder
	 */
	public CrawTaskBuilder usePageRetryCount(int retryCount){
		crawlTask.setPageRetryCount(retryCount);
		return this;
	}
	
	/**
	 * 一般抓取某个网站会有统一的编码，如果你不想每次都调用PageRequest.setPageEncoding的话，那么你可以设置一个默认的编码
	 * @return
	 */
	public CrawTaskBuilder usePageEncoding(PageEncoding defaultEncoding){
		crawlTask.setDefaultEncoding(defaultEncoding);
		return this;
	}
	
	/**
	 * 添加种子URL设置附加参数和页面编码格式
	 * 每个injectStartUrl方法注入的种子URL会用一个单独的StatContext包装。如果StatContext在抓取过程中不会产生较多新的跟进Request
	 * 那么推荐你使用useDynamicEntrance设置入口URL将会更加提升效率
	 * @param url
	 * @param contextAttribute
	 * @param PageEncoding
	 * @return
	 */
	public CrawTaskBuilder injectStartUrl(String url,Class<? extends PageProcessor> processorCls,Map<String,Object> contextAttribute,PageEncoding  pageEncoding){
		crawlTask.addStartUrl(url, processorCls,contextAttribute,  pageEncoding);
		return this;
	}
	
	/**
	 * 添加种子URL并设置附加参数
	 * 每个injectStartUrl方法注入的种子URL会用一个单独的StatContext包装。如果StatContext在抓取过程中不会产生较多新的跟进Request
	 * 那么推荐你使用useDynamicEntrance设置入口URL将会更加提升效率
	 * @param url
	 * @param contextAttribute
	 * @return
	 */
	public CrawTaskBuilder injectStartUrl(String url,Class<? extends PageProcessor> processorCls,Map<String,Object> contextAttribute){
		return injectStartUrl(url, processorCls ,contextAttribute , null);
	}
	
	/**
	 * 添加种子URL，并指定PageProcessor。
	 * 每个injectStartUrl方法注入的种子URL会用一个单独的StatContext包装。如果StatContext在抓取过程中不会产生较多新的跟进Request
	 * 那么推荐你使用useDynamicEntrance设置入口URL将会更加提升效率
	 * @param url
	 * @return
	 */
	public CrawTaskBuilder injectStartUrl(String url,Class<? extends PageProcessor> processorCls){
		return injectStartUrl(url, processorCls ,null);
	}
	
	/**
	 * 如果你想在单个StartContext中直接初始化跟进URL，或者让爬虫分批注入种子的话。那么DynamicEntrance提供了这样的接口
	 * @param dynamicEntranceCls DynamicEntrance的继承实现类
	 * @return
	 */
	public CrawTaskBuilder useDynamicEntrance(Class<? extends DynamicEntrance> dynamicEntranceCls){
		DynamicEntrance dynamicEntrance;
		try {
			dynamicEntrance = dynamicEntranceCls.newInstance();
			crawlTask.setDynamicEntrance(dynamicEntrance);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	/**
	 * 如果你想在单个StartContext中直接初始化跟进URL，或者让爬虫分批注入种子的话。那么DynamicEntrance提供了这样的接口
	 * @param dynamicEntrance 实例
	 * @return
	 */
	public CrawTaskBuilder useDynamicEntranceInstance(DynamicEntrance dynamicEntrance){
			if(dynamicEntrance == null){
				throw new NullPointerException();
			}
			crawlTask.setDynamicEntrance(dynamicEntrance);
		return this;
	}
	
	/**
	 * SimpleBlockingQueue采用先进先出的FIFO原则。广度优先策略合适的队列
	 * @return
	 */
	public CrawTaskBuilder useQueueSimpleBlockingRequest(){
		crawlTask.setRequestQueue(new SimpleBlockingQueue());
		return this;
	}
	
	/**
	 * DelayedBlockingQueue和SimpleBlockingQueue一样采用先进先出的FIFO原则。广度优先策略合适的队列、
	 * 但是增加了设置延时的特性
	 * @return
	 */
	public CrawTaskBuilder useQueueQueueDelayedRequest(int delayInMilliseconds){
		crawlTask.setRequestQueue(new DelayedBlockingQueue(delayInMilliseconds));
		return this;
	}
	
	/**
	 * 使用优先级队列，在一些抓取分页较多情景下推荐使用QueuePriorityRequest。因为
	 * 它能很好的保证优先级高的Request优先被处理。从而防止队列金字塔式的膨胀
	 * @return
	 */
	public CrawTaskBuilder useQueuePriorityRequest(){
		crawlTask.setRequestQueue(new RequestPriorityBlockingQueue());
		return this;
	}
	
	
	/**
	 * 使用延迟优先级队列，和QueuePriorityRequest类似。但QueueDelayedPriorityRequest额外提供了延迟抓取的功能
	 * 在一些因为请求频率过快而被封的网站上推荐使用QueueDelayedPriorityRequest
	 * @param delayInMilliseconds  每次取Request距离上次时间延迟delayInMilliseconds毫秒
	 * @return
	 */
	public CrawTaskBuilder useQueueDelayedPriorityRequest(int delayInMilliseconds){
		crawlTask.setRequestQueue(new DelayedPriorityBlockingQueue(delayInMilliseconds));
		return this;
	}
	
	/**
	 * 使用基于redis的队列
	 * @return
	 */
	public CrawTaskBuilder useRedisQueueRequest(String host, int port, String queue){
		crawlTask.setRequestQueue(new RedisRequestBlockingQueue(host, port, queue));
		return this;
	}
	
	/**
	 * 使用基于redis的队列
	 * @return
	 */
	public CrawTaskBuilder useRedisQueueRequest(String host, int port, JedisPoolConfig config, String queue){
		crawlTask.setRequestQueue(new RedisRequestBlockingQueue(host, port, config, queue));
		return this;
	}
	
	/**
	 * 设置监听器，监听爬虫的CrawlTask的onStart 和 onFinish。在此你可以发送邮件或者其他方式来知晓爬虫的执行情况
	 * @param listener
	 * @return
	 */
	public CrawTaskBuilder useTaskLifeListener(TaskLifeListener listener){
		crawlTask.setTaskLifeListener(listener);
		return this;
	}
	
	/**
	 * 设置Cookie，当Driver创建时设置cookies。在需要登录情况下你可以将登录好的Cookies注入downloader
	 * @param listener
	 * @return
	 */
	public CrawTaskBuilder useCookie(Set<Cookie> cookies){
		if(cookies == null || cookies.isEmpty()){
			throw new IllegalArgumentException("cookies不能为NULL或者空");
		}
		if(downloader instanceof DefaultPageDownloader){
			((DefaultPageDownloader)downloader).addHttpClientLifeListener(new SetCookieHttpClientLifeListener(cookies));
		}else if(downloader instanceof WebDriverDownloader){
			((WebDriverDownloader)downloader).addWebDriverLifeListener(new SetCookieWebDriverLifeListener(cookies));
		}else{
			throw new IllegalArgumentException("由于谷歌浏览器内核阻止设置Cookie操作。所以设置Cookie失败。你可以使用DefaultPageDownloader或者WebDriverDownloader替换");
		}
		return this;
	}
	
	/**
	 * 当你使用ChromeDownloader作为下载器时可以设置ChromeDriverLifeListener
	 * @param listener
	 * @return
	 */
	public void addChromeDriverLifeListener(ChromeDriverLifeListener chromeDriverLifeListener) {
		if(downloader instanceof ChromeDriverDownloader){
			ChromeDriverDownloader d = (ChromeDriverDownloader) downloader;
			d.addChromeDriverLifeListener(chromeDriverLifeListener);
		}else{
			throw new RuntimeException("addChromeDriverLifeListener()需要使用  ChromeDownloader");
		}
	}
	
	/**
	 * 当你使用WebDriverDownloader作为下载器时可以设置ChromeDriverLifeListener
	 * @param listener
	 * @return
	 */
	public void addWebDriverLifeListener(WebDriverLifeListener webDriverLifeListener) {
		if(downloader instanceof WebDriverDownloader){
			WebDriverDownloader d = (WebDriverDownloader) downloader;
			d.addWebDriverLifeListener(webDriverLifeListener);
		}else{
			throw new RuntimeException("addWebDriverLifeListener()需要使用  WebDriverDownloader");
		}
	}
	
	/**
	 * 当你使用默认的DefaultPageDownloader作为下载器时可以设置HttpClientLifeListener
	 * @param listener
	 * @return
	 */
	public void addHttpClientLifeListener(HttpClientLifeListener httpClientLifeListener) {
		if(downloader instanceof DefaultPageDownloader){
			DefaultPageDownloader d = (DefaultPageDownloader) downloader;
			d.addHttpClientLifeListener(httpClientLifeListener);
		}else{
			throw new RuntimeException("addHttpClientLifeListener()需要使用  DefaultPageDownloader");
		}
	}
	
	
	/**
	 * 使用代理IP切换机制时设置一个ProxyIpPool的实现类即可。在封IP网站下推荐使用收费版代理IP效果更佳
	 * @param proxyIpPoolCls  
	 * @param initSize  每次代理IP缓冲池IP不足时加载IP的个数，推荐使用公式initSize=thread*5
	 * @param pastTime  单位毫秒 每个IP自身的过期时间，当代理IP过期时间到的时候会被清除。这个值根据代理IP的质量决定
	 * @param max_use_count  每个代理IP最多使用的次数。推荐使用公式max_use_count =（目标网站连续请求才被封的次数）减去  2到3
	 * @return
	 */
	public CrawTaskBuilder useProxyIpPool(Class<? extends ProxyIpPool> proxyIpPoolCls,int initSize,long pastTime,int max_use_count) {
		ProxyIpPool proxyIpPool;
		try {
			Constructor<? extends ProxyIpPool> constructor = proxyIpPoolCls.getConstructor(int.class,long.class,int.class);
			proxyIpPool = constructor.newInstance(initSize,pastTime, max_use_count);
			crawlTask.getDownloader().setProxyIpPool(proxyIpPool);
		} catch (InstantiationException | NoSuchMethodException | SecurityException |IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	/**
	 * 当然你也可以自己构造一个实例设置ProxyIpPool
	 * @param proxyIpPool
	 * @return
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	public CrawTaskBuilder useProxyIpPoolInstance(ProxyIpPool proxyIpPool) {
		if(proxyIpPool == null){
			throw new NullPointerException();
		}
		crawlTask.getDownloader().setProxyIpPool(proxyIpPool);
		return this;
	}
	
	private final void initComponent(){
		if (threadPool == null || threadPool.isShutdown()) {
            threadPool = new CountableThreadPool(DEFAULT_TASK_THREADPOOL);
            crawlTask.setThreadPool(threadPool);
        }
		PageDownloader downloader = crawlTask.getDownloader();
		if(downloader == null){
			downloader = new DefaultPageDownloader();
			crawlTask.setDownloader(downloader);
		}
		if(downloader instanceof ChromeDriverDownloader){
			if(threadPool.getThreadNum() > 10){
				((ChromeDriverDownloader)downloader).setMaxDriverCount(10);//谷歌浏览器最多driver数30个因为耗内存巨大
			}else{
				((ChromeDriverDownloader)downloader).setMaxDriverCount(threadPool.getThreadNum());
			}
		}
	}
	
	/**
	 * 使用定时循环启动，使用24小时制
	 * @param hour  从几点开始启动，如果当前时间小于改时间则等待到改时间启动
	 * @param period  每次抓取时间间隔  单位毫秒
	 * @param endHour  到几点结束
	 * @return
	 */
	public final CrawTaskBuilder useTimer(int hour,long period,int endHour){
		new CrawlTimerTask(hour, period, endHour, crawlTask);
		return this;
	}
	
	/**
	 * 设置同时下载文件的线程数 ，默认3个线程
	 * @param thread
	 * @return CrawTaskBuilder
	 */
	public CrawTaskBuilder useDownloadFileThread(int thread){
		crawlTask.setDownloadFileThread(thread);
		return this;
	}
	
	/**
	 * 文件下载延迟，默认300ms
	 * @param millisecond
	 * @return
	 */
	public CrawTaskBuilder useDownloadFileDelayTime(int millisecond){
		crawlTask.setDownloadFileDelayTime(millisecond);
		return this;
	}
	
	/**
	 * 配置完成，即可创建CrawlTask
	 * @return CrawlTask
	 */
	public CrawlTask build(){
		initComponent();
		return crawlTask;
	}
	
	
}
