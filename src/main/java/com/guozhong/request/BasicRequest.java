package com.guozhong.request;

import java.io.Serializable;


/**
 * 所有Request都直接或间接的继承自BasicRequest。BasicRequest是所有Request的父类
 * 基本的实现有
 * 1、priority优先级设置，在0-1000之间。值越大越先被请求
 * 2、requestCount记录Request总共被请求了多少次
 * 3、实现Comparable接口。可排序，和priority相关
 * 
 * @author 郭钟 
 * @QQ群  202568714
 *
 */
public abstract class BasicRequest implements Comparable<BasicRequest> ,Serializable{
	
	/**
	 * 请求类型
	 * @author Administrator
	 *
	 */
	public enum Type{
    	PAGE_REQUEST,
    	TRANSACTION_REQUEST,
    	BINARY_REQUEST;
    }
	
	protected Type type;
	
	/**
     * 父节点的Request
     */
    protected BasicRequest parentRequest;
	
	private int priority = 0; 
	
	/**
     * 记录Request被发送的次数
     */
    private int requestCount = 0;
    

	public Type getType() {
		return type;
	}
	 
    public int getPriority() {
		return 1000 - priority;
	}
    
	public void setPriority(int priority) {
		this.priority = 1000 - priority;
	}
	
    public void recodeRequest(){
    	requestCount++;
    }
    
    public int getHistoryCount(){
    	return requestCount;
    }
	
    public BasicRequest getParentRequest() {
		return parentRequest;
	}

	public void setParentRequest(BasicRequest parentRequest) {
		this.parentRequest = parentRequest;
	}

	/**
	 * request排序
	 * getPriority越小  优先级越高   但是对于上层调用无需关心  框架会做反转
	 */
	@Override
	public int compareTo(BasicRequest o) {
		if(this.getPriority() < o.getPriority()){
    		return 1;
    	}else if(this.getPriority() == o.getPriority()){
    		return 0;
    	}else{
    		return -1;
    	}
	}
	
	/**
	 * 当子url或者当前url完成的时候回调
	 * @param hashcode  实际Request的hashCode
	 */
	public void notify(int hashcode) {
		if( parentRequest != null){
			parentRequest.notify(hashcode);
		}
	}
	
	/**
	 * 设置属性
	 * @param attribute
	 * @param value
	 * @return  返回BasicRequest对象自身
	 */
	public abstract  BasicRequest addAttribute(String attribute,Object value);
	
	/**
	 * 取得属性
	 * @param attribute
	 * @return 返回attribute属性对应的value。没有则返回null
	 */
	public  abstract Object getAttribute(String attribute);
    
    
}
