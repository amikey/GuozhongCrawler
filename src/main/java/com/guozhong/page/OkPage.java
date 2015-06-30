/**
 * 
 */
package com.guozhong.page;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebElement;

import com.guozhong.downloader.driverpool.DriverPoolInterface;
import com.guozhong.downloader.impl.ZhongHttpClient;
import com.guozhong.request.PageRequest;


/**
 * OKPage代表状态码200-299直接的响应页面
 * @author 郭钟 
 * @QQ群  202568714
 *
 */
public class OkPage extends Page {

	    private final PageRequest request;
	    private final Status status;
	    private final String content ; 
	    private final WebElement root;
	   
	   

	    public OkPage(final PageRequest request, final Status status, String content, WebElement root,final DriverPoolInterface driverPool,int driverIndex ) {
	        this.request = request;
	        this.status = status;
	        this.content = content;
	        this.root = root;
	        this.driverPool = driverPool;
	        this.driverIndex = driverIndex;
	    }


	    public String getContent() {
	        return this.content;
	    }


	    public Status getStatus() {
	        return status;
	    }
	    
		@Override
		public PageRequest getRequest() {
			return this.request;
		}
		
		public WebElement getRoot(){
			return this.root;
		}
		
		@Override
		public String toString() {
			return "OkPage [status=" + status.name() +",url:"+request.getUrl()+ ", content=" + content + "]";
		}
		
		@Override
		public Object getRequestAttribute(String attribute) {
			return request.getAttribute(attribute);
		}
		
		
		
}
