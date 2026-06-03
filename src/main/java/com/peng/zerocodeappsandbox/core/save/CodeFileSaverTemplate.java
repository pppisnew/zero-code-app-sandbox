package com.peng.zerocodeappsandbox.core.save;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.peng.zerocodeappsandbox.constant.AppConstant;
import com.peng.zerocodeappsandbox.exception.BusinessException;
import com.peng.zerocodeappsandbox.exception.ErrorCode;
import com.peng.zerocodeappsandbox.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 抽象代码文件保存器 - 模板方法模式
 *
 * @author yupi
 */
public abstract class CodeFileSaverTemplate<T> {

    /**
     * 文件保存根目录
     */
    protected static final String FILE_SAVE_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;

    /**
     * 模板方法：保存代码
     *
     * @param appId 应用 id
     * @param result 代码结果
     * @return 保存目录
     */
    public final File saveCode(
            Long appId,
            T result
    ) {

        // 1. 校验 appId
        validateAppId(appId);

        // 2. 校验结果
        validateInput(result);

        // 3. 构建应用目录
        String baseDirPath = buildAppDir(appId);

        // 4. 保存文件
        saveFiles(result, baseDirPath);

        // 5. 返回目录
        return new File(baseDirPath);
    }

    /**
     * 校验 appId
     */
    protected void validateAppId(Long appId) {

        if (appId == null || appId <= 0) {

            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "appId 非法"
            );
        }
    }

    /**
     * 输入校验（子类可扩展）
     */
    protected void validateInput(T result) {

        if (result == null) {

            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "代码结果不能为空"
            );
        }
    }

    /**
     * 构建应用目录
     */
    protected final String buildAppDir(
            Long appId
    ) {

        String dirPath =
                FILE_SAVE_ROOT_DIR
                        + File.separator
                        + appId
                        + File.separator
                        + "source";

        FileUtil.mkdir(dirPath);

        return dirPath;
    }

    /**
     * 写入代码文件
     */
    protected final void writeCodeFile(
            String baseDirPath,
            String filePath,
            String content
    ) {

        validateFile(filePath, content);

        String safeFilePath =
                buildSafeFilePath(filePath);

        String fullFilePath =
                baseDirPath
                        + File.separator
                        + safeFilePath;

        // 创建父目录
        File parentFile =
                new File(fullFilePath).getParentFile();

        if (parentFile != null) {
            FileUtil.mkdir(parentFile);
        }

        // 写入文件
        FileUtil.writeString(
                content,
                fullFilePath,
                StandardCharsets.UTF_8
        );
    }

    /**
     * 文件基础校验
     */
    protected void validateFile(
            String filePath,
            String content
    ) {

        if (StrUtil.isBlank(filePath)) {

            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "文件路径不能为空"
            );
        }

        if (StrUtil.isBlank(content)) {

            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "文件内容不能为空"
            );
        }
    }

    /**
     * 构建安全文件路径
     */
    protected final String buildSafeFilePath(
            String filePath
    ) {

        // windows 路径兼容
        filePath = filePath.replace("\\", "/");

        // 去掉开头 /
        filePath = StrUtil.removePrefix(filePath, "/");

        // 防止路径穿越
        if (filePath.contains("..")) {

            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "非法文件路径"
            );
        }

        return filePath;
    }

    /**
     * 获取代码类型
     */
    protected abstract CodeGenTypeEnum getCodeType();

    /**
     * 保存文件
     */
    protected abstract void saveFiles(
            T result,
            String baseDirPath
    );
}