package com.peng.zerocodeappsandbox.controller;

import com.peng.zerocodeappsandbox.annotation.AuthCheck;
import com.peng.zerocodeappsandbox.common.BaseResponse;
import com.peng.zerocodeappsandbox.common.CursorPageResponse;
import com.peng.zerocodeappsandbox.common.ResultUtils;
import com.peng.zerocodeappsandbox.constant.UserConstant;
import com.peng.zerocodeappsandbox.exception.ErrorCode;
import com.peng.zerocodeappsandbox.exception.ThrowUtils;
import com.peng.zerocodeappsandbox.model.dto.chat.ChatHistoryQueryRequest;
import com.peng.zerocodeappsandbox.model.entity.App;
import com.peng.zerocodeappsandbox.model.entity.User;
import com.peng.zerocodeappsandbox.model.vo.chat.ChatHistoryVO;
import com.peng.zerocodeappsandbox.service.AppService;
import com.peng.zerocodeappsandbox.service.ChatHistoryService;
import com.peng.zerocodeappsandbox.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对话历史 控制层。
 *
 * @author peng
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Autowired
    private ChatHistoryService chatHistoryService;

    @Autowired
    private AppService appService;

    @Autowired
    private UserService userService;

    /**
     * 游标查询某个应用的对话历史，仅应用创建者和管理员可见。
     */
    @PostMapping("/list/app")
    public BaseResponse<CursorPageResponse<ChatHistoryVO>> listAppChatHistoryByCursor(
            @RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null
                || chatHistoryQueryRequest.getAppId() == null
                || chatHistoryQueryRequest.getAppId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        App app = appService.getById(chatHistoryQueryRequest.getAppId());
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isOwner = loginUser.getId().equals(app.getUserId());
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        ThrowUtils.throwIf(!isOwner && !isAdmin, ErrorCode.NO_AUTH_ERROR);
        return ResultUtils.success(chatHistoryService.listAppChatHistoryByCursor(chatHistoryQueryRequest));
    }

    /**
     * 管理员游标查询所有应用的对话历史，默认按时间降序。
     */
    @PostMapping("/admin/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<CursorPageResponse<ChatHistoryVO>> listAllChatHistoryByCursor(
            @RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(chatHistoryService.listAllChatHistoryByCursor(chatHistoryQueryRequest));
    }
}
