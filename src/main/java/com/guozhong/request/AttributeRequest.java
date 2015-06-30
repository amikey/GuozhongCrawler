package com.guozhong.request;

import java.util.HashMap;

/**
 * AttributeRequest是BasicRequest操作是属性的实现。
 * @author 郭钟 
 * @QQ群  202568714
 *
 */
public class AttributeRequest extends BasicRequest {
	
	/**
     * request属性
     */
    private HashMap<String,Object> attributes = null;

	@Override
	public BasicRequest addAttribute(String attribute, Object value) {
		if(attributes == null){
    		attributes = new HashMap<String,Object>();
    	}
    	attributes.put(attribute, value);
    	return this;
	}

	@Override
	public Object getAttribute(String attribute) {
		if(attributes == null){
    		return null;
    	}
    	return attributes.get(attribute);
	}

}
