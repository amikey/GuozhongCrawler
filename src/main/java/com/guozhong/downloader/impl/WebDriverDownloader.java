package com.guozhong.downloader.impl;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;




















import org.apache.http.conn.ConnectTimeoutException;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;





















import com.guozhong.CrawlTask;
import com.guozhong.component.PageProcessor;
import com.guozhong.component.PageScript;
import com.guozhong.component.listener.WebDriverLifeListener;
import com.guozhong.downloader.JavaScriptDownloader;
import com.guozhong.downloader.PageDownloader;
import com.guozhong.downloader.driverpool.WebDriverPool;
import com.guozhong.exception.NormalContainException;
import com.guozhong.exception.ProxyIpLoseException;
import com.guozhong.model.Proccessable;
import com.guozhong.page.DefaultPageFactory;
import com.guozhong.page.Page;
import com.guozhong.page.PageFactory;
import com.guozhong.page.Status;
import com.guozhong.proxy.ProxyIp;
import com.guozhong.proxy.ProxyIpPool;
import com.guozhong.request.PageRequest;
import com.guozhong.thread.CountableThreadPool;
import com.guozhong.util.ExceptionUtil;
import com.guozhong.util.ProccessableUtil;
import com.guozhong.util.StringUtil;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.google.common.cache.Weigher;
/**
 * 模拟浏览器下载器
 * @author Administrator
 *
 */
public class WebDriverDownloader extends PageDownloader implements JavaScriptDownloader{
	private final Logger log = Logger.getLogger(WebDriverDownloader.class);

	private volatile WebDriverPool webDriverPool;
	
	private final PageFactory pageFactory;

    private Logger logger = Logger.getLogger(getClass());
    
    private HashMap<Class<? extends PageProcessor>,PageScript> scripts = new HashMap<Class<? extends PageProcessor>,PageScript>();
    
    private DefaultPageDownloader defaultDownloader = new DefaultPageDownloader();
    
    private ProxyIpPool proxyIpPool;
    
    public WebDriverDownloader() {
    	pageFactory = new DefaultPageFactory();
    }
    
    
    public void addJavaScriptFunction(Class<? extends PageProcessor> processorCls,PageScript javaScript){
    	PageScript func = scripts.get(processorCls);
    	if(func == null){
    		scripts.put(processorCls , javaScript);
    	}else{
    		throw new RuntimeException("脚本已经存在");
    	}
    }

    /**
     * 拒绝mimeType问题后定
     */
	@Override
	public Page download(PageRequest request,CrawlTask task) {
		checkInit();
		if(request.isDefaultDownload()){
			return defaultDownload(request, task);
		}
		
		Page page = null;
		
		if(proxyIpPool == null){
			try {
				page = go(request, task, null);
			} catch (ProxyIpLoseException e) {}
			return page;
		}
		int proxyIpRequestCount = 0 ;
		ProxyIp ip = null;
		while(true){
			if(proxyIpRequestCount >= this.maxProxyRequestCount){
				log.error(request.getUrl()+"下载次数超过"+maxProxyRequestCount+"被丢弃"); 
				break;
			}
			ip = proxyIpPool.pollProxyIp();//不断去拿最新的代理IP去下载
			try {
				page = go(request, task, ip);
				if(ip.incrementRequestCount() >= proxyIpPool.getMaxUseCount()){
					//System.out.println(ip+"使用达到"+proxyIpPool.getMaxValidCount()+"次");
				}else{
					ip.markCache();//缓存IP
					//System.out.println(ip+"使用成功");
				}
				return page;
			} catch (ProxyIpLoseException e) {
				proxyIpRequestCount++;
				log.info(request.getUrl()+" "+ip+">下载失败");
				continue;
			} 
		}
		return page;
	}
	
	private Page go(PageRequest request,CrawlTask task,ProxyIp ip)throws ProxyIpLoseException{
		ProxyIpLoseException exception  = null;
		ZhongWebDriver webDriver;
		PageScript script = findPageScripts(request.getProcessorClass());
		try {
			webDriver = webDriverPool.get((script != null));
	    } catch (InterruptedException e) {
	           logger.error("interrupted", e);
	           e.printStackTrace();
	           return null;
	    }
	    setProxyIp(webDriver,ip);
		//logger.info("downloading page " + request.getUrl());
		Page page = null;
		int statuCode = 0;
		try{
			if(!request.getHedaers().isEmpty()){
				setHeaders(webDriver,request.getHedaers());
			}
			webDriver.get(request.getUrl());
			if(script != null){
				List<Proccessable> objectContainer = ProccessableUtil.buildProcceableList();
				script.executeJS(webDriver,objectContainer);
				if(!objectContainer.isEmpty()){
		     		task.handleResult(null, objectContainer);
		     	}
		    }
			String pageSource = webDriver.getPageSource();
			statuCode = webDriver.getResponseCode();
			Status status = Status.fromHttpCode(statuCode);
			if(ip != null){//代理验证是否是正常网页
				PageProcessor pageProcessor = task.findPageProccess(request.getProcessorClass());
				if(pageProcessor.getNormalContain() == null){
					throw new NullPointerException("您设置了代理模式，请为每个PageProccess实现getNormalContain方法");
				}
				Matcher matcher = pageProcessor.getNormalContain().matcher(pageSource);
				boolean containalNormal = matcher.find();
				if(!containalNormal){//网页内容是否正常含有特征字符串
					exception = new ProxyIpLoseException(ip.toString());
					throw exception;
				}
			}
			if(status.getBegin() >= 400 || status.equals(Status.UNSPECIFIED_ERROR)){
				page = pageFactory.buildErrorPage(request, status,webDriverPool , webDriver.getIndex());
			}else{
				WebElement root = null;
				if(pageSource.contains("<html")){//是html页面
					root = webDriver.findElement(By.xpath("//html"));
				}
				page = pageFactory.buildOkPage(request, status, pageSource,root,webDriverPool , webDriver.getIndex());
			}
			page.setStatusCode(statuCode);
		}catch(Exception e){
//			e.printStackTrace();
			if(e instanceof NullPointerException){
				throw new RuntimeException(e); 
			}
			if(ip != null ){//判断异常是否是代理Ip所导致的
				if(ExceptionUtil.existProxyException(e)){
					//e.printStackTrace();
					exception = new ProxyIpLoseException(ip.toString());
				}else{
					e.printStackTrace();
					System.out.println("ipppp:"+ip);
					page = pageFactory.buildRetryPage(request,webDriverPool , webDriver.getIndex());
				}
			}else{
				page = pageFactory.buildRetryPage(request,webDriverPool , webDriver.getIndex());
			}
		}finally{
			webDriverPool.returnToPool(webDriver);
			if(exception != null){//抛出ip失效异常
				throw exception;
			}
		}
		return page;
	}
	
	private void setHeaders(ZhongWebDriver webDriver,Map<String, String> hedaers) {
		Set<Entry<String, String>> sets = hedaers.entrySet();
		for (Entry<String, String> entry : sets) {
			webDriver.addRequestHeader(entry.getKey(), entry.getValue());
		}
	}


	public Page defaultDownload(PageRequest request,CrawlTask task) {
		return defaultDownloader.download(request, task);
	} 
	
	private final void setProxyIp(ZhongWebDriver webDriver,ProxyIp ip){
		if(proxyIpPool == null){
			return ;
		}
		WebClient wc = webDriver.getClient();
		WebClientOptions o = wc.getOptions();
		ProxyConfig proxyConfig = new ProxyConfig(ip.getIp(),ip.getPort());
		o.setProxyConfig(proxyConfig);
	}

	
    public void setMaxDriverCount(int count) {
    	checkInit();
    	webDriverPool.setMaxDriverCount(count);
    }
    
    public void setMinDriverCount(int count) {
    	checkInit();
    	webDriverPool.setMinDriverCount(count);
    }

    public void close() throws IOException {
        webDriverPool.closeAll();
    }
    
    private void checkInit() {
        if (webDriverPool == null) {
            synchronized (this){
                webDriverPool = new WebDriverPool();
            }
        }
    }

	public void setTimeout(int second) {
		this.webDriverPool.setPageLoadTimeout(second);
	}

	

	public PageScript findPageScripts(Class<? extends PageProcessor> processorCls){
		PageScript pageScript = scripts.get(processorCls);
		return pageScript;
	}


	public void open() { 
		checkInit();
		webDriverPool.open();
	}


	public final void setProxyIpPool(ProxyIpPool proxyIpPool) {
		this.proxyIpPool = proxyIpPool;
		defaultDownloader.setProxyIpPool(proxyIpPool);
	}


	@Override
	public void setMaxProxyRequestCount(int count) {
		super.setMaxProxyRequestCount(count);
		defaultDownloader.setMaxProxyRequestCount(count);
	}
	
	
	public void addWebDriverLifeListener(WebDriverLifeListener webDriverLifeListener){
		webDriverPool.addWebDriverLifeListener(webDriverLifeListener);
	}
	

	@Override
	public boolean supportJavaScript() {
		return true;
	}
}
