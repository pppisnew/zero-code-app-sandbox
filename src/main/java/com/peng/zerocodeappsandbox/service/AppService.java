package com.peng.zerocodeappsandbox.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.peng.zerocodeappsandbox.model.dto.app.AppQueryRequest;
import com.peng.zerocodeappsandbox.model.entity.App;
import com.peng.zerocodeappsandbox.model.entity.User;
import com.peng.zerocodeappsandbox.model.vo.app.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author peng
 */
public interface AppService extends IService<App> {

    /**
     * 获取脱敏后的应用信息
     */
    AppVO getAppVO(App app);

    /**
     * 获取脱敏后的应用信息列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 用户自己的应用查询条件
     */
    QueryWrapper getUserAppQueryWrapper(AppQueryRequest appQueryRequest, Long userId);

    /**
     * 精选应用查询条件
     */
    QueryWrapper getGoodAppQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 管理员应用查询条件
     */
    QueryWrapper getAdminAppQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用信息
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 删除应用并清理对话历史
     */
    boolean removeAppWithChatHistory(Long appId);

    /**
     * 部署应用
     */
    String deployApp(Long appId, User loginUser);
}
