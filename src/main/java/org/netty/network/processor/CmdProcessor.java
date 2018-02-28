/**
 * Copyright (c) 2018 chenfanglin
 * All rights reserved
 */
package org.netty.network.processor;

import org.netty.network.codec.Command;

import io.netty.channel.ChannelHandlerContext;

/**
 * cmd 处理接口
 * @author chenfanglin
 * 2018年2月8日下午1:46:41
 */
public interface CmdProcessor <T extends Command<?>> {

    void process(ChannelHandlerContext ctx, T command);
}
