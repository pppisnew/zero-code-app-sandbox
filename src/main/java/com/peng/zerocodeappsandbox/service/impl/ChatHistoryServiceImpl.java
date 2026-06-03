package com.peng.zerocodeappsandbox.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.peng.zerocodeappsandbox.common.CursorPageResponse;
import com.peng.zerocodeappsandbox.exception.BusinessException;
import com.peng.zerocodeappsandbox.exception.ErrorCode;
import com.peng.zerocodeappsandbox.model.entity.ChatHistory;
import com.peng.zerocodeappsandbox.mapper.ChatHistoryMapper;
import com.peng.zerocodeappsandbox.model.dto.chat.ChatHistoryQueryRequest;
import com.peng.zerocodeappsandbox.model.enums.MessageTypeEnum;
import com.peng.zerocodeappsandbox.model.vo.chat.ChatHistoryVO;
import com.peng.zerocodeappsandbox.service.ChatHistoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对话历史 服务层实现。
 *
 * @author peng
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final int MAX_PAGE_SIZE = 50;

    @Override
    public boolean saveMessage(Long appId, Long userId, String message, MessageTypeEnum messageTypeEnum) {
        if (appId == null || appId <= 0 || userId == null || userId <= 0
                || StrUtil.isBlank(message) || messageTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "对话消息参数错误");
        }
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setAppId(appId);
        chatHistory.setUserId(userId);
        chatHistory.setMessage(message);
        chatHistory.setMessageType(messageTypeEnum.getValue());
        return this.save(chatHistory);
    }

    @Override
    public CursorPageResponse<ChatHistoryVO> listAppChatHistoryByCursor(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        if (chatHistoryQueryRequest == null || chatHistoryQueryRequest.getAppId() == null
                || chatHistoryQueryRequest.getAppId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用 id 不能为空");
        }
        return listByCursor(chatHistoryQueryRequest, false, true);
    }

    @Override
    public CursorPageResponse<ChatHistoryVO> listAllChatHistoryByCursor(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        if (chatHistoryQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        return listByCursor(chatHistoryQueryRequest, true, false);
    }

    @Override
    public boolean removeByAppId(Long appId) {
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用 id 不能为空");
        }
        QueryWrapper queryWrapper = QueryWrapper.create().eq("appId", appId);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count == 0) {
            return true;
        }
        return this.remove(queryWrapper);
    }

    @Override
    public ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory) {
        if (chatHistory == null) {
            return null;
        }
        ChatHistoryVO chatHistoryVO = new ChatHistoryVO();
        BeanUtil.copyProperties(chatHistory, chatHistoryVO);
        return chatHistoryVO;
    }

    @Override
    public List<ChatHistoryVO> getChatHistoryVOList(List<ChatHistory> chatHistoryList) {
        if (CollUtil.isEmpty(chatHistoryList)) {
            return new ArrayList<>();
        }
        return chatHistoryList.stream().map(this::getChatHistoryVO).collect(Collectors.toList());
    }

    private CursorPageResponse<ChatHistoryVO> listByCursor(ChatHistoryQueryRequest chatHistoryQueryRequest,
                                                           boolean allowEmptyAppId,
                                                           boolean reverseRecords) {
        String messageType = chatHistoryQueryRequest.getMessageType();
        if (StrUtil.isNotBlank(messageType)
                && MessageTypeEnum.getEnumByValue(messageType) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息类型错误");
        }
        int pageSize = chatHistoryQueryRequest.getPageSize();
        if (pageSize <= 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分页大小过大");
        }
        Long appId = chatHistoryQueryRequest.getAppId();
        if (!allowEmptyAppId && (appId == null || appId <= 0)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用 id 不能为空");
        }
        List<ChatHistory> chatHistoryList = this.mapper.selectByCursor(
                appId,
                chatHistoryQueryRequest.getUserId(),
                messageType,
                chatHistoryQueryRequest.getCursor(),
                chatHistoryQueryRequest.getCursorId(),
                pageSize + 1
        );
        boolean hasMore = chatHistoryList.size() > pageSize;
        if (hasMore) {
            chatHistoryList = chatHistoryList.subList(0, pageSize);
        }
        CursorPageResponse<ChatHistoryVO> cursorPageResponse = new CursorPageResponse<>();
        cursorPageResponse.setHasMore(hasMore);
        if (CollUtil.isNotEmpty(chatHistoryList)) {
            ChatHistory lastChatHistory = chatHistoryList.get(chatHistoryList.size() - 1);
            cursorPageResponse.setNextCursor(lastChatHistory.getCreateTime());
            cursorPageResponse.setNextCursorId(lastChatHistory.getId());
        }
        if (reverseRecords) {
            chatHistoryList = new ArrayList<>(chatHistoryList);
            Collections.reverse(chatHistoryList);
        }
        cursorPageResponse.setRecords(getChatHistoryVOList(chatHistoryList));
        return cursorPageResponse;
    }
}
