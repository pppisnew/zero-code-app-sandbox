package com.peng.zerocodeappsandbox.core.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peng.zerocodeappsandbox.core.ai.model.CodeFileResult;
import com.peng.zerocodeappsandbox.core.ai.model.MultiFileCodeResult;
import com.peng.zerocodeappsandbox.exception.BusinessException;
import com.peng.zerocodeappsandbox.exception.ErrorCode;

import java.util.regex.Pattern;

/**
 * 多文件代码解析器
 */
public class MultiFileCodeParser
        implements CodeParser<MultiFileCodeResult> {

    /**
     * 清理 think / markdown
     */
    private static final Pattern CLEAN_PATTERN =
            Pattern.compile(
                    "<think>[\\s\\S]*?</think>|```(?:json)?|```",
                    Pattern.CASE_INSENSITIVE
            );

    /**
     * ObjectMapper
     */
    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper()
                    .configure(
                            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                            false
                    );

    @Override
    public MultiFileCodeResult parseCode(
            String codeContent
    ) {

        if (isBlank(codeContent)) {

            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "模型返回内容为空"
            );
        }

        try {

            // 1. 清理
            String cleaned =
                    CLEAN_PATTERN
                            .matcher(codeContent)
                            .replaceAll("")
                            .replace("\uFEFF", "")
                            .trim();

            // 2. 提取 JSON
            String json =
                    extractJson(cleaned);

            // 3. 修复 JSON
            json =
                    json.replaceAll(
                            ",\\s*([}\\]])",
                            "$1"
                    );

            // 4. 解析
            MultiFileCodeResult result =
                    OBJECT_MAPPER.readValue(
                            json,
                            MultiFileCodeResult.class
                    );

            // 5. 校验 + 默认值
            validate(result);
            fillDefaultValue(result);

            return result;

        } catch (JsonProcessingException e) {

            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "JSON 解析失败: "
                            + e.getOriginalMessage()
            );

        } catch (Exception e) {

            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "AI 返回结果解析失败: "
                            + e.getMessage()
            );
        }
    }

    /**
     * 提取最外层 JSON
     */
    private String extractJson(
            String text
    ) {

        int start =
                text.indexOf('{');

        if (start < 0) {

            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "未检测到 JSON"
            );
        }

        int level = 0;
        boolean inString = false;
        boolean escape = false;

        for (int i = start; i < text.length(); i++) {

            char c = text.charAt(i);

            if (escape) {
                escape = false;
                continue;
            }

            if (c == '\\') {
                escape = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                continue;
            }

            if (inString) {
                continue;
            }

            if (c == '{') {
                level++;
            } else if (c == '}') {

                level--;

                if (level == 0) {

                    return text.substring(
                            start,
                            i + 1
                    );
                }
            }
        }

        throw new BusinessException(
                ErrorCode.SYSTEM_ERROR,
                "JSON 结构不完整"
        );
    }

    /**
     * 校验
     */
    private void validate(
            MultiFileCodeResult result
    ) {

        if (result == null
                || result.getFiles() == null
                || result.getFiles().isEmpty()) {

            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "文件列表不能为空"
            );
        }

        for (CodeFileResult file :
                result.getFiles()) {

            if (isBlank(file.getFilePath())) {

                throw new BusinessException(
                        ErrorCode.SYSTEM_ERROR,
                        "文件路径不能为空"
                );
            }

            if (isBlank(file.getContent())) {

                throw new BusinessException(
                        ErrorCode.SYSTEM_ERROR,
                        "文件内容不能为空"
                );
            }
        }
    }

    /**
     * 默认值
     */
    private void fillDefaultValue(
            MultiFileCodeResult result
    ) {

        if (isBlank(result.getProjectName())) {
            result.setProjectName("generated-project");
        }

        if (result.getDescription() == null) {
            result.setDescription("");
        }

        for (CodeFileResult file :
                result.getFiles()) {

            if (isBlank(file.getFileType())) {

                String path =
                        file.getFilePath()
                                .toLowerCase();

                if (path.endsWith(".html")) {
                    file.setFileType("html");
                } else if (path.endsWith(".css")) {
                    file.setFileType("css");
                } else if (path.endsWith(".js")) {
                    file.setFileType("javascript");
                } else {
                    file.setFileType("text");
                }
            }

            if (isBlank(result.getEntryFile())
                    && file.getFilePath()
                    .endsWith(".html")) {

                result.setEntryFile(
                        file.getFilePath()
                );
            }
        }
    }

    /**
     * blank
     */
    private boolean isBlank(
            String str
    ) {
        return str == null
                || str.isBlank();
    }
}