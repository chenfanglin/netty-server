package org.netty.network.annotation;

/**
 * Copyright (c) 2018 chenfanglin
 * All rights reserved
 */
import java.lang.reflect.Method;

import org.netty.network.processor.BaseHttpService;

public class RequestMapperHolder {

	private BaseHttpService service;
	
	private Method method;
	
	public RequestMapperHolder() {
	}
	
	public RequestMapperHolder (BaseHttpService service, Method method) {
		this.service = service;
		this.method = method;
	}
	
	public BaseHttpService getService() {
		return service;
	}
	public void setService(BaseHttpService service) {
		this.service = service;
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	
	
}
