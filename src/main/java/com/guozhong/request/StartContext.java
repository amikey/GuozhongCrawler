package com.guozhong.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.guozhong.component.BinaryProcessor;
import com.guozhong.component.PageProcessor;
import com.guozhong.component.TransactionCallBack;
import com.guozhong.model.Proccessable;
import com.guozhong.request.PageRequest.Method;
import com.guozhong.request.PageRequest.PageEncoding;
/**
 *  StartContext是注入时所有seed的上下文信息如果爬虫在抓取过程当中需要共享一些变量。那么可使用StartContext作为容器。
 * @author 郭钟 
 * @QQ群  202568714
 *
 */
public final class StartContext {
	
	/**
	 * 全局属性
	 */
	private final HashMap<String, Object> contextAttribute = new HashMap<String, Object>();
	/**
	 * 定义根url
	 */
	private List<BasicRequest> seeds = new ArrayList<BasicRequest>();
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 构造一个StartContext。通常用来充当seedRequest的容器
	 */
	public StartContext(){}
	
	/**
	 * 构造一个StartContext。并且加入一个种子URL
	 * @param url
	 * @param processorCls
	 */
	public StartContext(String url,Class<? extends PageProcessor> processorCls) {
		this(url, processorCls, null);
	}
	
	
	/**
	 * 构造一个StartContext。并且加入一个种子URL
	 * @param url 
	 * @param processorCls 
	 * @param pageEncoding  URL对应网页的编码
	 */
	public StartContext(String url,Class<? extends PageProcessor> processorCls,PageEncoding pageEncoding) {
		BasicRequest seed = createPageRequest(url, processorCls, 0 ,pageEncoding);
		seeds.add(seed);
	}
	
	
	/**
	 * 创建网页下载请求PageRequest
	 * @param url  这个请求对应的http或者https 地址
	 * @param processorCls 下载完成后处理这个网页Page的PageProcessor的class对象
	 * @return PageRequest
	 */
    public  PageRequest createPageRequest(String url,Class<? extends PageProcessor> processorCls){
    	PageRequest req = new PageRequest();
    	req.setUrl(url);
    	req.setMethod(Method.GET);
    	req.setProcessorClass(processorCls);
    	return req;
    }
    
    /**
     * 创建网页下载请求PageRequest
     * @param url  这个请求对应的http或者https 地址
     * @param processorCls  下载完成后处理这个网页Page的PageProcessor的class对象
     * @param priority   设置这个PageRequest的priority。需要注意的只有使用crawTaskBuilder.useQueuePriorityRequest或者crawTaskBuilder.useQueueDelayedPriorityRequest的时候
     * priority才会起作用并排序。
     * @param pageEncoding  这个PageRequest对应URL的网页编码格式。如果不指定那么会用crawTaskBuilder中指定的usePageEncoding。如果crawTaskBuilder没有
     * 使用usePageEncoding。则默认用UTF-8编码
     * @return PageRequest
     */
    public  PageRequest createPageRequest(String url,Class<? extends PageProcessor> processorCls,int priority,PageEncoding pageEncoding){
    	if(priority >=0 && priority<=1000){
    		PageRequest req = new PageRequest();
        	req.setUrl(url);
        	req.setMethod(Method.GET);
        	req.setPriority(priority) ;
        	req.setProcessorClass(processorCls);
        	req.setPageEncoding(pageEncoding);
        	return req;
    	}else{
    		throw new IllegalArgumentException("priority的值必须在0-1000之间");
    	}
    }
    
    /**
     * 创建网页下载请求PageRequest
     * @param url  这个请求对应的http或者https 地址
     * @param processorCls  下载完成后处理这个网页Page的PageProcessor的class对象
     * @param priority  设置这个PageRequest的priority。需要注意的只有使用crawTaskBuilder.useQueuePriorityRequest或者crawTaskBuilder.useQueueDelayedPriorityRequest的时候
     * priority才会起作用并排序。
     * @return PageRequest
     */
    public  PageRequest createPageRequest(String url,Class<? extends PageProcessor> processorCls,int priority){
    	if(priority >=0 && priority<=1000){
    		PageRequest req = new PageRequest();
    		req.setUrl(url);
    		req.setMethod(Method.GET);
    		req.setPriority(priority) ;
    		req.setProcessorClass(processorCls);
    		return req;
    	}else{
    		throw new IllegalArgumentException("priority的值必须在0-1000之间");
    	}
    }
    
    /**
     * 创建一个二进制下载请求
     * @param url 这个请求对应的http或者https 地址
     * @param processorCls  文件下载时处理这个InputStream的BinaryProcessor的class对象
     * @return BinaryRequest
     */
    public BinaryRequest createBinaryRequest(String url,Class<? extends BinaryProcessor> processorCls){
    	BinaryRequest req = new BinaryRequest(url, processorCls);
    	return req;
    }
    
    /**
     * 创建支持事务的下载请求。
     * @param transactionCallBack   事务完成后的回调接口的Class
     * @return TransactionRequest
     */
    public TransactionRequest createTransactionRequest(final TransactionCallBack transactionCallBack){
    	TransactionRequest req = new TransactionRequest() {
			
			@Override
			public void callBack(TransactionRequest transactionRequest,List<Proccessable> objectContainer) throws Exception {
				if(transactionCallBack != null){
					transactionCallBack.callBack(transactionRequest, objectContainer);
				}
			}
		};
    	return req;
    }
    
    /**
     *  给定一个child集合创建支持事务的下载请求。
     * @param transactionCallBack 事务完成后的回调接口
     * @param child child集合
     * @return TransactionRequest
     */
    public TransactionRequest createTransactionRequest(final TransactionCallBack transactionCallBack,BasicRequest ... child){
    	TransactionRequest req = new TransactionRequest() {
    		
    		@Override
    		public void callBack(TransactionRequest transactionRequest,List<Proccessable> objectContainer) throws Exception {
    			transactionCallBack.callBack(transactionRequest, objectContainer);
    		}
    	};
    	if(child != null && child.length != 0){
    		for (int i = 0; i < child.length; i++) {
    			req.addChildRequest(child[i]);
			}
    	}
    	return req;
    }
    

	/**
	 * 注入种子
	 * @param request
	 */
	public void injectSeed(BasicRequest request){
		this.seeds.add(request);
	}
	
	/**
	 * 返回该StartContext所包含的所有种子URL
	 * @return
	 */
	public List<BasicRequest> getSeedRequests(){
		return this.seeds;
	}

	/**
	 * 返回attribute对应的value 这个方法是线程安全的
	 * @param key
	 * @return  返回attribute对应的value
	 */
	public  Object getContextAttribute(String attribute){
		Object value;
		synchronized (contextAttribute) {
			value = contextAttribute.get(attribute);
		}
		return value;
	}
	
	/**
	 * 向StartContext域put一个属性值。并返回之前的attribute对应的value。如果之前没有attribute属性那么返回null。这个方法是线程安全的
	 * @param attribute
	 * @param value
	 * @return 返回之前的attribute对应的value。如果之前没有attribute属性那么返回null
	 */
	public Object putContextAttribute(String attribute, Object value) {
		synchronized (contextAttribute) {
			contextAttribute.put(attribute, value);
		}
		return value;
	}
	
	/**
	 * 返回种子URL的个数
	 * @return
	 */
	public int getSeedSize(){
		return seeds.size();
	}
	
	/**
	 * 返回StartContext是否为空。
	 * @return
	 */
	public boolean isEmpty(){
		return seeds.isEmpty();
	}
}
