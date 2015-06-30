package com.guozhong.page;

import java.util.ArrayList;
import java.util.List;

import com.guozhong.downloader.driverpool.DriverPoolInterface;
import com.guozhong.downloader.impl.ZhongHttpClient;
import com.guozhong.request.PageRequest;


/**
 * @author jonasabreu
 * 
 */
final public class ErrorPage extends Page {

	    private final PageRequest request;
	    private final Status error;
	    public ErrorPage(final PageRequest request, final Status error,final DriverPoolInterface driverPool,int driverIndex ) {
	        this.request = request;
	        this.error = error;
	        this.driverPool = driverPool;
	        this.driverIndex = driverIndex;
	    }


	    public String getContent() {
	        return "";
	    }


	    public Status getStatus() {
	        return this.error;
	    }

		@Override
		public PageRequest getRequest() {
			return this.request;
		}


		@Override
		public Object getRequestAttribute(String attribute) {
			return request.getAttribute(attribute);
		}



}
