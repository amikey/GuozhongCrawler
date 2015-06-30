package guozhong.kanzhun.pageprocessor;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import com.guozhong.component.PageProcessor;
import com.guozhong.component.PageScript;
import com.guozhong.model.Proccessable;
import com.guozhong.page.OkPage;
import com.guozhong.page.Page;
import com.guozhong.request.BasicRequest;
import com.guozhong.request.StartContext;

public class CountAbleOKPageProccessor implements PageProcessor {

	static Pattern normalContain = Pattern.compile("所在城市");//正常页面必定会存在的字符，你也可以使用页面上其他的字符
	static AtomicInteger atomicInteger = new AtomicInteger(0);
	
	@Override
	public PageScript getJavaScript() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pattern getNormalContain() {
		return normalContain;
	}

	@Override
	public void process(OkPage page, StartContext context,
			List<BasicRequest> queue, List<Proccessable> objectContainer)
			throws Exception {
		int count = atomicInteger.incrementAndGet();
		System.out.println("成功了抓取了:"+count+"次");
	}

	@Override
	public void processErrorPage(Page page, StartContext context)
			throws Exception {
	}

}
