package guozhong.zhiyou.prcessor;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.guozhong.component.PageProcessor;
import com.guozhong.component.PageScript;
import com.guozhong.downloader.impl.JavaScriptDriver;
import com.guozhong.model.Proccessable;
import com.guozhong.page.OkPage;
import com.guozhong.page.Page;
import com.guozhong.request.BasicRequest;
import com.guozhong.request.PageRequest;
import com.guozhong.request.StartContext;
import com.guozhong.request.PageRequest.PageEncoding;
import com.guozhong.util.JavaScriptUtil;

public class PageCompanyDescript implements PageProcessor {
	@Override
	public PageScript getJavaScript() {
		return new PageScript() {
			
			@Override
			public void executeJS(JavaScriptDriver driver,List<Proccessable> objectContainer) throws Exception {
				WebElement element = driver.findElement(By.id("companyH1"));
				driver.executeScript("arguments[0].innerHTML='WebDriverDownloader支持执行JavaScript';", element);
			}
		};
	}

	@Override
	public Pattern getNormalContain() {
		return null;
	}
	
	@Override
	public void process(OkPage page, StartContext context,List<BasicRequest> queue, List<Proccessable> objectContainer)
			throws Exception {
		System.out.println("源码在这里，你可以在这里解析出信息喽");
		Document doc = Jsoup.parse(page.getContent());
		Element h1 = doc.select("h1[id=companyH1]").first();
		if(h1 != null){
			System.out.println("公司全称:"+h1.text());
		}
	}

	@Override
	public void processErrorPage(Page arg0, StartContext arg1)
			throws Exception {
	}

}
