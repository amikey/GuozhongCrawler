package com.guozhong.queue;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.guozhong.request.BasicRequest;
import com.guozhong.request.BasicRequest;

/**
 * 延迟优先级队列
 * @author Administrator
 *
 */
public final class DelayedPriorityBlockingQueue implements BlockingRequestQueue ,Serializable{
	
	private final RequestPriorityBlockingQueue queue;
	private final int delayInMilliseconds;
	private volatile long lastSuccesfullPop;
	
	/**
	 * 
	 * @param delayInMilliseconds
	 */
	public DelayedPriorityBlockingQueue(final int delayInMilliseconds) {
		this.delayInMilliseconds = delayInMilliseconds;
		queue = new RequestPriorityBlockingQueue();
		lastSuccesfullPop = System.currentTimeMillis() - delayInMilliseconds;
	}
	
	
	public BasicRequest poll() {
		synchronized (queue) {
			while ((System.currentTimeMillis() - lastSuccesfullPop <= delayInMilliseconds) && !queue.isEmpty()) {
				sleep();
			}
			lastSuccesfullPop = System.currentTimeMillis();
			return queue.poll();
		}
	}

	public BasicRequest poll(final long timeout, final TimeUnit unit) throws InterruptedException {
		synchronized (queue) {
			while ((System.currentTimeMillis() - lastSuccesfullPop <= delayInMilliseconds) && !queue.isEmpty()) {
				sleep();
			}
			lastSuccesfullPop = System.currentTimeMillis();
			return queue.poll(timeout, unit);
		}
	}

	public BasicRequest take() throws InterruptedException {
		synchronized (queue) {
			while ((System.currentTimeMillis() - lastSuccesfullPop <= delayInMilliseconds) && !queue.isEmpty()) {
				sleep();
			}
			lastSuccesfullPop = System.currentTimeMillis();
			return queue.take();
		}
	}

	public BasicRequest remove() {
		return queue.remove();
	}

	private void sleep() {
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// Delegate Methods. Java is just soooo fun sometimes...

	public boolean add(final BasicRequest e) {
		return queue.add(e);
	}

	public void clear() {
		queue.clear();
	}


	public BasicRequest element() {
		return queue.element();
	}

	@Override
	public boolean equals(final Object o) {
		return queue.equals(o);
	}

	@Override
	public int hashCode() {
		return queue.hashCode();
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}

	public BasicRequest peek() {
		return queue.peek();
	}

	public int size() {
		return queue.size();
	}

	@Override
	public boolean remove(BasicRequest e) {
		return queue.remove((Object)e);
	}

}
