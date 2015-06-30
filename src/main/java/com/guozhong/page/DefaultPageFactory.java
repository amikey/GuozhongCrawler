package com.guozhong.page;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;

import com.guozhong.CrawlTask;
import com.guozhong.downloader.driverpool.DriverPoolInterface;
import com.guozhong.request.PageRequest;


/**
 * Default implementation for {@link PageFactory}.
 */
public class DefaultPageFactory implements PageFactory {
	private static Logger logger = Logger.getLogger(DefaultPageFactory.class);

	@Override
	public Page buildOkPage(PageRequest request,Status status, String content, WebElement root, DriverPoolInterface driverPool,int driverIndex ) {
		return new OkPage(request, status, content,root , driverPool, driverIndex);
	}

	@Override
	public Page buildErrorPage(PageRequest request, Status error, DriverPoolInterface drivePool,int driverIndex ) {
		return new ErrorPage(request, error,drivePool,driverIndex ); 
	}

	@Override
	public Page buildRetryPage(PageRequest request,DriverPoolInterface drivePool,int driverIndex) {
		return new RetryPage(request,drivePool,driverIndex);
	}
}
