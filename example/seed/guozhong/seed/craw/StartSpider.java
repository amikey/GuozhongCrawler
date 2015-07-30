package guozhong.seed.craw;

import guozhong.seed.processor.BaikeHomeProcessor;
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
	 * @param args
	 */
	public static void main(String[] args) {
		         
		String bingbing = "http://baike.baidu.com/link?url=Iy_93jslm_AvYfWXAQNAmnr69bqx1-SdgH1MiHjJU6GbXFkVYSqQw4FZxS2OhcfxZFzmw5_MvedFq6MUNt8RBK";//范冰冰百科
		String tangwei = "http://baike.baidu.com/view/355019.htm";//汤唯百科
	    CrawTaskBuilder builder  = CrawlManager.getInstance()
		.prepareCrawlTask("人物百科爬虫", DefaultPageDownloader.class)
		.useThread(2)//使用两个线程下载
		.injectStartUrl(bingbing, BaikeHomeProcessor.class)
		.injectStartUrl(tangwei, BaikeHomeProcessor.class)
		.usePageEncoding(PageEncoding.UTF8);
		CrawlTask spider = builder.build();
		CrawlManager.getInstance().start(spider);
		
	}
	
	
}
