package com.guozhong.request;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.guozhong.component.Pipeline;
import com.guozhong.component.TransactionCallBack;
import com.guozhong.model.Proccessable;
import com.guozhong.util.ProccessableUtil;

/**
 * 为了实现和维护并发抓取的属性信息提供线程安全的事务请求。TransactionRequest是一个抽象类自己不能设置Processor，却需要实现
 * TransactionCallBack接口。TransactionRequest是个复合的BasicRequest。他可以将多个PageRequest、BinaryRequest甚至TransactionRequest
 * 自己的对象添加到child集合中，在下载过程中首先下载TransactionRequest中的所有childRequest，每个childRequest下载完成后使用notify方式逐步向上通知，
 * 直到所有的child下载完成TransactionRequest回调 TransactionCallBack的callBack方法通知业务层这个TransactionRequest下载完成。
 * 
 * @author 郭钟 
 * @QQ群  202568714
 *
 */
public abstract class TransactionRequest extends BasicRequest implements TransactionCallBack{
	
	/**
	 * 事务处理可以含有多个Request
	 */
	private List<BasicRequest> multiRequests = new ArrayList<BasicRequest>();
	
	/**
	 * 标记所有的PageRequest是否都已完成请求和处理
	 */
	private ConcurrentHashMap<Integer, Boolean> successFlag = new ConcurrentHashMap<Integer, Boolean>();
	
	private Pipeline pipeline;
	
	public TransactionRequest(){
		type = Type.TRANSACTION_REQUEST;
	}

	public Pipeline getPipeline() {
		return pipeline;
	}

	public void setPipeline(Pipeline pipeline) {
		this.pipeline = pipeline;
	}

	@Override
	public BasicRequest addAttribute(String attribute, Object value) {
		Iterator<BasicRequest> iter = multiRequests.iterator();
		BasicRequest basicRequest = null;
		while(iter.hasNext()){
			basicRequest = iter.next();
			basicRequest.addAttribute(attribute, value);
		}
		return this;
	}
  
	@Override
	public Object getAttribute(String attribute) {
		Object value = null;
		Iterator<BasicRequest> iter = multiRequests.iterator();
		BasicRequest basicRequest = null;
		while(iter.hasNext()){
			basicRequest = iter.next();
			value = basicRequest.getAttribute(attribute);
			if(value != null)
				break;
		}
		return value;
	}
	
	/**
	 * 添加一个BasicRequest到TransactionRequest的child中
	 * @param request
	 */
	public void addChildRequest(BasicRequest request){
		if(request.hashCode() == this.hashCode()){
			return;
		}
		request.setParentRequest(this);
		successFlag.put(request.hashCode(), false);
		multiRequests.add(request);
	}
	
	/**
	 * 返回这个TransactionRequest所有child的迭代器
	 * @return
	 */
	public Iterator<BasicRequest> iteratorChildRequests(){
		return multiRequests.iterator();
	}

	@Override
	public void notify(int hashcode) {
		if(successFlag.containsKey(hashcode)){
			successFlag.put(hashcode, true);
			checkComplete();
		}else{
			throw new RuntimeException("not found hashcode :"+hashcode);
		}
	}
	
	/**
	 * 检查是否所有的Request标记都不是false。如果是那么所有的Request已经请求完成和处理。
	 * 
	 */
	public void checkComplete(){
		if(!successFlag.containsValue(false)){
			List<Proccessable> objectContainer = ProccessableUtil.buildProcceableList();
			try {
				callBack(this, objectContainer);
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				if(null != pipeline){
					pipeline.proccessData(objectContainer);
				}
				if(parentRequest != null){
					parentRequest.notify(this.hashCode());
				}
			}
		}
	}
	
	
}
