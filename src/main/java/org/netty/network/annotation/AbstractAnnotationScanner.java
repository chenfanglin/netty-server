/**
 * Copyright (c) 2018 chenfanglin
 * All rights reserved
 */
package org.netty.network.annotation;

import org.netty.network.codec.CommandDecoder;
import org.netty.network.processor.CmdProcessor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public abstract class AbstractAnnotationScanner implements ApplicationContextAware{

	protected ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	public abstract CommandDecoder<?> getDecoder(int cmd);
	
	public abstract CmdProcessor<?> getProcessor(int cmd);
	
	public abstract RequestMapperHolder getRequestMapperHolder(String url);
}
