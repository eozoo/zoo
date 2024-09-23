/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.fom.ScheduleContext;
import org.springframework.fom.annotation.EnableFom;

/**
 * @author shanhuiming
 */
@ConditionalOnClass(ScheduleContext.class)
@EnableFom
@Configuration
public class FomConfiguration {

}
