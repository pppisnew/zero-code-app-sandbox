package com.peng.zerocodeappsandbox.ai;

import com.peng.zerocodeappsandbox.core.ai.AiCodeGeneratorService;
import com.peng.zerocodeappsandbox.core.ai.model.HtmlCodeResult;
import com.peng.zerocodeappsandbox.core.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult htmlCode = aiCodeGeneratorService.generateHtmlCode("请生成一个购物网站主页");
        Assertions.assertNotNull(htmlCode);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult multiFileCode = aiCodeGeneratorService.generateMultiFileCode("请生成一个购物网站主页");
        Assertions.assertNotNull(multiFileCode);
    }
}