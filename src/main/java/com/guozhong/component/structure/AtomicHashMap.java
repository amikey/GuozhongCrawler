package com.guozhong.component.structure;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.guozhong.model.Proccessable;

public final class AtomicHashMap extends
		ConcurrentHashMap<Proccessable, AtomicInteger> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public  final AtomicInteger getOrCreate(Proccessable key) {
		AtomicInteger atomicInteger = get(key);
		if(atomicInteger != null){
			return atomicInteger;
		}else{
			atomicInteger = new AtomicInteger(0);
			put(key, atomicInteger);
		}
		return atomicInteger;
	};
	
	
}
