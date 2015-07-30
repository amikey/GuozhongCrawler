package com.guozhong.component;

import java.util.List;
import java.util.Set;

import com.guozhong.model.Proccessable;
import com.guozhong.request.TransactionRequest;

/**
 * TransactionRequest的实现接口
 * @author 郭钟 
 * @QQ群  202568714
 *
 */
public interface TransactionCallBack {
	
	/**
	 * 处理一个事务完成的所有结果
	 * @param page
	 * @param context
	 * @param queue  
	 * @throws Exception
	 */
	void callBack(TransactionRequest transactionRequest , List<Proccessable> objectContainer)throws Exception;
	
}
