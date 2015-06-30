package com.guozhong.component.structure;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.RuntimeErrorException;

import com.guozhong.model.Proccessable;

/**
 * 结构化数据管理，用于自动将实体对象发送至PipeLine
 * @author Administrator
 *
 */
public final class StructManager {
	
	/**
	 * 缓存正在处理中的实体对象，记录处理的个数
	 */
	private AtomicHashMap inProccess = new AtomicHashMap();
	
	/**
	 * 添加一个处理标记
	 * @param object
	 * @return 返回当前正在处理的个数
	 */
	public int addProccess(Proccessable object){
		AtomicInteger atomicInteger = inProccess.getOrCreate(object);
		return atomicInteger.incrementAndGet();//标记一个新的处理
	}
	
	/**
	 * 为一个Proccessable集合添加处理标记
	 * @param object
	 * @return 返回当前正在处理的个数
	 */
	public void addProccess(Set<Proccessable> objects){
		for (Proccessable object : objects) {
			AtomicInteger atomicInteger = inProccess.getOrCreate(object);
			atomicInteger.incrementAndGet();//标记一个新的处理
		}
	}
	
	public int removeProccess(Proccessable object){
		AtomicInteger atomicInteger = inProccess.get(object);
		if(atomicInteger == null){
			throw new RuntimeException("漏添标记");
		}
		int count = atomicInteger.decrementAndGet();
		if(count == 0){
			inProccess.remove(object);
		}
		return count;
	}
	
	public int getProccess(Proccessable object){
		AtomicInteger atomicInteger = inProccess.get(object);
		return atomicInteger==null?0:atomicInteger.get();
	}
	
	
}
