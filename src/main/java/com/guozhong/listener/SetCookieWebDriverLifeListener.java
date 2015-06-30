package com.guozhong.listener;

import java.util.Set;



import com.guozhong.component.listener.WebDriverLifeListener;
import com.guozhong.downloader.impl.ZhongWebDriver;
import com.guozhong.request.Cookie;

public class SetCookieWebDriverLifeListener implements WebDriverLifeListener {
	
	private Set<Cookie> setCookies = null;
	
	public SetCookieWebDriverLifeListener(Set<Cookie> setCookies) {
		super();
		this.setCookies = setCookies;
	}


	@Override
	public void onCreated(int index, ZhongWebDriver webDriver) {
		if(setCookies  != null){
			com.gargoylesoftware.htmlunit.util.Cookie cookieManagerStore = null;
			for (Cookie cookie : setCookies) {
				cookieManagerStore = new com.gargoylesoftware.htmlunit.util.Cookie(cookie.getDomain(), cookie.getName(), cookie.getValue(), cookie.getPath(), cookie.getExpiry(), false);
				webDriver.setCookies(cookieManagerStore);
			}
		}
	}
	
	
	@Override
	public void onQuit(int index, ZhongWebDriver webDriver) {
	}
	

}
