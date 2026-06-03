package com.peng.zerocodeappsandbox.model.dto.chat;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ChatHistoryQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 创建用户 id（管理员查询使用）
     */
    private Long userId;

    /**
     * 消息类型：user/ai/error
     */
    private String messageType;

    /**
     * 游标时间，查询该时间之前的历史记录
     */
    private LocalDateTime cursor;

    /**
     * 游标 id，与 cursor 一起避免同一秒消息分页丢失
     */
    private Long cursorId;

    /**
     * 每页大小
     */
    private int pageSize = 10;
}
