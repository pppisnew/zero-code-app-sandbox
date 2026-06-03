package com.peng.zerocodeappsandbox.core.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peng.zerocodeappsandbox.core.ai.model.CodeFileResult;
import com.peng.zerocodeappsandbox.exception.BusinessException;
import com.peng.zerocodeappsandbox.exception.ErrorCode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工业级 CodeFile 解析器（稳定版）
 *
 * 支持：
 * 1. JSON（优先）
 * 2. markdown JSON
 * 3. markdown HTML
 * 4. 裸 HTML（弱识别）
 * 5. fallback 自动修复
 */
public class SingleCodeFileParser implements CodeParser<CodeFileResult> {

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final String DEFAULT_FILE_PATH = "index.html";

    // markdown json
    private static final Pattern JSON_BLOCK_PATTERN =
            Pattern.compile("```json\\s*([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    // markdown html
    private static final Pattern HTML_BLOCK_PATTERN =
            Pattern.compile("```html\\s*([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    // 弱 HTML 匹配（关键优化）
    private static final Pattern HTML_PATTERN =
            Pattern.compile(
                    "(<!DOCTYPE[\\s\\S]*?</html>|<html[\\s\\S]*?</html>)",
                    Pattern.CASE_INSENSITIVE
            );

    @Override
    public CodeFileResult parseCode(String codeContent) {

        if (codeContent == null || codeContent.trim().isEmpty()) {
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "模型返回内容为空"
            );
        }

        // 1. 统一预处理（关键优化）
        String content = normalize(codeContent.trim());

        // 2. 直接 JSON（最优先）
        CodeFileResult directJson = tryParseJson(content);
        if (directJson != null) {
            return directJson;
        }

        // 3. markdown JSON
        String jsonBlock = extract(content, JSON_BLOCK_PATTERN);
        if (jsonBlock != null) {
            CodeFileResult jsonResult = tryParseJson(jsonBlock);
            if (jsonResult != null) {
                return jsonResult;
            }
        }

        // 4. markdown HTML
        String htmlBlock = extract(content, HTML_BLOCK_PATTERN);
        if (htmlBlock != null) {
            return buildHtmlResult(htmlBlock);
        }

        // 5. 裸 HTML（弱识别）
        String html = extract(content, HTML_PATTERN);
        if (html != null) {
            return buildHtmlResult(html);
        }

        // 6. 最终 fallback（避免脏数据）
        if (looksLikeHtml(content)) {
            return buildHtmlResult(content);
        }

        // 7. 最后兜底：当 JSON / HTML 都不是 → 仍按 HTML 处理，但已 normalize
        return buildHtmlResult(content);
    }

    /**
     * JSON 解析（安全模式）
     */
    private CodeFileResult tryParseJson(String json) {
        try {
            CodeFileResult result = OBJECT_MAPPER.readValue(json, CodeFileResult.class);

            if (result.getContent() != null && !result.getContent().trim().isEmpty()) {
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
    private CodeFileResult buildHtmlResult(String htmlContent) {
        CodeFileResult result = new CodeFileResult();
        result.setFilePath(DEFAULT_FILE_PATH);
        result.setFileType("html");
        result.setContent(htmlContent);
        return result;
    }

    /**
     * 默认字段补全
     */
    private void fillDefaultFields(CodeFileResult result) {
        if (result.getFilePath() == null || result.getFilePath().isBlank()) {
            result.setFilePath(DEFAULT_FILE_PATH);
        }
        if (result.getFileType() == null || result.getFileType().isBlank()) {
            result.setFileType("html");
        }
    }

    /**
     * 正则提取
     */
    private String extract(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * ⭐ 关键优化：输入归一化（解决 \n \" 问题）
     */
    private String normalize(String input) {
        return input
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\t", "\t");
    }

    /**
     * ⭐ HTML 轻量识别（防误判）
     */
    private boolean looksLikeHtml(String s) {
        String lower = s.toLowerCase();

        return lower.contains("<html")
                || lower.contains("<!doctype")
                || lower.contains("<body")
                || lower.contains("<div")
                || lower.contains("<head");
    }
}