package com.peng.zerocodeappsandbox.mapper;

import com.mybatisflex.core.BaseMapper;
import com.peng.zerocodeappsandbox.model.entity.ChatHistory;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 映射层。
 *
 * @author peng
 */
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {

    /**
     * 游标查询对话历史
     */
    List<ChatHistory> selectByCursor(@Param("appId") Long appId,
                                     @Param("userId") Long userId,
                                     @Param("messageType") String messageType,
                                     @Param("cursor") LocalDateTime cursor,
                                     @Param("cursorId") Long cursorId,
                                     @Param("limit") Integer limit);
}
