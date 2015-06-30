package com.guozhong.downloader.impl;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.guozhong.CrawlTask;
import com.guozhong.component.PageProcessor;
import com.guozhong.component.PageScript;
import com.guozhong.component.listener.ChromeDriverLifeListener;
import com.guozhong.downloader.JavaScriptDownloader;
import com.guozhong.downloader.PageDownloader;
import com.guozhong.downloader.driverpool.ChromeWebDriverPool;
import com.guozhong.model.Proccessable;
import com.guozhong.page.DefaultPageFactory;
import com.guozhong.page.Page;
import com.guozhong.page.PageFactory;
import com.guozhong.page.Status;
import com.guozhong.proxy.ProxyIp;
import com.guozhong.proxy.ProxyIpPool;
import com.guozhong.request.PageRequest;
import com.guozhong.util.ProccessableUtil;

/**
 * 谷歌下载器，如果需要登录可以用这个
 * @author Administrator
 *
 */
public final class ChromeDriverDownloader extends PageDownloader implements JavaScriptDownloader{
	
	private volatile ChromeWebDriverPool webDriverPool;
	
	private HashMap<Class<? extends PageProcessor>,PageScript> scripts = new HashMap<Class<? extends PageProcessor>,PageScript>();
	
	private final PageFactory pageFactory;

	public ChromeDriverDownloader() {
    	pageFactory = new DefaultPageFactory();
    }
	
	@Override
	public void close() throws IOException {
		checkInit();
		webDriverPool.closeAll();
	}

	@Override
	public Page download(PageRequest request, CrawlTask task) {
		checkInit();
		com.guozhong.downloader.impl.ZhongChromeDriver webDriver = null;
		Page page = null ;
		try{
			webDriver = webDriverPool.get();
			webDriver.get(request.getUrl());
			PageScript script = findPageScripts(request.getProcessorClass());
			if(script != null){
				List<Proccessable> objectContainer = ProccessableUtil.buildProcceableList();
		     	script.executeJS(webDriver,objectContainer);
		     	if(!objectContainer.isEmpty()){
		     		task.handleResult(null, objectContainer);
		     	}
		    }
			Status status = Status.fromHttpCode(200);//暂时定为200,将来扩展ChromeDriver取得
			String pageSource =webDriver.getPageSource();
			if(status.getBegin() >= 400 || status.equals(Status.UNSPECIFIED_ERROR)){
				page = pageFactory.buildErrorPage(request, status,webDriverPool, webDriver.getIndex());
			}else{
				WebElement root = webDriver.findElement(By.xpath("//html"));
				page = pageFactory.buildOkPage(request, status, pageSource ,root,webDriverPool, webDriver.getIndex());
			}
			page.setStatusCode(webDriver.getStatusCode());
		}catch(Exception e){
			e.printStackTrace();
			page = pageFactory.buildRetryPage(request,webDriverPool, webDriver.getIndex());
		}finally{
			if(webDriver!=null){
				webDriverPool.returnToPool(webDriver);
			}
		}
		return page;
	}
	

	 private void checkInit() {
	        if (webDriverPool == null) {
	            synchronized (this){
	                webDriverPool = new ChromeWebDriverPool();
	            }
	        }
	 }
	 
	public void setMaxDriverCount(int drivercount) {
		checkInit();
		webDriverPool.setMaxDriverCount(drivercount);
	}
	
	public void setMinDriverCount(int drivercount) {
		checkInit();
		webDriverPool.setMinDriverCount(drivercount);
	}
	

	@Override
	public void setTimeout(int second) {
		checkInit();
		this.webDriverPool.setPageLoadTimeout(second);
	}

	public void addChromeDriverLifeListener(
			ChromeDriverLifeListener chromeDriverLifeListener) {
		checkInit();
		webDriverPool.addChromeDriverLifeListener(chromeDriverLifeListener);
	}

	@Override
	public void open() {
		// TODO Auto-generated method stub
		checkInit();
		webDriverPool.open();
	}
	
	@Override
	public void setProxyIpPool(ProxyIpPool proxyIpPool) {
		throw new RuntimeException("谷歌浏览器暂时不支持设置动态代理IP,你可以在打开浏览器后设置");
	}

	@Override
	public void setMaxProxyRequestCount(int count) {
		throw new RuntimeException("谷歌浏览器暂时不支持设置动态代理IP,你可以在打开浏览器后设置");
	}

	@Override
	public boolean supportJavaScript() {
		return true;
	}

	@Override
	public void addJavaScriptFunction(Class<? extends PageProcessor> processorCls,PageScript javaScript){
    	PageScript func = scripts.get(processorCls);
    	if(func == null){
    		scripts.put(processorCls , javaScript);
    	}else{
    		throw new RuntimeException("脚本已经存在");
    	}
    }

	@Override
	public PageScript findPageScripts(
			Class<? extends PageProcessor> processorCls) {
		PageScript pageScript = scripts.get(processorCls);
		return pageScript;
	}

	
}
