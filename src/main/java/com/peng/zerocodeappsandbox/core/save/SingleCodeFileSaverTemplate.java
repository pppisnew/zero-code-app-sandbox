package com.peng.zerocodeappsandbox.core.save;

import cn.hutool.core.util.StrUtil;
import com.peng.zerocodeappsandbox.core.ai.model.CodeFileResult;
import com.peng.zerocodeappsandbox.exception.BusinessException;
import com.peng.zerocodeappsandbox.exception.ErrorCode;
import com.peng.zerocodeappsandbox.model.enums.CodeGenTypeEnum;

/**
 * HTML 单文件保存器
 *
 * @author yupi
 */
public class SingleCodeFileSaverTemplate
        extends CodeFileSaverTemplate<CodeFileResult> {

    /**
     * 默认 HTML 文件名
     */
    private static final String DEFAULT_HTML_FILE_NAME =
            "index.html";

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void saveFiles(
            CodeFileResult result,
            String baseDirPath
    ) {

        writeCodeFile(
                baseDirPath,
                result.getFilePath(),
                result.getContent()
        );
    }

    @Override
    protected void validateInput(
            CodeFileResult result
    ) {

        super.validateInput(result);

        // 默认文件名
        if (StrUtil.isBlank(result.getFilePath())) {

            result.setFilePath(
                    DEFAULT_HTML_FILE_NAME
            );
        }

        // html 文件校验
        if (!result.getFilePath().endsWith(".html")) {

            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "HTML 文件必须以 .html 结尾"
            );
        }

        // 通用文件校验
        validateFile(
                result.getFilePath(),
                result.getContent()
        );
    }
}