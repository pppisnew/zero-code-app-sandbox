package com.peng.zerocodeappsandbox.core.save;

import cn.hutool.core.util.StrUtil;
import com.peng.zerocodeappsandbox.core.ai.model.CodeFileResult;
import com.peng.zerocodeappsandbox.core.ai.model.MultiFileCodeResult;
import com.peng.zerocodeappsandbox.exception.BusinessException;
import com.peng.zerocodeappsandbox.exception.ErrorCode;
import com.peng.zerocodeappsandbox.model.enums.CodeGenTypeEnum;

import java.util.List;

/**
 * 多文件代码保存器
 *
 * @author yupi
 */
public class MultiFileCodeFileSaverTemplate
        extends CodeFileSaverTemplate<MultiFileCodeResult> {

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    @Override
    protected void saveFiles(
            MultiFileCodeResult result,
            String baseDirPath
    ) {

        for (CodeFileResult file : result.getFiles()) {

            if (file == null) {
                continue;
            }

            writeCodeFile(
                    baseDirPath,
                    file.getFilePath(),
                    file.getContent()
            );
        }
    }

    @Override
    protected void validateInput(
            MultiFileCodeResult result
    ) {

        super.validateInput(result);

        List<CodeFileResult> files =
                result.getFiles();

        if (files == null || files.isEmpty()) {

            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "文件列表不能为空"
            );
        }

        boolean hasEntryFile = false;

        for (CodeFileResult file : files) {

            if (file == null) {
                continue;
            }

            validateFile(
                    file.getFilePath(),
                    file.getContent()
            );

            // 检查入口文件
            if (StrUtil.equals(
                    file.getFilePath(),
                    result.getEntryFile()
            )) {

                hasEntryFile = true;
            }
        }

        if (!hasEntryFile) {

            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "入口文件不存在"
            );
        }
    }
}