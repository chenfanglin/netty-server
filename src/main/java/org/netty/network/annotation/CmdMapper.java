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
 * tcp命令字处理器注解
 * @author chenfanglin
 * 2018年2月8日上午10:53:11
 */
@Target({ java.lang.annotation.ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CmdMapper {
	
	public abstract int value();
}
