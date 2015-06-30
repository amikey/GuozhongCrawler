package com.guozhong.page;

import org.openqa.selenium.WebElement;

import com.guozhong.downloader.driverpool.DriverPoolInterface;
import com.guozhong.request.PageRequest;


/**
 * Contract for {@link com.funhigh.page.WePage.crawler.Page}s factory.
 */
public interface PageFactory {
	
	Page buildOkPage(PageRequest request,Status status, String content, WebElement root, DriverPoolInterface driverPool,int driverIndex );

	Page buildErrorPage(PageRequest request, Status error, DriverPoolInterface drivePool,int driverIndex );

	Page buildRetryPage(PageRequest request,DriverPoolInterface drivePool,int driverIndex);
}
