package com.guozhong.page;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.guozhong.downloader.driverpool.DriverPoolInterface;
import com.guozhong.downloader.impl.ZhongHttpClient;
import com.guozhong.request.PageRequest;

/**
 * 在请求过程中由于网络问题请求失败的
 * @author 郭钟 
 * @QQ群  202568714
 *
 */
public class RetryPage extends Page {

    private final PageRequest request;
    
    private final DriverPoolInterface driverPool ;
    
    private  int driverIndex ;

    public RetryPage(final PageRequest request,final  DriverPoolInterface driverPool,int driverIndex ) {
        this.request = request;
        this.driverPool = driverPool;
        this.driverIndex = driverIndex;
    }



	@Override
	public PageRequest getRequest() {
		return this.request;
	}



	@Override
	public String getContent() {
		return null;
	}



	@Override
	public Status getStatus() {
		return null;
	}

	public int getRetryCount() {
		return request.getHistoryCount();
	}

	
	@Override
	public Object getRequestAttribute(String attribute) {
		return request.getAttribute(attribute);
	}



}
