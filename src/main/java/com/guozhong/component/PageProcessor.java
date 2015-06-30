package com.guozhong.component;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;












import com.guozhong.model.Proccessable;
import com.guozhong.page.OkPage;
import com.guozhong.page.Page;
import com.guozhong.request.BasicRequest;
import com.guozhong.request.PageRequest;
import com.guozhong.request.StartContext;

/**
 * PageProcessor是所有PageRequest请求完成后处理的接口。
 * 例如：startContext.createPageRequest("http://my.oschina.net/u/1377701/blog/425984",OschinaProcessor.class)即可指定处理页面结果
 * 此时重写OschinaProcessor.process方法即可完成解析工作
 * @author 郭钟
 *
 */
public interface PageProcessor {
	
	/**
	 * 如果这个页面需要动态交互JS，定义一个PageScript返回
	 * @return
	 */
	public PageScript getJavaScript();
	
	/**
	 * 当启动代理Ip访问时需要重写此方法，返回正常网页应该带有的字符串标识。比如www.baidu.com带有“百度”
	 * @return
	 */
	public Pattern getNormalContain();
	
	/**
	 * 处理一个页面
	 * @param page  下载完成的网页
	 * @param context  当前所有入口的上下文对象
	 * @param queue  加入跟进Request的List容器，处理完成后queue的所有Request会被推送到抓取队列中
	 * @throws Exception 
	 */
	public void process(OkPage page,StartContext context,List<BasicRequest> queue,List<Proccessable> objectContainer)throws Exception; 
	
	/**
	 * 处理错误页面
	 * @param page
	 * @param context
	 */
	public void processErrorPage(Page page,StartContext context)throws Exception;
}


