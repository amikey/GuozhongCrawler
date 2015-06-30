package com.guozhong.component;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.guozhong.downloader.impl.JavaScriptDriver;
import com.guozhong.model.Proccessable;

public  interface  PageScript {
	/**
	 * 在这里执行你的JS代码。
	 * @param driver  这个JavaScriptDriver是WebDriver最基本的实现。覆盖WebDriver所有方法
	 * @throws Exception
	 */
	public void executeJS(JavaScriptDriver driver,List<Proccessable> objectContainer)throws Exception;
}
