/**
 * Copyright (c) 2018 chenfanglin
 * All rights reserved
 */
package org.netty.network.codec;

import org.netty.network.packet.Header;

/**
 * @author chenfanglin
 * 2018年2月9日下午12:57:41
 */
public interface CommandDecoder<T extends Command<?>> {

    /**
     * 命令解码,将数据包包体转换成对应的Protobuf对象.
     * @param header 数据包包头
     * @param bodyData 数据包包体
     * @throws Exception if anything goes wrong.
     */
    T decode(Header header, byte[] bodyData) throws Exception;
}
