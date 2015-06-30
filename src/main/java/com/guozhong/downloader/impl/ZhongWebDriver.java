package com.guozhong.downloader.impl;


import java.util.HashSet;
import java.util.Set;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.util.Cookie;

/**
 * 扩展的HtmlUnitDriver为了效率关闭了css加载和js执行抛异常  
 * @author Administrator
 *
 */
public final class ZhongWebDriver extends HtmlUnitDriver implements JavaScriptDriver{
	
	private int index ;
	private Set<String> headerNames = new HashSet<String>();
	
	public ZhongWebDriver() {
		this(false);
	}

	public ZhongWebDriver(boolean enableJavascript) {
		this(BrowserVersion.FIREFOX_24);
		headerNames = new HashSet<String>();
		if(enableJavascript){
			setJavascriptEnabled(enableJavascript);
			WebClient webClient = getWebClient();
			webClient.getOptions().setCssEnabled(false);
			webClient.setAjaxController(new NicelyResynchronizingAjaxController());
			//webClient.getOptions().setTimeout(50000);
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);  
			webClient.getOptions().setActiveXNative(false);  //设置是否允许本地ActiveX或没有。默认值是false。
		}
	}
	
	public ZhongWebDriver(BrowserVersion version) {
		super(version);
	}

	public ZhongWebDriver(Capabilities capabilities) {
		super(capabilities);
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	public int getResponseCode() {
		Page page = lastPage();
		if (page == null) {
		      return -1;
		 }
		WebResponse response = page.getWebResponse();
		return response.getStatusCode();
    }


	public void addRequestHeader(String name,String value){
		WebClient webClient = getWebClient();
		webClient.addRequestHeader(name, value);
		headerNames.add(name);
	}

	public void clearHeaders(){
		WebClient webClient = getWebClient();
		for (String name:headerNames) {
			webClient.removeRequestHeader(name);
		}
		headerNames.clear();
	}
	
	public void get(String url){
		super.get(url);
		try {
			Thread.sleep(200);//等待200ms
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public WebClient getClient(){
		return  getWebClient();
	}
	
	public Set<com.gargoylesoftware.htmlunit.util.Cookie>  getCookies(){
		return getWebClient().getCookieManager().getCookies();
	}
	
	public void setCookies(Set<Cookie> cookies){
		CookieManager cookieManager = new CookieManager();
		cookieManager.setCookiesEnabled(true);
		for (Cookie cookie : cookies) {
			cookieManager.addCookie(cookie);
		}
		getWebClient().setCookieManager(cookieManager);
	}
	
	public void setCookies(Cookie cookie){
		getWebClient().getCookieManager().addCookie(cookie);
	}
	
	public Object executeScript(String script, Object ... args){
		return super.executeScript(script, args);
	}
	
	public Object executeAsyncScript(String script, Object ... args){
		return super.executeAsyncScript(script, args);
	}
	
}
