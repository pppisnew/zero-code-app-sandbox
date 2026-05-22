package com.peng.zerocodeappsandbox.core.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Description("完整可运行的 HTML 内容，必须包含 <!DOCTYPE html>")
@Data
public class HtmlCodeResult {

    @Description("文件路径，例如 index.html")
    private String filePath;

    @Description("文件类型，例如 html css javascript")
    private String fileType;

    @Description("完整文件内容")
    private String content;
}
