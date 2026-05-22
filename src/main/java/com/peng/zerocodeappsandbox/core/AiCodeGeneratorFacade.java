package com.peng.zerocodeappsandbox.core;

import com.peng.zerocodeappsandbox.core.ai.AiCodeGeneratorService;
import com.peng.zerocodeappsandbox.core.ai.model.HtmlCodeResult;
import com.peng.zerocodeappsandbox.core.ai.model.MultiFileCodeResult;
import com.peng.zerocodeappsandbox.core.parser.CodeParserExecutor;
import com.peng.zerocodeappsandbox.core.save.CodeFileSaverExecutor;
import com.peng.zerocodeappsandbox.exception.BusinessException;
import com.peng.zerocodeappsandbox.exception.ErrorCode;
import com.peng.zerocodeappsandbox.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;

/**
 * AI 代码生成门面，组合生成和保存功能
 */
@Slf4j
@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 通用流式代码处理
     *
     * @param codeStream 代码流
     * @param codeGenType 代码生成类型
     * @param <T> 解析结果类型
     * @return Flux
     */
    public <T> Flux<String> processCodeStream(
            Flux<String> codeStream,
            CodeGenTypeEnum codeGenType
    ) {

        StringBuilder codeBuilder = new StringBuilder();

        return codeStream

                // 收集代码
                .doOnNext(codeBuilder::append)

                // 流结束后处理
                .concatWith(
                        Mono.defer(() -> {

                            String completeCode =
                                    codeBuilder.toString();

                            return Mono.fromCallable(() -> {

                                        // 解析
                                        @SuppressWarnings("unchecked")
                                        T parsedResult =
                                                (T) CodeParserExecutor.executeParser(
                                                        completeCode,
                                                        codeGenType
                                                );

                                        // 保存
                                        File savedDir =
                                                CodeFileSaverExecutor.executeSaver(
                                                        parsedResult,
                                                        codeGenType
                                                );

                                        log.info(
                                                "代码保存成功: {}",
                                                savedDir.getAbsolutePath()
                                        );

                                        return "";
                                    })

                                    // IO线程池
                                    .subscribeOn(
                                            Schedulers.boundedElastic()
                                    )

                                    // 错误处理
                                    .onErrorResume(e -> {

                                        log.error(
                                                "代码保存失败",
                                                e
                                        );

                                        return Mono.empty();
                                    });
                        })
                );
    }

    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码（流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }


}
