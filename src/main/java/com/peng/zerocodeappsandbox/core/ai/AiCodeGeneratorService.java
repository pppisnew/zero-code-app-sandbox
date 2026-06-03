package com.peng.zerocodeappsandbox.core.ai;

import com.peng.zerocodeappsandbox.core.ai.model.CodeFileResult;
import com.peng.zerocodeappsandbox.core.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {

    /**
     * 生成 HTML 代码
     *
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/html-system-prompt.txt")
    CodeFileResult generateHtmlCode(@MemoryId Long memoryId, @UserMessage String userMessage);

    /**
     * 生成多文件代码
     *
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(@MemoryId Long memoryId, @UserMessage String userMessage);

    /**
     * 生成 HTML 代码（流式）
     *
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(@MemoryId Long memoryId, @UserMessage String userMessage);

    /**
     * 生成多文件代码（流式）
     *
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(@MemoryId Long memoryId, @UserMessage String userMessage);

}
