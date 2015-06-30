package com.guozhong.util;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.guozhong.downloader.impl.JavaScriptDriver;

public final class JavaScriptUtil {
	
	/**
	 * 模拟点击工具方法
	 * @param driver
	 * @param ele  按钮组件的WebElement对象
	 * @return
	 */
	public static final Object click(JavaScriptDriver driver,WebElement ele){
		Object o = driver.executeScript("arguments[0].click();", ele);
		try {
			Thread.sleep(1000);//执行js后是默认等1S 保证html源码重新被加载
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return o;
	}

}
