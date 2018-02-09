/**
 * Copyright (c) 2018 chenfanglin
 * All rights reserved
 */
package org.netty.network.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringUtils implements ApplicationContextAware{

	private static ApplicationContext APPLICATIONCONTEXT;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		APPLICATIONCONTEXT = applicationContext;
	}
	
	public static ApplicationContext getApplicationContext() {
		if (APPLICATIONCONTEXT == null) {
			throw new NullPointerException("ApplicationContext is null");
		}
		return APPLICATIONCONTEXT;
	}

	public static Object getBean(String name){
		return getApplicationContext().getBean(name);
	}
	
	public static <T> T getBean(Class<T> clazz) throws BeansException {
		return getApplicationContext().getBean(clazz);
//		String oriName = clazz.getSimpleName();
//		String name = oriName.substring(0, 1).toLowerCase() + oriName.substring(1);
//		return (T) getBean(name);
	}
}
