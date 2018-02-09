/**
 * Copyright (c) 2018 chenfanglin
 * All rights reserved
 */
package org.netty.network.util;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * JSON工具类
 * @author chenfanglin
 * 2018年2月8日下午8:41:17
 */
public abstract class JsonUtils {

    private JsonUtils() {
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.setSerializationInclusion(Include.NON_NULL);  //排除null属性
        MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS); //排除空属性
        // 排除反序列化时多余的属性
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    /**
     * 将对象转换成JSON
     *
     * @param object
     * @return
     * @throws JsonProcessingException
     */
    public static String objectToJson(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将JSON转换成对象
     *
     * @param jsonValue
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T jsonToObject(String jsonValue, Class<T> clazz) {
        try {
            return MAPPER.readValue(jsonValue, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 将JSON转换成Java集合
     *
     * @param jsonValue
     * @param clazz     集合元素的Class
     * @param <T>
     * @return
     */
    public static <T> List<T> jsonToList(String jsonValue, Class<T> clazz) {
        try {
            return MAPPER.readValue(jsonValue, getCollectionType(clazz));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 将集合转换成Jackson所能够识别的类型
     *
     * @param elementClass
     * @return
     */
    public static JavaType getCollectionType(Class elementClass) {
        return MAPPER.getTypeFactory().constructCollectionType(List.class, elementClass);
    }


}
