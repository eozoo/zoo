package com.cowave.commons.framework.helper.operation;

/**
 *
 * @author shanhuiming
 *
 */
public interface OperationAccepter<T extends OperationLog> {

	/**
	 * 接收操作日志
	 */
	void accept(T log);
}
