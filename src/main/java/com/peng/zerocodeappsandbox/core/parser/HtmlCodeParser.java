package com.peng.zerocodeappsandbox.core.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peng.zerocodeappsandbox.core.ai.model.HtmlCodeResult;
import com.peng.zerocodeappsandbox.exception.BusinessException;
import com.peng.zerocodeappsandbox.exception.ErrorCode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML 文件解析器
 *
 * 支持：
 * 1. JSON结构化输出
 * 2. markdown json
 * 3. markdown html
 * 4. 裸 HTML
 *
 * 最终统一返回 HtmlCodeResult
 *
 * @author yupi
 */
public class HtmlCodeParser
        implements CodeParser<HtmlCodeResult> {

    /**
     * markdown json
     */
    private static final Pattern JSON_BLOCK_PATTERN =
            Pattern.compile(
                    "```json\\s*([\\s\\S]*?)```",
                    Pattern.CASE_INSENSITIVE
            );

    /**
     * markdown html
     */
    private static final Pattern HTML_BLOCK_PATTERN =
            Pattern.compile(
                    "```html\\s*([\\s\\S]*?)```",
                    Pattern.CASE_INSENSITIVE
            );

    /**
     * 完整 HTML
     */
    private static final Pattern HTML_PATTERN =
            Pattern.compile(
                    "(<!DOCTYPE\\s+html[\\s\\S]*?</html>)",
                    Pattern.CASE_INSENSITIVE
            );

    /**
     * Jackson
     */
    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper()
                    .configure(
                            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                            false
                    );

    /**
     * 默认文件名
     */
    private static final String DEFAULT_FILE_PATH =
            "index.html";

    @Override
    public HtmlCodeResult parseCode(String codeContent) {

        if (codeContent == null
                || codeContent.trim().isEmpty()) {

            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "模型返回内容为空"
            );
        }

        String content = codeContent.trim();

        // 1. 优先解析 JSON
        HtmlCodeResult jsonResult =
                tryParseJson(content);

        if (jsonResult != null) {
            return jsonResult;
        }

        // 2. markdown json
        String jsonBlock =
                extract(content, JSON_BLOCK_PATTERN);

        if (jsonBlock != null) {

            HtmlCodeResult blockResult =
                    tryParseJson(jsonBlock);

            if (blockResult != null) {
                return blockResult;
            }
        }

        // 3. markdown html
        String htmlBlock =
                extract(content, HTML_BLOCK_PATTERN);

        if (htmlBlock != null) {

            return buildHtmlResult(
                    htmlBlock.trim()
            );
        }

        // 4. 裸 HTML
        String html =
                extract(content, HTML_PATTERN);

        if (html != null) {

            return buildHtmlResult(
                    html.trim()
            );
        }

        // 5. 最终兜底
        return buildHtmlResult(content);
    }

    /**
     * 解析 JSON
     */
    private HtmlCodeResult tryParseJson(String json) {

        try {

            HtmlCodeResult result =
                    OBJECT_MAPPER.readValue(
                            json,
                            HtmlCodeResult.class
                    );

            if (result.getContent() != null
                    && !result.getContent()
                    .trim()
                    .isEmpty()) {

                fillDefaultFields(result);

                return result;
            }

        } catch (JsonProcessingException ignored) {
        }

        return null;
    }

    /**
     * 构建 HTML 结果
     */
    private HtmlCodeResult buildHtmlResult(
            String htmlContent
    ) {

        HtmlCodeResult result =
                new HtmlCodeResult();

        result.setFilePath(DEFAULT_FILE_PATH);

        result.setFileType("html");

        result.setContent(htmlContent);

        return result;
    }

    /**
     * 填充默认字段
     */
    private void fillDefaultFields(
            HtmlCodeResult result
    ) {

        if (result.getFilePath() == null
                || result.getFilePath().isBlank()) {

            result.setFilePath(DEFAULT_FILE_PATH);
        }

        if (result.getFileType() == null
                || result.getFileType().isBlank()) {

            result.setFileType("html");
        }
    }

    /**
     * 正则提取
     */
    private String extract(
            String content,
            Pattern pattern
    ) {

        Matcher matcher =
                pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }
}