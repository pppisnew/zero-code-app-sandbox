package com.peng.zerocodeappsandbox.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.peng.zerocodeappsandbox.constant.AppConstant;
import com.peng.zerocodeappsandbox.core.AiCodeGeneratorFacade;
import com.peng.zerocodeappsandbox.exception.BusinessException;
import com.peng.zerocodeappsandbox.exception.ErrorCode;
import com.peng.zerocodeappsandbox.exception.ThrowUtils;
import com.peng.zerocodeappsandbox.mapper.AppMapper;
import com.peng.zerocodeappsandbox.model.dto.app.AppQueryRequest;
import com.peng.zerocodeappsandbox.model.entity.App;
import com.peng.zerocodeappsandbox.model.entity.User;
import com.peng.zerocodeappsandbox.model.enums.CodeGenTypeEnum;
import com.peng.zerocodeappsandbox.model.enums.MessageTypeEnum;
import com.peng.zerocodeappsandbox.model.vo.app.AppVO;
import com.peng.zerocodeappsandbox.service.AppService;
import com.peng.zerocodeappsandbox.service.ChatHistoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author peng
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    private static final Set<String> APP_SORT_FIELDS = Set.of(
            "id", "appName", "cover", "initPrompt", "codeGenType", "deployKey", "deployedTime",
            "priority", "userId", "editTime", "createTime", "updateTime"
    );

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        return appList.stream().map(this::getAppVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getUserAppQueryWrapper(AppQueryRequest appQueryRequest, Long userId) {
        if (appQueryRequest == null || userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String appName = appQueryRequest.getAppName();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", userId)
                .like("appName", appName, StrUtil.isNotBlank(appName));
        addOrderBy(queryWrapper, sortField, sortOrder);
        return queryWrapper;
    }

    @Override
    public QueryWrapper getGoodAppQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String appName = appQueryRequest.getAppName();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("priority", AppConstant.GOOD_APP_PRIORITY)
                .like("appName", appName, StrUtil.isNotBlank(appName));
        addOrderBy(queryWrapper, sortField, sortOrder);
        return queryWrapper;
    }

    @Override
    public QueryWrapper getAdminAppQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("id", id, id != null)
                .eq("codeGenType", codeGenType, StrUtil.isNotBlank(codeGenType))
                .eq("priority", priority, priority != null)
                .eq("userId", userId, userId != null)
                .like("appName", appName, StrUtil.isNotBlank(appName))
                .like("cover", cover, StrUtil.isNotBlank(cover))
                .like("initPrompt", initPrompt, StrUtil.isNotBlank(initPrompt))
                .like("deployKey", deployKey, StrUtil.isNotBlank(deployKey));
        addOrderBy(queryWrapper, sortField, sortOrder);
        return queryWrapper;
    }

    private void addOrderBy(QueryWrapper queryWrapper, String sortField, String sortOrder) {
        if (StrUtil.isNotBlank(sortField) && APP_SORT_FIELDS.contains(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equalsIgnoreCase(sortOrder));
        }
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
        // 5. 保存用户消息
        boolean saveUserMessageResult = chatHistoryService.saveMessage(appId, loginUser.getId(),
                message, MessageTypeEnum.USER);
        ThrowUtils.throwIf(!saveUserMessageResult, ErrorCode.OPERATION_ERROR, "保存用户消息失败");
        // 6. 调用 AI 生成代码，并记录 AI 成功/失败结果
        StringBuilder aiResponseBuilder = new StringBuilder();
        try {
            return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId)
                    .doOnNext(aiResponseBuilder::append)
                    .doOnComplete(() -> saveAiMessage(appId, loginUser.getId(), aiResponseBuilder.toString()))
                    .doOnError(error -> saveAiErrorMessage(appId, loginUser.getId(), error));
        } catch (Exception e) {
            saveAiErrorMessage(appId, loginUser.getId(), e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeAppWithChatHistory(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        boolean removeAppResult = this.removeById(appId);
        ThrowUtils.throwIf(!removeAppResult, ErrorCode.OPERATION_ERROR, "删除应用失败");
        boolean removeChatHistoryResult = chatHistoryService.removeByAppId(appId);
        ThrowUtils.throwIf(!removeChatHistoryResult, ErrorCode.OPERATION_ERROR, "删除对话历史失败");
        return true;
    }

    private void saveAiMessage(Long appId, Long userId, String aiResponse) {
        if (StrUtil.isBlank(aiResponse)) {
            chatHistoryService.saveMessage(appId, userId, "AI 回复为空", MessageTypeEnum.ERROR);
            return;
        }
        chatHistoryService.saveMessage(appId, userId, aiResponse, MessageTypeEnum.AI);
    }

    private void saveAiErrorMessage(Long appId, Long userId, Throwable throwable) {
        String errorMessage = throwable == null ? "AI 回复失败" : throwable.getMessage();
        if (StrUtil.isBlank(errorMessage)) {
            errorMessage = "AI 回复失败";
        }
        chatHistoryService.saveMessage(appId, userId, "AI 回复失败：" + errorMessage, MessageTypeEnum.ERROR);
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限部署该应用，仅本人可以部署
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 没有则生成 6 位 deployKey（大小写字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 构建源目录路径，优先使用当前代码保存器的 {appId}/source 结构
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR
                + File.separator
                + appId
                + File.separator
                + "source";
        File sourceDir = new File(sourceDirPath);
        // 兼容旧版 {codeGenType}_{appId} 结构
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            String codeGenType = app.getCodeGenType();
            String oldSourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR
                    + File.separator
                    + codeGenType + "_" + appId;
            sourceDir = new File(oldSourceDirPath);
        }
        // 6. 检查源目录是否存在
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }
        // 7. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
        }
        // 8. 更新应用的 deployKey 和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 9. 返回可访问的 URL
        return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }

}
