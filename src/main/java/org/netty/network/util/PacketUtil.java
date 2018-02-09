/**
 * Copyright (c) 2018 chenfanglin
 * All rights reserved
 */
package org.netty.network.util;

import java.util.Arrays;

import org.netty.network.packet.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;

/**
 * 数据包工具类
 * @author chenfanglin
 * 2018年2月9日下午12:00:36
 */
public class PacketUtil {

    private static final Logger log = LoggerFactory.getLogger(PacketUtil.class);
    public static final int DEFAULT_HEAD_LEN = 84;
    /**
     * 解析数据包包头
     * @param frame 数据包
     * @return
     */
    public static final Header parsePacketHeader(ByteBuf frame) {
        // 计算实际接受到的数据包的总长度
        int actualLen = frame.readableBytes();
        // 获取数据包的总长度
        int totalLen = frame.readInt();

        // 检查数据包的总长度
        if (checkPackageTotalLength(actualLen, totalLen)) {
            return null;
        }

        // 获取包头的长度
        long headLen = frame.readUnsignedInt();

        // 获取包体的长度
        int bodyLength = actualLen - (int) headLen;

        if (log.isDebugEnabled()) {
            log.debug("Total Length:{},Head Length:{},Body Length:{}", actualLen, headLen, bodyLength);
        }
        // 获取命令
        long cmd = frame.readUnsignedInt();
        // 用户ID
        int userId = frame.readInt();
        // 游戏ID
        long gameId = frame.readUnsignedInt();
        // 解析时间戳
        long timeStamp = frame.readLong();

        Header header = new Header();
        header.setLen(totalLen);
        header.setHeadLen(headLen);
        header.setCmd(cmd);
        header.setUserId(userId);
        header.setGameId(gameId);
        header.setTimeStamp(timeStamp);
        header.setBodyLen(bodyLength);
        return header;
    }



    /**
     * 根据包体的长度从ByteBuf中读取数据到字节数组中
     * @param frame  存储数据包的ByteBuf
     * @param header 数据包的包头
     */
    public static byte[] readPacketBody(ByteBuf frame, Header header) {
        // 读取包体
        byte[] body = new byte[(int) header.getBodyLen()];
        frame.readBytes(body);
        if (log.isDebugEnabled()) {
            log.debug("Body Content:{}", Arrays.toString(body));
            log.debug("writeIndex:{}", frame.readerIndex());
        }
        return body;
    }


    /**
     * 查询数据包的总长度
     * @param actualLen 实际接受到的数据包总长度
     * @param totalLen 包头中标识的数据包总长度
     * @return 当接受到的长度与实际长度不一致时，返回false
     */
    private static  boolean checkPackageTotalLength(int actualLen, int totalLen) {
        // 当实际接受到的包的长度与包头上的长度不同,则数据包异常
        if (actualLen != (totalLen + 4)) {
            log.warn("Packet Exception,accept Header is:{},actual Header is:{}", actualLen, totalLen);
            return true;
        }
        return false;
    }


}
