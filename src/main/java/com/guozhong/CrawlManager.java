package com.guozhong;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.guozhong.component.DynamicEntrance;
import com.guozhong.component.PageProcessor;
import com.guozhong.component.Pipeline;
import com.guozhong.component.listener.ChromeDriverLifeListener;
import com.guozhong.component.listener.HttpClientLifeListener;
import com.guozhong.component.listener.TaskLifeListener;
import com.guozhong.downloader.PageDownloader;
import com.guozhong.downloader.impl.ChromeDriverDownloader;
import com.guozhong.downloader.impl.DefaultPageDownloader;
import com.guozhong.downloader.impl.WebDriverDownloader;
import com.guozhong.proxy.ProxyIpPool;
import com.guozhong.queue.DelayedPriorityBlockingQueue;
import com.guozhong.queue.RequestPriorityBlockingQueue;
import com.guozhong.request.PageRequest.PageEncoding;
import com.guozhong.thread.CountableThreadPool;
import com.guozhong.timer.CrawlTimerTask;
import com.guozhong.util.URLUtil;


/**
 * CrawlManager是CrawlTask的管理和启动类。
 * @author 郭钟
 *
 */

public class CrawlManager{
	
	private static Logger logger = Logger.getLogger(CrawlManager.class);
	
	/**
	 * 默认的线程数
	 */
	public static final int DEFAULT_TASK_THREADPOOL = 5;
	
	private static final CrawlManager spiderManager = new CrawlManager();
	
	protected Map<String,CrawlTask> allTask = new HashMap<String, CrawlTask>();
	
	private CrawlManager(){}
	
	/**
	 * 在一个java进程中CrawlManager的实例只能是一个
	 * @return
	 */
	public final synchronized static CrawlManager getInstance(){
		return spiderManager;
	}
	
	/**
	 * GuozhongCrawler内置三大下载器：
	 * 1、分别是采用HttpClient作为内核下载的DefaultPageDownloader
	 * 2、采用HtmlUnitDriver作为内核下载WebDriverDownloader
	 * 3、采用ChromeDriver调用浏览器作为内核下载的ChromeDriverDownloader。
	 * 另外你可以继承自PageDownloader实现自己的PageDownloader类
	 * @param taskName
	 * @param cls
	 * @return
	 */
	public CrawTaskBuilder prepareCrawlTask(String taskName,Class<? extends PageDownloader> cls) {
		if(spiderManager.allTask.containsKey(taskName)){//如果任务存在并且还在运行
			throw new IllegalArgumentException("任务已经存在");
		}else{
			CrawTaskBuilder builder = new CrawTaskBuilder(taskName);
			builder.newDownloadInstance(cls);
			builder.getCrawlTask().ownerSpider(spiderManager);
			return builder;
		}
	}
	
	/**
	 * 启动任务
	 */
	public void start(CrawlTask crawlTask) {
		CrawWork crawWork = new CrawWork(crawlTask);
		crawWork.start();
	}
	
	/**
	 * 销毁任务
	 */
	public void destoryCrawTask(String taskName){
		System.out.println("销毁："+taskName);
		allTask.remove(taskName);
	}
	
	/**
	 * 通过CrawlTask的name取得一个CrawlTask
	 * @param name
	 * @return
	 */
	public CrawlTask getCrawlTask(String name){
		return allTask.get(name);
	}
	
	public boolean hasRunCrawTask(){
		return !allTask.isEmpty();
	}
	
	/**
	 * 
	 * @author 郭钟 
	 * @QQ群  202568714
	 * CrawWork作用于爬虫的控制线程类。
	 *
	 */
	final class CrawWork extends Thread{

		private CrawlTask crawlTask;
		
		public CrawWork(CrawlTask crawlTask) {
			super(crawlTask);
			this.crawlTask = crawlTask;
		}

		@Override
		public synchronized void start() {
			super.start();
			/**
			 * 确保安全启动后加入CrawManager管理
			 */
			allTask.put(crawlTask.getTaskName(), crawlTask);
		}
		
	}
}
