package com.guozhong.component.listener;

import com.guozhong.downloader.impl.ZhongWebDriver;


public interface WebDriverLifeListener {
	/**
	 * 监听创建GuoWebDriver实例
	 * 每创建好一个则回调如下方法
	 */
	public void onCreated(int index,ZhongWebDriver webDriver);
	
	
	/**
	 * 监听关闭GuoWebDriver实例
	 */
	public void onQuit(int index,ZhongWebDriver webDriver);
}
