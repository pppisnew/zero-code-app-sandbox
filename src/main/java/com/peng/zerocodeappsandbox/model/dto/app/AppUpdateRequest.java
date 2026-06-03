package com.peng.zerocodeappsandbox.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;
}
