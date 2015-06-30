package com.guozhong;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.guozhong.component.DynamicEntrance;
import com.guozhong.component.PageProcessor;
import com.guozhong.component.PageScript;
import com.guozhong.component.Pipeline;
import com.guozhong.component.listener.TaskLifeListener;
import com.guozhong.downloader.JavaScriptDownloader;
import com.guozhong.downloader.PageDownloader;
import com.guozhong.downloader.impl.ChromeDriverDownloader;
import com.guozhong.downloader.impl.DefaultFileDownloader;
import com.guozhong.downloader.impl.WebDriverDownloader;
import com.guozhong.exception.EntranceException;
import com.guozhong.model.Proccessable;
import com.guozhong.page.OkPage;
import com.guozhong.page.Page;
import com.guozhong.page.RetryPage;
import com.guozhong.queue.BlockingRequestQueue;
import com.guozhong.queue.RequestPriorityBlockingQueue;
import com.guozhong.request.BasicRequest;
import com.guozhong.request.BinaryRequest;
import com.guozhong.request.Cookie;
import com.guozhong.request.PageRequest;
import com.guozhong.request.StartContext;
import com.guozhong.request.PageRequest.PageEncoding;
import com.guozhong.request.TransactionRequest;
import com.guozhong.thread.CountableThreadPool;
import com.guozhong.util.ProccessableUtil;

/**
 * 爬虫任务类。控制整个爬虫的生命周期
 * @author 郭钟 
 * @QQ群  202568714
 *
 */
public class CrawlTask implements Runnable{
	
	private static Logger logger = Logger.getLogger(CrawlTask.class);
	
	
	public static final int DEFAULT_MAX_PAGE_RETRY_COUNT = 1;
	
	/**
	 * 默认文件下载线程数 
	 */
	public static final int DEFAULT_DOWNLOAD_FILE_THREAD = 3;
	
	private  String taskName ;
	
	
	private BlockingQueue<StartContext> startRequests = new LinkedBlockingQueue<StartContext>();
	
	/**
	 * 备份初始URL
	 */
	private List<StartContext> allStartBackups = new ArrayList<StartContext>();
	
	/**
	 * 默认用无延迟、优先级队列
	 */
	private BlockingRequestQueue requestQueue = new RequestPriorityBlockingQueue();
	
	private PageDownloader downloader ;
	
	private DefaultFileDownloader defaultFileDownloader;
	
	private int download_file_thread = DEFAULT_DOWNLOAD_FILE_THREAD;
	
	private CountableThreadPool downloadThreadPool ;
	
	private CountableThreadPool offlineHandleThreadPool ; //离线处理线程
	
	private int maxPageRetryCount = DEFAULT_MAX_PAGE_RETRY_COUNT;
	
	private Map<Class<? extends PageProcessor>,PageProcessor> taskPageProccess = new HashMap<Class<? extends PageProcessor>,PageProcessor>();
	
	private Pipeline  pipeline ;
	
	private CrawlManager spider ;
	
	private StartContext context;
	
	/**
	 * 生命周期监听类
	 */
	private TaskLifeListener taskLifeListener;
	
	/**
	 * 最近一次任务开始的时间
	 */
	private long lastStartTime ;
	
	/**
	 * 动态入口URL
	 */
	private DynamicEntrance dynamicEntrance;
	
	
	/**
	 * 缺省的编码
	 */
	private PageEncoding defaultEncoding;
	
	/**
	 * 临时变量，记录任务开始注入口URL个数
	 */
	private int seedCount;
	
	public CrawlTask(String name){
		name = name.replaceAll("[/\\\\*\\?<>|]", "_");//  /\*?<>|  替换文件名非法字符
		this.taskName  =name;
	}
	
	
	public String getTaskName(){
		return taskName;
	}

	public TaskLifeListener getTaskLifeListener(){
		return taskLifeListener;
	}

	public void setTaskLifeListener(TaskLifeListener taskLifeListener) {
		this.taskLifeListener = taskLifeListener;
	}
	
	public DynamicEntrance getDynamicEntrance() {
		return dynamicEntrance;
	}

	public void setDynamicEntrance(DynamicEntrance dynamicEntrance) {
		this.dynamicEntrance = dynamicEntrance;
	}
	
	public void setDownloadFileThread(int download_file_thread){
		if(download_file_thread > 200){
			throw new RuntimeException("下载线程不能大于200个");
		}
		if(download_file_thread<3){
			this.download_file_thread = 3;
		}else{
			this.download_file_thread = download_file_thread;
		}
	}
	
	public void setDownloadFileDelayTime(int time){
		initFileDownloadInstance();
		defaultFileDownloader.setDelayTime(time);
	}

	public long getLastStartTime() {
		return lastStartTime;
	}

	public void addStartContxt(StartContext context)
	{
		if(context.isEmpty()){
			throw new EntranceException("StartContext必须至少有1个seed");
		}
		allStartBackups.add(context);
		seedCount+=context.getSeedSize();
	}
	
	
	/**
	 * 添加种子URL设置附加参数和页面编码格式
	 * @param url
	 * @param contextAttribute
	 * @param charSet
	 * @return
	 */
	public void addStartUrl(String url,Class<? extends PageProcessor> processorCls,Map<String,Object> contextAttribute,PageEncoding pageEncoding){
		StartContext context = null;
		context = new StartContext(url, processorCls,pageEncoding);
		if(contextAttribute != null){
			for (Map.Entry<String, Object> keyValuePair :  contextAttribute.entrySet()) {
				context.putContextAttribute(keyValuePair.getKey(), keyValuePair.getValue());
			}
		}
		addStartContxt(context);
	}
	
	/**
	 * 清除所有初始URL  
	 * 在监听任务里初始化入口URL前调用该方法释放之前的入口url
	 */
	private void clearStartRequest(){
		startRequests.clear();
		allStartBackups.clear();
	}
	
	public void pushRequest(BasicRequest request){
		request.recodeRequest();//记录请求
		this.requestQueue.add(request);
	}
	
	public int getPageRetryCount() {
		return maxPageRetryCount;
	}
	
	public void setPageRetryCount(int pageRetryCount) {
		this.maxPageRetryCount = pageRetryCount;
	}
	
	public PageEncoding getDefaultEncoding() {
		return defaultEncoding;
	}


	public void setDefaultEncoding(PageEncoding defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}


	public void setDownloader(PageDownloader downloader) {
		this.downloader = downloader;
	}
	
	
	public Pipeline getPipeline() {
		return pipeline;
	}
	
	public void setPipeline(Pipeline pipeline) {
		this.pipeline = pipeline;
	}
	
	public BlockingRequestQueue getRequestQueue() {
		return requestQueue;
	}
	
	public void setRequestQueue(BlockingRequestQueue queue){
		this.requestQueue = queue;
	}
	
	public void setThreadPool(CountableThreadPool threadPool) {
		if(this.downloadThreadPool != null && !this.downloadThreadPool.isShutdown()){
			this.downloadThreadPool.shutdown();
		}
		this.downloadThreadPool = threadPool;
		initOfflineThread();
	}

	/**
	 * 初始化离线处理线程池
	 */
	private final void initOfflineThread() {
		if(offlineHandleThreadPool!=null){
			return;
		}
		if(downloadThreadPool.getThreadNum()<=20){
			 offlineHandleThreadPool = new CountableThreadPool(5);
		}else{
			int offlineThreadNum = downloadThreadPool.getThreadNum()/4;
			offlineHandleThreadPool = new CountableThreadPool(offlineThreadNum);
		}
	}
	
	public final BasicRequest poolRequest() throws InterruptedException{
		BasicRequest req = null;
		while (true) {
			if(downloadThreadPool.getIdleThreadCount() == 0){
				Thread.sleep(100);
				continue;//等待有线程可以工作
			}
			if ((!requestQueue.isEmpty() || !isSingleStartFinished())) {
				req = requestQueue.poll();
				if (req != null)
					break;
				else 
					Thread.sleep(100);
			} else {
				break;
			}
		}
		return req;
	}
	
	
	/**
	 * 从入口URL队列取得一个URL  如果为Null则说明这批入口已经抓完。去询问动态入口实例是否需要加载新的入口URL
	 * @return
	 */
	private boolean nextStartUrlQueue() {
		
		if(startRequests.isEmpty() && dynamicEntrance != null){
			if(dynamicEntrance.continueLoad()){
				if(dynamicEntrance.isClearLast()){
					clearStartRequest();//清除之前的入口URL
				}
				initDynamicEntrance();
				copyStartBackupToStartQueue();
			}
		}
		
		context = startRequests.poll();
		
		if(context != null && !context.isEmpty()){
			List<BasicRequest> subRequest = context.getSeedRequests();
			for (BasicRequest sub : subRequest) {
				pushRequest(sub);//添加跟进url
			}
		}
		return context!=null;
	}
	
	
	/**
	 * 每个入口URL及子队列全部抓取完成则返回true
	 * @return
	 */
	public boolean isSingleStartFinished(){
		int alive = downloadThreadPool.getThreadAlive();
		int offline = offlineHandleThreadPool.getThreadAlive();
		if(alive == 0 && requestQueue.isEmpty() && offline == 0){
			return true;
		}
		return false;
	}
	
	public boolean isRuning(){
		if(downloadThreadPool.isShutdown()){
			return false;
		}
		int alive = downloadThreadPool.getThreadAlive();
		int offline = offlineHandleThreadPool.getThreadAlive();
		if(alive > 0 || !requestQueue.isEmpty() || offline > 0)
			return true;
		return false;
	}
	
	public PageDownloader getDownloader(){
		return this.downloader;
	}
	
	@Override
	public void run() {
		logger.info("开始抓取");
		try{
			initTask();
		}catch(EntranceException e){
			destoryCrawlTask();
			//任务生命周期回调
			if(taskLifeListener != null){
				taskLifeListener.onFinished(this);
			}
			throw e;
		}
		
		lastStartTime  = System.currentTimeMillis();
		downloader.open();//打开下载器
		
		while(!Thread.currentThread().isInterrupted()){
			BasicRequest request ;
			try{
				request = poolRequest();
				if(request == null){
					if(isSingleStartFinished() && !nextStartUrlQueue()){//如果当前入口URL抓完并且没有了下一个入口URL则完成任务
						destoryCrawlTask();
						break;
					}else{
						//seelp(0.2f);//每抓完一个入口URL  沉睡200ms
						continue;
					}
				}
			}catch(Exception e){
				e.printStackTrace();
				logger.error("轮询队列出错",e);
				break;
			}
			final BasicRequest finalRequest = request;
			final StartContext finalContext  = context;
			invokeDownload(finalRequest, finalContext);
		}
		
	}
	
	/**
	 * 初始化任务
	 */
	private void initTask() throws EntranceException{
		logger.info("正在注入口信息....");
		initDynamicEntrance();
		if(allStartBackups.isEmpty()){
			throw new EntranceException("种子URL数至少有1个");
		}
		copyStartBackupToStartQueue();
		logger.info("注入StartContext"+startRequests.size()+"个"); 
		logger.info("注入种子URL"+seedCount+"个"); 
		if(taskLifeListener != null){
			taskLifeListener.onStart(this);
		}
		nextStartUrlQueue();
	}


	/**
	 * 从备份开始库拷贝到开始队列
	 */
	private void copyStartBackupToStartQueue() {
		if(startRequests.isEmpty()){
			for (StartContext context: allStartBackups) {//开始队列加载种子URL
				startRequests.add(context);
			}
		}
	}

	/**
	 * 动态加载入口URL
	 */
	private final void initDynamicEntrance() {
		if(dynamicEntrance != null){
			List<StartContext> startContexts = dynamicEntrance.loadStartContext();
			if(startContexts != null){
				for (StartContext sc : startContexts) {
					addStartContxt(sc);
				}
			}
		}
	}
	
	
	/**
	 * 调用下载
	 * @param finalRequest
	 * @param finalContext
	 */
	@SuppressWarnings("unused")
	private final void invokeDownload(final BasicRequest finalRequest,
			final StartContext finalContext) {
		switch(finalRequest.getType()){
		case PAGE_REQUEST:
			final PageRequest pageRequest = (PageRequest) finalRequest;
			if(pageRequest.getPageEncoding()==null && defaultEncoding!=null){
				pageRequest.setPageEncoding(defaultEncoding);
			}else{
				pageRequest.setPageEncoding(PageEncoding.UTF8);
			}
			downloadThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					PageProcessor pageProccess = findPageProccess(pageRequest.getProcessorClass());
					if(pageProccess == null){
						return;
					}
					Page page = downloader.download(pageRequest,CrawlTask.this);
					if(page == null) return;//取不到page则返回
					logger.info("抓取:"+pageRequest.getUrl()+"\tStatus:"+page.getStatus()+"\tCode:"+page.getStatusCode());
					offlineHandle(pageProccess, page, finalContext);
				}
			});
			break;
		case TRANSACTION_REQUEST:
			TransactionRequest transactionRequest = (TransactionRequest)finalRequest;
			transactionRequest.setPipeline(pipeline);
			Iterator<BasicRequest> basicRequestIter = transactionRequest.iteratorChildRequests();
			while(basicRequestIter.hasNext()){
				BasicRequest child = basicRequestIter.next();
				invokeDownload(child, finalContext);
			}
			break;
		case BINARY_REQUEST:
			initFileDownloadInstance();
			defaultFileDownloader.downloadFile((BinaryRequest) finalRequest);
			break;
		}
	}

	/**
	 * 初始化文件下载线程池
	 */
	private void initFileDownloadInstance() {
		if(defaultFileDownloader == null){
			synchronized (this){
				if(defaultFileDownloader == null){
					defaultFileDownloader = new DefaultFileDownloader(download_file_thread);
				}
			}
		}
	}
	
	/**
	 * 离线处理
	 * @param pageProccess
	 * @param page
	 * @return
	 */
	private final void offlineHandle(final PageProcessor pageProccess ,final Page page,final StartContext finalContext){
		offlineHandleThreadPool.execute(new Runnable() {
			
			@Override
			public void run() {
				if(page instanceof RetryPage){
					RetryPage retryPage = (RetryPage) page;
					PageRequest retryRequest = retryPage.getRequest();
					if(retryRequest.getHistoryCount() < maxPageRetryCount){
						pushRequest(retryRequest);
						logger.warn("重新请求URL："+retryPage.getRequest().getUrl());
					}else{
						retryRequest.notify(retryRequest.hashCode());
						logger.error("下载次数超过"+maxPageRetryCount+":"+retryPage.getRequest().getUrl()+" 被丢弃");
					}
				}else if(page instanceof OkPage){
					try {
						List<BasicRequest> newRequests = new ArrayList<BasicRequest>();
						List<Proccessable> objectContainer = new ArrayList<Proccessable>();
					    pageProccess.process((OkPage) page,finalContext,newRequests,objectContainer);
						handleResult(newRequests,objectContainer);
						BasicRequest basicRequest = page.getRequest();
					    basicRequest.notify(basicRequest.hashCode());
					} catch (Exception e) {
						if(page.isNeedPost()){
							page.handleComplete();
						}
						e.printStackTrace();
						logger.error("离线处理异常URL:"+page.getRequest().getUrl(),e);
					}
				}
			}
		});
	}

	public void handleResult(List<BasicRequest> newRequests,List<Proccessable> objectContainer){
		//跟进URL加入队列
		if(newRequests != null){
			for (BasicRequest req : newRequests) {
				pushRequest(req);
			}
		}
		if(pipeline != null && !objectContainer.isEmpty()){
			pipeline.proccessData(objectContainer);
		}
	}
	
	private synchronized PageProcessor  addPageProccess(Class<? extends PageProcessor> proccessCls){
		if(taskPageProccess.get(proccessCls) != null){
			return taskPageProccess.get(proccessCls);
		}
		PageProcessor proccess = null;
		try {
			proccess = proccessCls.newInstance();
			if(!taskPageProccess.containsKey(proccessCls)){
				taskPageProccess.put( proccessCls , proccess );
				PageScript javaScript = proccess.getJavaScript();
				if(javaScript != null){
					if(!downloader.supportJavaScript()){
						throw new RuntimeException("javaScript需要使用WebDriverDownloader或者ChromeDriverDownloader");
					}
					JavaScriptDownloader seleniumDownloader = (JavaScriptDownloader) downloader;
					seleniumDownloader.addJavaScriptFunction(proccess.getClass(),javaScript);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return proccess;
	}
	
	public PageProcessor findPageProccess(
			Class<? extends PageProcessor> processorCls) {
		PageProcessor pageProcessor = taskPageProccess.get(processorCls);
		if(pageProcessor == null){
			pageProcessor = addPageProccess(processorCls);
		}
		return pageProcessor;
	}
	
	
	protected void ownerSpider(CrawlManager spider){
		this.spider = spider;
	}

	/**
	 * 任务完成销毁任务
	 */
	private final void destoryCrawlTask(){
		//释放下载器
		try {
			downloader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		spider.destoryCrawTask(taskName);//销毁任务
		downloadThreadPool.shutdown();
		offlineHandleThreadPool.shutdown();
		
		//任务生命周期回调
		if(taskLifeListener != null){
			taskLifeListener.onFinished(this);
		}
		logger.info(taskName+"完成销毁");
	}
	
}
