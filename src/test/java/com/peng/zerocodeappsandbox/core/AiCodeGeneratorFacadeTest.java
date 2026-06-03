package com.peng.zerocodeappsandbox.core;

import com.peng.zerocodeappsandbox.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI代码生成门面测试
 *
 * 简单功能测试
 *
 * @author yupi
 */
@SpringBootTest
@Slf4j
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    /**
     * 同步 HTML 生成测试
     */
    @Test
    void generateAndSaveHtmlCode() {

        File result = aiCodeGeneratorFacade.generateAndSaveCode(
                "生成一个购物网站首页，要求科技风格",
                CodeGenTypeEnum.HTML,
                1L
        );

        assertNotNull(result);

        assertTrue(result.exists());

        assertTrue(result.isDirectory());

        log.info("HTML代码保存路径: {}", result.getAbsolutePath());
    }

    /**
     * 同步多文件生成测试
     */
    @Test
    void generateAndSaveMultiFileCode() {

        File result = aiCodeGeneratorFacade.generateAndSaveCode(
                "生成一个 Todo List 应用，包含 HTML、CSS、JS",
                CodeGenTypeEnum.MULTI_FILE,
                2L
        );

        assertNotNull(result);

        assertTrue(result.exists());

        assertTrue(result.isDirectory());

        log.info("多文件代码保存路径: {}", result.getAbsolutePath());
    }

    /**
     * 流式 HTML 生成测试
     */
    @Test
    void generateAndSaveHtmlCodeStream() throws IOException {

        Flux<String> flux =
                aiCodeGeneratorFacade.generateAndSaveCodeStream(
                        "生成一个个人博客首页",
                        CodeGenTypeEnum.HTML,
                        3L
                );

        String result = flux
                .doOnNext(chunk ->
                        log.info("流式输出: {}", chunk)
                )
                .collectList()
                .map(list -> String.join("", list))
                .block(Duration.ofMinutes(5));

        assertNotNull(result);

        assertFalse(result.isEmpty());

        // 保存 AI 原始响应（强烈推荐）
        Files.writeString(
                Path.of("tmp/ai-result-single.json"),
                result,
                StandardCharsets.UTF_8
        );

        log.info("AI 原始响应已保存");

        log.info("流式生成完成");
    }

    @Test
    void generateAndSaveMultiFileCodeStream() throws Exception {

        Flux<String> flux =
                aiCodeGeneratorFacade.generateAndSaveCodeStream(
                        "生成一个音乐播放器网页",
                        CodeGenTypeEnum.MULTI_FILE,
                        4L
                );

        String result = flux
                .doOnNext(chunk ->
                        log.info("流式输出: {}", chunk)
                )
                .collectList()
                .map(list -> String.join("", list))
                .block(Duration.ofMinutes(5));

        assertNotNull(result);

        assertFalse(result.isEmpty());

        // 保存 AI 原始响应（强烈推荐）
        Files.writeString(
                Path.of("tmp/ai-result.json"),
                result,
                StandardCharsets.UTF_8
        );

        log.info("AI 原始响应已保存");

        log.info("流式多文件生成完成");
    }
}