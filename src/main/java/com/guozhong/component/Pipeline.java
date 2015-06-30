package com.guozhong.component;

import java.io.Serializable;
import java.util.List;

import com.guozhong.model.Proccessable;

/**
 * Pipeline
 * @author 郭钟 
 * @QQ群  202568714
 *
 */
public interface  Pipeline extends Serializable{
	
	/**
	 * 所有的结构化数据将流向这里。在这里存储你的bean
	 * @param procdata
	 */
	public  void proccessData(List<Proccessable> objectContainer); 
}
