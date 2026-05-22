package com.peng.zerocodeappsandbox.core.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

import java.util.List;

@Description("完整 HTML 内容，必须通过 link 引入 style.css，并通过 script 引入 script.js")
@Data
public class MultiFileCodeResult {

    @Description("项目名称")
    private String projectName;

    @Description("入口文件")
    private String entryFile;

    @Description("项目描述")
    private String description;

    @Description("生成的文件列表")
    private List<HtmlCodeResult> files;
}

