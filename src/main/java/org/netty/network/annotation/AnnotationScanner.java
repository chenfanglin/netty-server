/**
 * Copyright (c) 2018 chenfanglin
 * All rights reserved
 */
package org.netty.network.annotation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.netty.network.codec.CommandDecoder;
import org.netty.network.http.XXHttpRequest;
import org.netty.network.http.XXHttpResponse;
import org.netty.network.processor.BaseHttpService;
import org.netty.network.processor.CmdProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 自定义注解扫描器
 * 
 * @author chenfanglin 2018年2月8日下午1:18:43
 */
@Component
@SuppressWarnings("rawtypes")
public class AnnotationScanner extends AbstractAnnotationScanner implements InitializingBean {

	public static final Logger logger = LoggerFactory.getLogger(AnnotationScanner.class);

	/**
	 * 扫描结果缓存 key: cmd命令字 value：对应的解码器
	 */
	private Map<Integer, CommandDecoder> scanCommandDecoders = new ConcurrentHashMap<Integer, CommandDecoder>();

	/**
	 * 扫描结果缓存 key: cmd命令字 value：对应的处理器
	 */
	private Map<Integer, CmdProcessor> scanCmdProcessors = new ConcurrentHashMap<Integer, CmdProcessor>();

	/**
	 * 扫描结果缓存 key: url value：对应的处理器
	 */
	private Map<String, RequestMapperHolder> scanRequestMappers = new ConcurrentHashMap<String, RequestMapperHolder>();

	@SuppressWarnings("unchecked")
	@Override
	public CommandDecoder getDecoder(int cmd) {
		return scanCommandDecoders.get(cmd);
	}

	@SuppressWarnings("unchecked")
	@Override
	public CmdProcessor getProcessor(int cmd) {
		return scanCmdProcessors.get(cmd);
	}
	
	@Override
	public RequestMapperHolder getRequestMapperHolder(String url) {
		return scanRequestMappers.get(url);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		buildCommandDecoderMap();
		buildCmdProcessorMap();
		buildHttpRequestMap();
	}

	private void buildCommandDecoderMap() {
		String[] beanNames = applicationContext.getBeanDefinitionNames();
		for (String beanName : beanNames) {
			Object object = applicationContext.getBean(beanName);
			if (object instanceof CommandDecoder) {
				Class<?> clazz = object.getClass();
				if (clazz.getAnnotation(Deprecated.class) != null) {
					continue;
				}
				CmdCodec cmdCodecAnno = clazz.getAnnotation(CmdCodec.class);
				int cmd = cmdCodecAnno.value();
				CommandDecoder commandDecoder = (CommandDecoder) object;
				scanCommandDecoders.put(cmd, commandDecoder);
				logger.info("scanCommandDecoders:" + scanCommandDecoders.toString());
			}
		}
	}

	private void buildCmdProcessorMap() {
		String[] beanNames = applicationContext.getBeanDefinitionNames();
		for (String beanName : beanNames) {
			Object object = applicationContext.getBean(beanName);
			if (object instanceof CmdProcessor) {
				Class<?> clazz = object.getClass();
				if (clazz.getAnnotation(Deprecated.class) != null) {
					continue;
				}
				CmdMapper cmdMapperAnno = clazz.getAnnotation(CmdMapper.class);
				int cmd = cmdMapperAnno.value();
				CmdProcessor cmdProcessor = (CmdProcessor) object;
				scanCmdProcessors.put(cmd, cmdProcessor);
				logger.info("scanCmdProcessors:" + scanCmdProcessors.toString());
			}
		}
	}

	private void buildHttpRequestMap() {
		String[] beanNames = applicationContext.getBeanDefinitionNames();
		for (String beanName : beanNames) {
			Object object = applicationContext.getBean(beanName);
			if (object instanceof BaseHttpService) {
				BaseHttpService service = (BaseHttpService) object;
				Class<?> clazz = object.getClass();
				if (clazz.getAnnotation(Deprecated.class) != null) {
					continue;
				}
				Method[] methods = clazz.getDeclaredMethods();
				for (Method method : methods) {
					if (!isRequestMapperMethod(method)) {
						continue;
					}
					RequestMapper requestMapperAnno = method.getAnnotation(RequestMapper.class);
					String url = requestMapperAnno.value();
					if (!StringUtils.isEmpty(url)) {
						scanRequestMappers.put(url, new RequestMapperHolder(service, method));
						logger.info("scanRequestMappers:" + scanRequestMappers.toString());
					}
				}
			}
		}
	}

	/**
	 * 有RequestMapper注解的方法
	 * 
	 * @param method
	 * @return
	 */
	private boolean isRequestMapperMethod(Method method) {
		if (method.getAnnotation(Deprecated.class) != null) {
			return false;
		}
		if (!Modifier.isPublic(method.getModifiers())) {
			return false;
		}
		Class<?>[] pts = method.getParameterTypes();
		if (pts.length != 2) {
			return false;
		}
		if (!pts[0].isAssignableFrom(XXHttpRequest.class) || !pts[1].isAssignableFrom(XXHttpResponse.class)) {
			return false;
		}
		return true;
	}

}
