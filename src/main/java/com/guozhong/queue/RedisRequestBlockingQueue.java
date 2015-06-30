package com.guozhong.queue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.sound.midi.VoiceStatus;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.guozhong.component.BinaryProcessor;
import com.guozhong.component.PageProcessor;
import com.guozhong.component.PageScript;
import com.guozhong.model.Proccessable;
import com.guozhong.page.OkPage;
import com.guozhong.page.Page;
import com.guozhong.request.BasicRequest;
import com.guozhong.request.BinaryRequest;
import com.guozhong.request.PageRequest;
import com.guozhong.request.StartContext;

/**
 * 优先级队列
 * 
 * @author Administrator
 *
 */
public final class RedisRequestBlockingQueue implements BlockingRequestQueue,Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JedisPool pool = null;

	private byte[] queue;
	
	
	/**
	 * 给定JedisPoolConfig初始化一个队列
	 * @param host
	 * @param port
	 * @param config
	 * @param queue
	 */
	public RedisRequestBlockingQueue(String host, int port, JedisPoolConfig config, String queue) {
		pool = new JedisPool(config, host, port, 15000);
		try {
			this.queue = queue.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 基于默认配置初始化一个队列
	 * @param host
	 * @param port
	 * @param queue
	 */
	public RedisRequestBlockingQueue(String host, int port, String queue) {
		JedisPoolConfig config = new JedisPoolConfig();

		// 连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
		config.setBlockWhenExhausted(true);

		// 设置的逐出策略类名, 默认DefaultEvictionPolicy(当连接超过最大空闲时间,或连接数超过最大空闲连接数)
		config.setEvictionPolicyClassName("org.apache.commons.pool2.impl.DefaultEvictionPolicy");

		// 是否启用pool的jmx管理功能, 默认true
		config.setJmxEnabled(true);

		// MBean ObjectName = new
		// ObjectName("org.apache.commons.pool2:type=GenericObjectPool,name=" +
		// "pool" + i); 默 认为"pool", JMX不熟,具体不知道是干啥的...默认就好.
		config.setJmxNamePrefix("pool");

		// 是否启用后进先出, 默认true
		config.setLifo(true);

		// 最大空闲连接数, 默认8个
		config.setMaxIdle(100);

		// 最大连接数, 默认8个
		config.setMaxTotal(300);

		// 获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,
		// 默认-1
		config.setMaxWaitMillis(10000);

		// 逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
		config.setMinEvictableIdleTimeMillis(1800000);

		// 最小空闲连接数, 默认0
		config.setMinIdle(20);

		// 每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
		config.setNumTestsPerEvictionRun(3);

		// 对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数
		// 时直接逐出,不再根据MinEvictableIdleTimeMillis判断 (默认逐出策略)
		config.setSoftMinEvictableIdleTimeMillis(1800000);

		// 在获取连接的时候检查有效性, 默认false
		config.setTestOnBorrow(true);

		// 在空闲时检查有效性, 默认false
		config.setTestWhileIdle(true);

		// 逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
		config.setTimeBetweenEvictionRunsMillis(20);

		pool = new JedisPool(config, host, port, 15000);
		try {
			this.queue = queue.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public BasicRequest remove() {
		Long leng = new OperationJedis<Long>() {
			
			@Override
			protected Long operation(Jedis jedis) throws Exception {
				return jedis.llen(queue);
			}
		}.exe();
		if (leng == 0) {
			throw new NoSuchElementException("队列长度为0");
		} else {
			return poll();
		}
	}

	@Override
	public BasicRequest poll() {
		BasicRequest basicRequest = null;

		byte[] data = new OperationJedis<byte[]>() {

			@Override
			protected byte[] operation(Jedis jedis) throws Exception {
				return jedis.rpop(queue);
			}
		}.exe();

		basicRequest = byteToObject(data);

		return basicRequest;
	}

	@Override
	public BasicRequest element() {
		BasicRequest basicRequest = null;

		byte[] data = new OperationJedis<byte[]>() {

			@Override
			protected byte[] operation(Jedis jedis) throws Exception {
				byte[] data = jedis.rpop(queue);
				if (data != null) {
					jedis.rpush(queue, data);
				}
				return data;
			}
		}.exe();

		if (data == null) {
			throw new NoSuchElementException("队列长度为0");
		}

		basicRequest = byteToObject(data);
		return basicRequest;
	}

	
	@Override
	public BasicRequest peek() {
		BasicRequest basicRequest = null;

		byte[] data = new OperationJedis<byte[]>() {

			@Override
			protected byte[] operation(Jedis jedis) throws Exception {
				byte[] data = jedis.rpop(queue);
				if (data != null) {
					jedis.rpush(queue, data);
				}
				return data;
			}
		}.exe();

		basicRequest = byteToObject(data);
		return basicRequest;
	}

	/**
	 * 返回队列中的元素个数。
	 */
	@Override
	public int size() {
		long size = new OperationJedis<Long>() {

			@Override
			protected Long operation(Jedis jedis) throws Exception {
				return jedis.llen(queue);
			}
		}.exe();
		return (int) size;
	}

	
	@Override
	public boolean isEmpty() {
		boolean isEmpty = new OperationJedis<Boolean>() {

			@Override
			protected Boolean operation(Jedis jedis) throws Exception {
				return jedis.llen(queue) == 0;
			}
		}.exe();
		return isEmpty;
	}

	@Override
	public void clear() {
		new OperationJedis<Void>() {

			@Override
			protected Void operation(Jedis jedis) throws Exception {
				jedis.del(queue);
				return null;
			}
		}.exe();
	}

	@Override
	public boolean add(final BasicRequest e) {
		new OperationJedis<Void>() {

			@Override
			protected Void operation(Jedis jedis) throws Exception {
				byte[] data = objectToByte(e);
				jedis.lpush(queue, data);
				return null;
			}
		}.exe();
		return true;
	}


	public BasicRequest take() throws InterruptedException {
		BasicRequest basicRequest = null;

		byte[] data = new OperationJedis<byte[]>() {

			@Override
			protected byte[] operation(Jedis jedis) throws Exception {
				byte[] data = null;
				while(true){
					data = jedis.rpop(queue);
					if(data != null){
						break;
					}
					Thread.sleep(100);
				}
				return data;
			}
		}.exe();

		basicRequest = byteToObject(data);

		return basicRequest;
	}

	@Override
	public boolean remove(final BasicRequest o) {
		new OperationJedis<Void>() {

			@Override
			protected Void operation(Jedis jedis) throws Exception {
				if(o instanceof BasicRequest){
					byte[] data = objectToByte((BasicRequest) o);
					jedis.lrem(queue, 0, data);
				}
				return null;
			}
		}.exe();
		return true;
	}

	public byte[] objectToByte(BasicRequest obj) {
		if (obj == null) {
			return null;
		}
		byte[] bytes = null;
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream oo = null;
		try {
			oo = new ObjectOutputStream(bo);
			oo.writeObject(obj);
			bytes = bo.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (oo != null) {
				try {
					oo.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (bo != null) {
				try {
					bo.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bytes;
	}

	public BasicRequest byteToObject(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		Serializable obj = null;
		ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
		ObjectInputStream oi = null;
		try {
			oi = new ObjectInputStream(bi);
			obj = (Serializable) oi.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (oi != null) {
				try {
					oi.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (bi != null) {
				try {
					bi.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return (BasicRequest) obj;
	}

	@SuppressWarnings("unused")
	private abstract class OperationJedis<E> {

		protected abstract E operation(Jedis jedis) throws Exception;

		public final E exe() {
			Jedis jedis = null;
			E result = null;
			try {
				jedis = pool.getResource();
				result = operation(jedis);
			} catch (Exception e) {
				e.printStackTrace();
				if (jedis != null) {
					pool.returnBrokenResource(jedis);
					jedis = null;
				}
			} finally {
				if (jedis != null) {
					pool.returnResource(jedis);
				}
			}
			return result;
		}
	}

//	
//	public static void main(String[] args) {
//		StartContext context = new StartContext();
//		System.out.println("1:"+System.currentTimeMillis());
//		RedisRequestBlockingQueue redisRequestBlockingQueue = new RedisRequestBlockingQueue("", 6379, "guo");
//		System.out.println("2:"+System.currentTimeMillis());
//		redisRequestBlockingQueue.add(context.createBinaryRequest("aaa", TestProcessor.class));
//		redisRequestBlockingQueue.add(context.createPageRequest("aasdsaaa2", TestProcessor.class));
//		redisRequestBlockingQueue.add(context.createPageRequest("aaa3", TestProcessor.class));
//		redisRequestBlockingQueue.add(context.createPageRequest("aa232a4", TestProcessor.class));
//		System.out.println("3:"+System.currentTimeMillis());
//		System.out.println(redisRequestBlockingQueue.poll());
//		System.out.println("--------------");
//		while(true){
//			try {
//				System.out.println(redisRequestBlockingQueue.take());
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			System.out.println(System.currentTimeMillis());
//		}
//	}
//	
//	
//	private static final class TestProcessor implements PageProcessor,BinaryProcessor{
//
//		@Override
//		public PageScript getJavaScript() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public Pattern getNormalContain() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public void process(OkPage page, StartContext context,
//				List<BasicRequest> queue, List<Proccessable> objectContainer)
//				throws Exception {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void processErrorPage(Page page, StartContext context)
//				throws Exception {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void process(BinaryRequest binaryRequest, InputStream input) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void processError(BinaryRequest binaryRequest) {
//			// TODO Auto-generated method stub
//			
//		}}
}
