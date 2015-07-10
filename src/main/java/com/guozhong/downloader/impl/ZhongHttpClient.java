package com.guozhong.downloader.impl;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import com.guozhong.downloader.driverpool.HttpClientPool;

/**
 * 扩展的HttpClient。增加对cookie的控制
 * @author 郭钟 
 * @QQ群  202568714
 *
 */
public final class ZhongHttpClient{
	
	/**
	 * 实例池中的编号
	 */
	private int index;
	
	private CloseableHttpClient core ;
	
	private BasicCookieStore cookieStore ;
	
	private HttpClientFactory httpClientFactory;
	
	public ZhongHttpClient(HttpClientFactory httpClientFactory){
		this.httpClientFactory = httpClientFactory;
	}
	
	
	
	public int getIndex() {
		return index;
	}



	public void setIndex(int index) {
		this.index = index;
	}



	public void setCookieStore(BasicCookieStore cookieStore){
		if(cookieStore == null){
			throw new NullPointerException();
		}
		this.cookieStore = cookieStore;
		core = httpClientFactory.buildDefaultHttpClient(cookieStore);
		
	}
	
	
	
	public CloseableHttpClient getCore() {
		return core;
	}

	public BasicCookieStore getCookieStore() {
		return cookieStore;
	}

	private final void checkInit(){
		if(core == null){
			this.cookieStore = new BasicCookieStore();
			core = httpClientFactory.buildDefaultHttpClient(cookieStore);
		}
	}

	public final void close() throws IOException {
		core.close();
	}

	public final HttpResponse execute(HttpRequestBase method) throws ClientProtocolException, IOException {
		checkInit();
		return core.execute(method);
	} 
	
}
