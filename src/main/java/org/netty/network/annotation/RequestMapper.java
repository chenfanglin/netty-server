/**
 * Copyright (c) 2018 chenfanglin
 * All rights reserved
 */
package org.netty.network.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Http请求Url
 * @author chenfanglin
 * 2018年2月8日上午10:55:06
 */
@Target({ java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapper {
	
	public abstract String value();
}
