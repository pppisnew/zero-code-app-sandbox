package com.peng.zerocodeappsandbox.service;

import com.mybatisflex.core.service.IService;
import com.peng.zerocodeappsandbox.common.CursorPageResponse;
import com.peng.zerocodeappsandbox.model.dto.chat.ChatHistoryQueryRequest;
import com.peng.zerocodeappsandbox.model.entity.ChatHistory;
import com.peng.zerocodeappsandbox.model.enums.MessageTypeEnum;
import com.peng.zerocodeappsandbox.model.vo.chat.ChatHistoryVO;

import java.util.List;

/**
 * 对话历史 服务层。
 *
 * @author peng
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 保存对话消息
     */
    boolean saveMessage(Long appId, Long userId, String message, MessageTypeEnum messageTypeEnum);

    /**
     * 游标查询某个应用的对话历史
     */
    CursorPageResponse<ChatHistoryVO> listAppChatHistoryByCursor(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 管理员游标查询所有对话历史
     */
    CursorPageResponse<ChatHistoryVO> listAllChatHistoryByCursor(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 删除某个应用的全部对话历史
     */
    boolean removeByAppId(Long appId);

    /**
     * 获取对话历史封装
     */
    ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory);

    /**
     * 获取对话历史封装列表
     */
    List<ChatHistoryVO> getChatHistoryVOList(List<ChatHistory> chatHistoryList);
}
