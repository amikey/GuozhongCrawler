package com.guozhong.downloader;

import com.guozhong.component.PageProcessor;
import com.guozhong.component.PageScript;

public interface JavaScriptDownloader {

	public void addJavaScriptFunction(Class<? extends PageProcessor> processorCls,PageScript javaScript);
	
	/**
	 * 取得当前页面需要执行的javaScript函数
	 * @param request
	 * @return
	 */
	public PageScript findPageScripts(Class<? extends PageProcessor> processorCls);
}
