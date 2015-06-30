package com.guozhong.queue;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;

import com.guozhong.request.BasicRequest;
import com.guozhong.request.PageRequest;
import com.guozhong.request.StartContext;

/**
 * 优先级队列
 * @author Administrator
 *
 */
public final class RequestPriorityBlockingQueue extends
		PriorityBlockingQueue<BasicRequest> implements BlockingRequestQueue{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public RequestPriorityBlockingQueue(){
		super();
	}

	@Override
	public boolean remove(BasicRequest e) {
		return super.remove(e);
	}
	
}
