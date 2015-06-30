package com.guozhong.page;

import org.openqa.selenium.WebElement;

import com.guozhong.downloader.driverpool.DriverPoolInterface;
import com.guozhong.request.PageRequest;

/**
 * 所有页面的抽象表示
 * 
 * @author 郭钟
 * @QQ群 202568714
 *
 */
public abstract class Page {

	protected DriverPoolInterface driverPool;
	protected int driverIndex;
	protected Object driver;
	protected int statusCode;

	public abstract String getContent();

	public abstract Status getStatus();

	public abstract PageRequest getRequest();

	public abstract Object getRequestAttribute(String attribute);

	public int getStatusCode() {
		return statusCode;
	};

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	
	/**
	 * 是否需要将driver放回队列
	 */
	public boolean isNeedPost() {
		return driver != null;
	};

	protected int getDriverIndex() {
		return driverIndex;
	}

	public Object getRequestDriver() {
		driver = driverPool.getDriver(driverIndex);
		return driver;
	}

	/**
	 * 将driver放回队列
	 */
	public void handleComplete() {
		if (isNeedPost()) {
			if (!handleComplete) {
				driverPool.handleComplete(driver);
				handleComplete = true;
			}
		}
	}

	private boolean handleComplete = false;
}
