package com.guozhong.request;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.guozhong.component.PageProcessor;
import com.guozhong.downloader.impl.DefaultFileDownloader;
import com.guozhong.model.Proccessable;


/**
 * 网页类型Request的表示。
 * @author 郭钟 
 * @QQ群  202568714
 *
 */
public class PageRequest extends AttributeRequest{
	/**
	 * 方法类型
	 * @author Administrator
	 *
	 */
	public enum Method{
    	GET,
    	POST;
    }
	
	/**
	 * 网页编码
	 */
	public enum PageEncoding{
    	UTF8,
    	GBK,
    	GB2312,
  //  	AUTO;
    }

	private String url;

    private Method method;
    
    private PageEncoding pageEncoding;
    
    
    /**
     * request的参数
     */
    private HashMap<String, String> requestParams = null;
    
    /**
     * 请求头
     */
    private HashMap<String,String> headers = null;
    
    /**
     * 标记一类请求
     */
    private Class<? extends PageProcessor> processorClass ;
    
    
    /**
     * 是否使用DefaultDownload请求
     */
    private boolean isDefaultDownload;
    
    protected PageRequest(){
    	type = type.PAGE_REQUEST;
    	method = Method.GET;
    }
    
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		if(url == null){
			throw new NullPointerException();
		}else{
			this.url = url;
		}
	}


	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}


	public void putParams(String name,String value){
		iniParmaContainer();
    	if(name != null&&value!=null){
    		requestParams.put(name, value);
    	}
    }

	private void iniParmaContainer() {
		if(requestParams == null){
			requestParams = new HashMap<String, String>();
		}
	}
    
    public Set<Entry<String, String>> getParams(){
    	iniParmaContainer();
    	return this.requestParams.entrySet();
    }
    
    public Object getParamsByName(String name){
    	iniParmaContainer();
    	return this.requestParams.get(name);
    }
    
    private void iniHeadersContainer() {
		if(headers == null){
			headers = new HashMap<String, String>();
		}
	}
    
    public void putHeader(String name,String value){
    	iniHeadersContainer();
    	headers.put(name, value);
    }
    
    public Map<String, String> getHedaers(){
    	iniHeadersContainer();
    	return this.headers;
    }
    
	public Class<? extends PageProcessor> getProcessorClass() {
		return processorClass;
	}

	public void setProcessorClass(Class<? extends PageProcessor> processorCls) {
		if(processorCls == null){
			throw new NullPointerException("Reuqest的PageProcessor不能为Null");
		}else{
			this.processorClass = processorCls;
		}
	}

	public boolean isDefaultDownload() {
		return isDefaultDownload;
	}

	public void setDefaultDownload(boolean defaultDown) {
		this.isDefaultDownload = defaultDown;
	}
	
	public PageEncoding getPageEncoding() {
		return pageEncoding;
	}

	public void setPageEncoding(PageEncoding pageEncoding) {
		if(pageEncoding != null){
			this.pageEncoding = pageEncoding;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PageRequest other = (PageRequest) obj;
		if (method != other.method)
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PageRequest [url=" + url + ", method=" + method
				+ ", pageEncoding=" + pageEncoding + ", requestParams="
				+ requestParams + ", headers=" + headers + ", processorClass="
				+ processorClass + ", isDefaultDownload=" + isDefaultDownload
				+ "]";
	}

}
