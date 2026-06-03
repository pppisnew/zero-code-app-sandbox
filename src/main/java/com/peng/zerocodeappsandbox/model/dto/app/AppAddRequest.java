package com.peng.zerocodeappsandbox.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用封面
     */
    private String cover;

    /**
     * 初始化 prompt
     */
    private String initPrompt;

    /**
     * 代码生成类型
     */
    private String codeGenType;
}
