package com.guozhong.downloader.driverpool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.openqa.selenium.WebDriver;

import com.guozhong.component.listener.HttpClientLifeListener;
import com.guozhong.downloader.impl.HttpClientFactory;
import com.guozhong.downloader.impl.ZhongHttpClient;
import com.guozhong.exception.DriverCreateException;
import com.guozhong.listener.SetCookieHttpClientLifeListener;

/**
 * @author 郭钟
 * Date: 15-5-26 <br>
 * Time: 下午1:41 <br>
 */
public final class HttpClientPool extends DriverPoolInterface{
	
	
    private List<HttpClientLifeListener> httpClientLifeListeners = new ArrayList<HttpClientLifeListener>();
    
    /**
     * 是否是登录模式
     */
    private boolean loginMode = false;
    
    private HttpClientFactory httpClientFactory = new HttpClientFactory();

    /**
     * 统计用过的webDriverList。好释放
     *   
     * */
    private List<ZhongHttpClient> httpClientList = Collections.synchronizedList(new ArrayList<ZhongHttpClient>());
    /**
     * store webDrivers available
     */
    private LinkedBlockingQueue<ZhongHttpClient> queue = new LinkedBlockingQueue<ZhongHttpClient>();
    

    public HttpClientPool() {
    }


    /**
     * 从池中取得一个DefaultHttpClient
     * @return
     * @throws InterruptedException
     */
    public final ZhongHttpClient get() throws InterruptedException {
    	if(loginMode){
    		return httpClientList.get(0);
    	}
    	ZhongHttpClient poll = null;
    	if(httpClientList.size() < min_drivers){
    		synchronized (httpClientList) {
    			if(httpClientList.size() < min_drivers){
    				createSimpleHttpClient();
    			}
    		}
    	}
    	poll = queue.poll();
        if (poll != null && !getIndexs.contains(poll.getIndex())) {
            return poll;
        }
        if (httpClientList.size() < max_drivers) {//如果webDriver使用的数量美誉达到capacity则继续创建webDriver
            synchronized (httpClientList) {
                if (httpClientList.size() < max_drivers) {
                	createSimpleHttpClient();
                }
            }
        }
        return queue.take();//此方法并不保证立即返回WebDriver，有可能等待之前的WebDriver执行完回到pool
    }

    /**
     * 创建核心实例
     */
	private final void createSimpleHttpClient(){
		if(loginMode){
			httpClientFactory.CREATE_MODE = HttpClientFactory.USE_HTTPS_HTTPCLIENT;
		}
		ZhongHttpClient poll;
		int driverIndex = httpClientList.size() ;
		poll = new ZhongHttpClient(httpClientFactory);
		poll.setIndex(driverIndex);
		invokeListener(poll, true);
		queue.add(poll);
		httpClientList.add(poll);
	}

    public final void returnToPool(ZhongHttpClient httpClient) {//将HttpClient添加到pool   	
    	if(!getIndexs.contains(httpClient.getIndex())){//被取得出去的driver不能回到队列   		 
    		queue.add(httpClient);
    	}
    }
    
    /**
     * 打开
     */
    public final void open(){
    }
    
    /**
     * 关闭DefaultHttpClient
     */
    @SuppressWarnings("deprecation")
	public final void closeAll() {
    	for (ZhongHttpClient client : httpClientList) {
    		invokeListener(client, false);
//    		try {
//				client.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		}
        //httpClientList.clear();
        //queue.clear();
    }
    
    public final void setPageLoadTimeout(int timeout){
    }
    
	public void addHttpClientLifeListener(HttpClientLifeListener httpClientLifeListener) {
		if(!httpClientLifeListeners.contains(httpClientLifeListener)){
			httpClientLifeListeners.add(httpClientLifeListener);
			if(httpClientLifeListener instanceof SetCookieHttpClientLifeListener){
				loginMode = true;//检测到有cookie设置，那么自动设置为登录模式。所有的请求使用同一个实例
				createSimpleHttpClient();//创建一个实例
			}
		}
	}
	
	public boolean removeHttpClientLifeListener(HttpClientLifeListener httpClientLifeListener){
		int index = httpClientLifeListeners.indexOf(httpClientLifeListener);
		if(index != -1){
			httpClientLifeListeners.remove(httpClientLifeListener);
		}
		return index != -1;
	}
	
	private void invokeListener(ZhongHttpClient httpClient , boolean isCreate){
		for (HttpClientLifeListener httpClientLifeListener : httpClientLifeListeners) {
			if(isCreate){
				httpClientLifeListener.onCreated(httpClient.getIndex(), httpClient);
			}else{
				httpClientLifeListener.onQuit(httpClient.getIndex(), httpClient);
			}
		}
	}


	@Override
	public Object getDriver(int driverIndex) {
		if(getIndexs.contains(driverIndex)){
			return null;
		}else{
			getIndexs.add(driverIndex);
		}
		for (ZhongHttpClient client : httpClientList) {
			if(client.getIndex() == driverIndex){
				queue.remove(client);//队列移除实例。防止处理未完成之前使用
				return client;
			}
		}
		return null;
	}


	@Override
	public void handleComplete(Object driver) {
		ZhongHttpClient httpClient = (ZhongHttpClient) driver;
		getIndexs.remove(httpClient.getIndex());//清除限制
    	queue.add(httpClient);//回到队列
	}
	
}
