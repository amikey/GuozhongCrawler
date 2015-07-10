package com.guozhong.queue;

import java.util.concurrent.LinkedBlockingQueue;

import com.guozhong.request.BasicRequest;

/**
 * SimpleBlockingQueue采用先进先出的FIFO原则。广度优先策略合适的队列
 * @author 郭钟 
 * @QQ群  202568714
 *
 */
public class SimpleBlockingQueue extends LinkedBlockingQueue<BasicRequest> implements BlockingRequestQueue {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean remove(BasicRequest e) {
		return super.remove(e);
	}

}
