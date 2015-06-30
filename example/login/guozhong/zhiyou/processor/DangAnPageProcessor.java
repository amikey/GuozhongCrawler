package guozhong.zhiyou.processor;

import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.guozhong.component.PageProcessor;
import com.guozhong.component.PageScript;
import com.guozhong.model.Proccessable;
import com.guozhong.page.OkPage;
import com.guozhong.page.Page;
import com.guozhong.request.BasicRequest;
import com.guozhong.request.StartContext;

public class DangAnPageProcessor implements PageProcessor {

	@Override
	public PageScript getJavaScript() {
		return null;
	}

	@Override
	public Pattern getNormalContain() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void process(OkPage page, StartContext context,
			List<BasicRequest> queue, List<Proccessable> objectContainer)
			throws Exception {
		System.out.println("-------------");
		System.out.println("http://www.jobui.com/settings/profile/modify/");
		System.out.println("-------------");
		System.out.println("源码：");
		System.out.println(page.getContent());
	}

	@Override
	public void processErrorPage(Page page, StartContext context)
			throws Exception {
		// TODO Auto-generated method stub

	}

}
