package com.peng.zerocodeappsandbox.core.parser;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peng.zerocodeappsandbox.core.ai.model.HtmlCodeResult;
import com.peng.zerocodeappsandbox.core.ai.model.MultiFileCodeResult;
import com.peng.zerocodeappsandbox.exception.BusinessException;
import com.peng.zerocodeappsandbox.exception.ErrorCode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 多文件代码解析器
 *
 * @author yupi
 */
public class MultiFileCodeParser
        implements CodeParser<MultiFileCodeResult> {

    /**
     * markdown json
     */
    private static final Pattern MARKDOWN_JSON_PATTERN =
            Pattern.compile(
                    "```json\\s*([\\s\\S]*?)```",
                    Pattern.CASE_INSENSITIVE
            );

    /**
     * 提取最外层 JSON
     */
    private static final Pattern JSON_PATTERN =
            Pattern.compile(
                    "(\\{[\\s\\S]*})"
            );

    /**
     * think 标签
     */
    private static final Pattern THINK_PATTERN =
            Pattern.compile(
                    "<think>[\\s\\S]*?</think>",
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

        if (codeContent == null
                || codeContent.isBlank()) {

            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "模型返回内容为空"
            );
        }

        try {

            // 1. 去 think 标签
            codeContent =
                    THINK_PATTERN
                            .matcher(codeContent)
                            .replaceAll("")
                            .trim();

            // 2. 提取 markdown json
            Matcher markdownMatcher =
                    MARKDOWN_JSON_PATTERN
                            .matcher(codeContent);

            if (markdownMatcher.find()) {

                codeContent =
                        markdownMatcher.group(1);
            }

            // 3. 提取最外层 JSON
            Matcher jsonMatcher =
                    JSON_PATTERN.matcher(codeContent);

            if (jsonMatcher.find()) {

                codeContent =
                        jsonMatcher.group(1);
            }

            // 4. JSON 解析
            MultiFileCodeResult result =
                    OBJECT_MAPPER.readValue(
                            codeContent,
                            MultiFileCodeResult.class
                    );

            // 5. 校验
            validate(result);

            // 6. 默认值
            fillDefaultValue(result);

            return result;

        } catch (Exception e) {

            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "AI 返回结果解析失败: "
                            + e.getMessage()
            );
        }
    }

    /**
     * 校验结果
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

        for (HtmlCodeResult file :
                result.getFiles()) {

            if (file.getFilePath() == null
                    || file.getFilePath().isBlank()) {

                throw new BusinessException(
                        ErrorCode.SYSTEM_ERROR,
                        "文件路径不能为空"
                );
            }

            if (file.getContent() == null
                    || file.getContent().isBlank()) {

                throw new BusinessException(
                        ErrorCode.SYSTEM_ERROR,
                        "文件内容不能为空"
                );
            }
        }
    }

    /**
     * 填充默认值
     */
    private void fillDefaultValue(
            MultiFileCodeResult result
    ) {

        if (result.getProjectName() == null
                || result.getProjectName().isBlank()) {

            result.setProjectName(
                    "generated-project"
            );
        }

        if (result.getDescription() == null) {
            result.setDescription("");
        }

        for (HtmlCodeResult file :
                result.getFiles()) {

            // 推断 fileType
            if (file.getFileType() == null
                    || file.getFileType().isBlank()) {

                String path =
                        file.getFilePath();

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

            // 自动设置 entryFile
            if ((result.getEntryFile() == null
                    || result.getEntryFile().isBlank())
                    && file.getFilePath()
                    .endsWith(".html")) {

                result.setEntryFile(
                        file.getFilePath()
                );
            }
        }
    }
}