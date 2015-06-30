package guozhong.zhiyou.login;

import guozhong.zhiyou.processor.DangAnPageProcessor;

import java.util.HashSet;
import java.util.Set;

import javax.swing.text.ZoneView;

import com.guozhong.CrawTaskBuilder;
import com.guozhong.CrawlManager;
import com.guozhong.CrawlTask;
import com.guozhong.downloader.impl.DefaultPageDownloader;
import com.guozhong.downloader.impl.ZhongChromeDriver;
import com.guozhong.request.Cookie;
import com.guozhong.request.PageRequest.PageEncoding;

public class StartSpider {

	/**
	 * 这是一个登录职友网后抓取档案页的例子。
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args)throws Exception {
		Set<Cookie> cookies = new HashSet<Cookie>();//构造一个容器装cookie的
		
		
		System.setProperty("webdriver.chrome.driver", "D:\\program files (x86)\\Chrome\\chromedriver.exe");
		ZhongChromeDriver zhongChromeDriver = new ZhongChromeDriver();
		zhongChromeDriver.get("http://www.jobui.com/");
		Thread.sleep(30 * 1000);//请在1分钟内完成登录操作
		zhongChromeDriver.dump(cookies);//cookie dump到集合中
		zhongChromeDriver.quit();//退出浏览器
		
		
		CrawTaskBuilder builder  = CrawlManager.getInstance()
		.prepareCrawlTask("职友网爬虫", DefaultPageDownloader.class);
		 builder.useQueueDelayedPriorityRequest(500)
		.useThread(2)
		.usePageEncoding(PageEncoding.UTF8)
		.useCookie(cookies)//将容器里装的cookie丢到框架中去
		.injectStartUrl("http://www.jobui.com/settings/profile/modify/", DangAnPageProcessor.class);
		 CrawlTask spider = builder.build();
		 CrawlManager.getInstance().start(spider);
	}

}
