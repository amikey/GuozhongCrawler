package com.guozhong.listener;

import java.util.Set;

import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import com.guozhong.component.listener.HttpClientLifeListener;
import com.guozhong.downloader.impl.ZhongHttpClient;
import com.guozhong.request.Cookie;

public class SetCookieHttpClientLifeListener implements HttpClientLifeListener {
	
	private Set<Cookie> setCookies = null;
	

	public SetCookieHttpClientLifeListener(Set<Cookie> setCookies) {
		super();
		this.setCookies = setCookies;
	}

	@Override
	public void onCreated(int index, ZhongHttpClient httpClient) {
		if(setCookies  != null){
			BasicCookieStore cookieStore = new BasicCookieStore();
			BasicClientCookie copy = null;
			for (Cookie cookie : setCookies) {
				copy = new BasicClientCookie(cookie.getName() ,cookie.getValue());
				copy.setDomain(cookie.getDomain());
				copy.setExpiryDate(cookie.getExpiry());
				copy.setPath(cookie.getPath());
				cookieStore.addCookie(copy);
			}
			httpClient.setCookieStore(cookieStore);
		}
		
	}

	@Override
	public void onQuit(int index, ZhongHttpClient httpClient) {
	}

}
