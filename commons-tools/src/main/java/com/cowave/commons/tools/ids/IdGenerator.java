/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.tools.ids;

import cn.hutool.core.lang.Assert;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author shanhuiming
 *
 */
public class IdGenerator {

	private final AtomicInteger index = new AtomicInteger(0);

	/**
	 * 获取固定长度的id
	 * @param prefix     id前缀
	 * @param suffix     id后缀
	 * @param dateFormat 时间格式
	 * @param indexLimit index上限
	 */
	public String generateIdWithDate(String prefix, String suffix, String dateFormat, int indexLimit){
		Assert.isTrue(StringUtils.isNotBlank(dateFormat), "dateFormat cannot be empty.");
		Assert.isTrue(indexLimit > 0, "indexLimit must greater than 0.");

		String date = new SimpleDateFormat(dateFormat).format(System.currentTimeMillis());
		int limitLen = String.valueOf(indexLimit).length();

		StringBuilder builder = new StringBuilder();
		if(StringUtils.isNotBlank(prefix)){
			builder.append(prefix);
		}

		builder.append(date);

		if(StringUtils.isNotBlank(suffix)){
			builder.append(suffix);
		}

		String currentIndex = String.valueOf(index.incrementAndGet() % indexLimit);
		int currentLen = currentIndex.length();

		int currentLimit = 1;
		for(int i = 1; i < currentLen; i++){
			currentLimit *= 10;
		}
		for(int i = currentIndex.length(); i < limitLen; i++){
			currentLimit *= 10;
			if(currentLimit < indexLimit){
				builder.append('0');
			}
		}
		return builder.append(currentIndex).toString();
	}
}
