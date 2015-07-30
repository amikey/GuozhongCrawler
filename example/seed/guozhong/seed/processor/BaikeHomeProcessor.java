package guozhong.seed.processor;

import guozhong.seed.model.BaiKe;

import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.guozhong.component.PageProcessor;
import com.guozhong.component.PageScript;
import com.guozhong.model.Proccessable;
import com.guozhong.page.OkPage;
import com.guozhong.page.Page;
import com.guozhong.request.BasicRequest;
import com.guozhong.request.StartContext;

public class BaikeHomeProcessor implements PageProcessor {
	@Override
	public PageScript getJavaScript() {
		return null;
	}

	@Override
	public Pattern getNormalContain() {
		return null;
	}

	@Override
	public void process(OkPage page, StartContext context,
			List<BasicRequest> queue, List<Proccessable> objectContainer)
			throws Exception {
		BaiKe baike = new BaiKe();
		Document doc = Jsoup.parse(page.getContent());
		String name = doc.select("h1").text();
		baike.setName(name);
		System.out.println(name + " 抓取完成");
		System.out.println(context.hashCode());
	}

	@Override
	public void processErrorPage(Page page, StartContext context)
			throws Exception {
		// TODO Auto-generated method stub

	}

}
