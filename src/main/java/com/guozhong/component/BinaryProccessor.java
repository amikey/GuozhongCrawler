package com.guozhong.component;

import java.io.InputStream;

import com.guozhong.request.BinaryRequest;

/**
 * 文件请求处理
 * @author Administrator
 *
 */
public interface BinaryProccessor {
	/**
	 * 请求文件
	 * @param binaryRequest
	 * @param input  文件对应的字节流
	 */
	public void process(BinaryRequest binaryRequest,InputStream input);
	
	/**
	 * 请求文件失败
	 * @param binaryRequest
	 */
	public void processError(BinaryRequest binaryRequest);
}
