package com.guozhong.queue;

import com.guozhong.request.BasicRequest;

/**
 * 线程安全的可阻塞式队列接口
 * @author 郭钟 
 * @QQ群  202568714
 *
 */
public interface BlockingRequestQueue {
	
	/**
	 * 检索并移除此队列的头，如果此队列为空，则返回 null。 
	 * @return
	 */
	public BasicRequest poll();
	
	/**
	 * 向队列中添加指定的元素。 
	 * @param e
	 * @return
	 */
	public boolean add(BasicRequest e);
	
	/**
	 * 检索并移除此队列的头部，如果此队列不存在任何元素，则一直等待。
	 * @return
	 * @throws InterruptedException
	 */
	public BasicRequest take() throws InterruptedException ;
	
	/**
	 * 检索，但是不移除此队列的头，如果此队列为空，则返回 null。
	 */
	public BasicRequest peek();
	
	/**
	 * 检索，但是不移除此队列的头。 此方法与 peek 方法的惟一不同是，如果此队列为空，它会抛出一个异常。
	 */
	public BasicRequest element();
	
	/**
	 * 从此队列移除指定元素的单个实例（如果存在）。 
	 * @return 
	 */
	public boolean remove(BasicRequest e);
	
	/**
	 * 检索并移除此队列的头。此方法与 poll 方法的不同在于，如果此队列为空，它会抛出一个异常。 抛出： NoSuchElementException
	 * - 如果此队列为空。
	 */
	public BasicRequest remove();
	
	/**
	 * 返回队列中的元素个数。
	 */
	public int size();
	
	/**
	 * 返回队列中的是否为空
	 */
	public boolean isEmpty();
	
	/**
	 * 清空队列所有元素
	 */
	public void clear();
	
}
