package guozhong.zhiyou.craw;

import guozhong.zhiyou.prcessor.ExecutableJavaScriptProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

















import com.guozhong.CrawTaskBuilder;
import com.guozhong.CrawlManager;
import com.guozhong.CrawlTask;
import com.guozhong.component.DynamicEntrance;
import com.guozhong.downloader.impl.ChromeDriverDownloader;
import com.guozhong.downloader.impl.DefaultPageDownloader;
import com.guozhong.downloader.impl.WebDriverDownloader;
import com.guozhong.request.PageRequest;
import com.guozhong.request.StartContext;
import com.guozhong.request.PageRequest.PageEncoding;


public class StartSpider {

	/**
	 * 这是一个职友网抓取公司信息的例子
	 * @param args
	 */
	public static void main(String[] args) {
		//设置chromedriver.exe路径
		System.setProperty("webdriver.chrome.driver", "D:\\program files (x86)\\Chrome\\chromedriver.exe");
		         
		String alibaba = "http://www.jobui.com/company/281097/";
		String taobao = "http://www.jobui.com/company/593687/";
	    CrawTaskBuilder builder  = CrawlManager.getInstance()
		.prepareCrawlTask("职友网爬虫", ChromeDriverDownloader.class)
		.useThread(2)//使用两个线程下载
		.injectStartUrl(alibaba, ExecutableJavaScriptProcessor.class)
		.injectStartUrl(taobao, ExecutableJavaScriptProcessor.class)
		.usePageEncoding(PageEncoding.UTF8);
		CrawlTask spider = builder.build();
		CrawlManager.getInstance().start(spider);
	}
	
}
