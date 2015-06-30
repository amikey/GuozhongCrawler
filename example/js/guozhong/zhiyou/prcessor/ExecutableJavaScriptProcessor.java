package guozhong.zhiyou.prcessor;

import java.util.List;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.guozhong.component.PageProcessor;
import com.guozhong.component.PageScript;
import com.guozhong.downloader.impl.JavaScriptDriver;
import com.guozhong.model.Proccessable;
import com.guozhong.page.OkPage;
import com.guozhong.page.Page;
import com.guozhong.request.BasicRequest;
import com.guozhong.request.StartContext;

public class ExecutableJavaScriptProcessor implements PageProcessor{

	@Override
	public PageScript getJavaScript() {
		return new PageScript() {
			
			@Override
			public void executeJS(JavaScriptDriver driver,
					List<Proccessable> objectContainer) throws Exception {
				    WebElement element = driver.findElement(By.id("companyH1"));
	                driver.executeScript("arguments[0].innerHTML='WebDriverDownloader支持执行JavaScript';", element);
			}
		};
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processErrorPage(Page page, StartContext context)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}
