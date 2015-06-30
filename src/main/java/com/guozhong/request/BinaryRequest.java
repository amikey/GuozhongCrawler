package com.guozhong.request;

import com.guozhong.component.BinaryProcessor;

/**
 * 文件下载类型Request的表示
 * @author 郭钟 
 * @QQ群  202568714
 *
 */
public class BinaryRequest extends AttributeRequest {

	private String url ;
	
	private final Class<? extends BinaryProcessor> binaryProccessor;
	
	public BinaryRequest(String url , Class<? extends BinaryProcessor> binaryProccessor){
		this.type = Type.BINARY_REQUEST;
		this.url = url;
		this.binaryProccessor = binaryProccessor;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		if(url == null){
			throw new NullPointerException();
		}else{
			this.url = url;
		}
	}
	
	public Class<? extends BinaryProcessor> getBinaryProccessor() {
		return binaryProccessor;
	}

	@Override
	public String toString() {
		return "BinaryRequest [url=" + url + ", binaryProccessor="
				+ binaryProccessor + "]";
	}
	
	
}
