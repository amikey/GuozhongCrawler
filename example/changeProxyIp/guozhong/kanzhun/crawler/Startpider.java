package guozhong.kanzhun.crawler;

import guozhong.kanzhun.pageprocessor.CountAbleOKPageProccessor;
import guozhong.kanzhun.proxy.KanzhunProxyIpPool;

import java.util.Arrays;
import java.util.List;



import com.guozhong.CrawTaskBuilder;
import com.guozhong.CrawlManager;
import com.guozhong.CrawlTask;
import com.guozhong.component.DynamicEntrance;
import com.guozhong.downloader.impl.DefaultPageDownloader;
import com.guozhong.request.PageRequest.PageEncoding;
import com.guozhong.request.StartContext;

public class Startpider {

	public static void main(String[] args) {
		CrawTaskBuilder builder = CrawlManager.getInstance()
		.prepareCrawlTask("看准网测试切换代理IP", DefaultPageDownloader.class)
		.useThread(100)// 使用多个线程下载
		.useDynamicEntrance(DynamicEntranceImpl.class)
		.useProxyIpPool(KanzhunProxyIpPool.class, 100, 1000 * 60 * 20, 30)//参数含义：KanzhunProxyIpPool.class是代理IP提供者。每次加载代理100个。代理过期时间（一般来说所有的代理都不是永久有效的）这里是20分钟。有效代理重复使用次数30次
		.usePageEncoding(PageEncoding.UTF8);
		CrawlTask spider = builder.build();
		CrawlManager.getInstance().start(spider);
	}
	
	public static final class DynamicEntranceImpl extends DynamicEntrance{

		@Override
		public List<StartContext> loadStartContext() {
			StartContext context = new StartContext();
			//注入2W个种子URL
			for (int i = 0; i < 20000; i++) {
				context.injectSeed(context.createPageRequest("http://www.kanzhun.com/companyl/search/?ka=banner-com", CountAbleOKPageProccessor.class));//公司
			}
			return Arrays.asList(context);
		}
		
	}
}
