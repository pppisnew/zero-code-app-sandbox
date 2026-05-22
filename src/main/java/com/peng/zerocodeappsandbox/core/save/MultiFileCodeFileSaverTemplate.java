package com.peng.zerocodeappsandbox.core.save;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.peng.zerocodeappsandbox.core.ai.model.HtmlCodeResult;
import com.peng.zerocodeappsandbox.core.ai.model.MultiFileCodeResult;
import com.peng.zerocodeappsandbox.exception.BusinessException;
import com.peng.zerocodeappsandbox.exception.ErrorCode;
import com.peng.zerocodeappsandbox.model.enums.CodeGenTypeEnum;

import java.io.File;
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

        List<HtmlCodeResult> files = result.getFiles();

        for (HtmlCodeResult file : files) {

            if (file == null) {
                continue;
            }

            String safeFilePath = buildSafeFilePath(
                    file.getFilePath()
            );

            String fullFilePath =
                    baseDirPath
                            + File.separator
                            + safeFilePath;

            // 创建父目录
            File parentDir =
                    new File(fullFilePath).getParentFile();

            if (parentDir != null) {
                FileUtil.mkdir(parentDir);
            }

            // 写入文件
            FileUtil.writeUtf8String(
                    file.getContent(),
                    fullFilePath
            );
        }
    }

    @Override
    protected void validateInput(
            MultiFileCodeResult result
    ) {

        super.validateInput(result);

        List<HtmlCodeResult> files = result.getFiles();

        if (files == null || files.isEmpty()) {

            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "文件列表不能为空"
            );
        }

        boolean hasEntryFile = false;

        for (HtmlCodeResult file : files) {

            validateFile(file);

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

    /**
     * 校验单个文件
     */
    private void validateFile(HtmlCodeResult file) {

        if (file == null) {

            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "文件不能为空"
            );
        }

        // 文件路径
        if (StrUtil.isBlank(file.getFilePath())) {

            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "文件路径不能为空"
            );
        }

        // 文件内容
        if (StrUtil.isBlank(file.getContent())) {

            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "文件内容不能为空"
            );
        }

        // 防止路径穿越
        if (file.getFilePath().contains("..")) {

            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "非法文件路径"
            );
        }
    }

    /**
     * 构建安全文件路径
     */
    private String buildSafeFilePath(String filePath) {

        // windows 路径兼容
        filePath = filePath.replace("\\", "/");

        // 去掉开头 /
        filePath = StrUtil.removePrefix(filePath, "/");

        return filePath;
    }
}