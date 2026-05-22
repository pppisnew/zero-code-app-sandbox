package com.peng.zerocodeappsandbox.core.save;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.peng.zerocodeappsandbox.core.ai.model.HtmlCodeResult;
import com.peng.zerocodeappsandbox.exception.BusinessException;
import com.peng.zerocodeappsandbox.exception.ErrorCode;
import com.peng.zerocodeappsandbox.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * HTML代码文件保存器
 *
 * 支持：
 * 1. 动态文件路径
 * 2. 自动创建目录
 * 3. 防止路径穿越
 * 4. 多层级目录
 *
 * @author yupi
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {

    /**
     * 默认 HTML 文件名
     */
    private static final String DEFAULT_HTML_FILE_NAME = "index.html";

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {

        String filePath = buildSafeFilePath(result);

        // 最终完整路径
        String fullFilePath = baseDirPath + File.separator + filePath;

        // 自动创建父目录
        File parentFile = new File(fullFilePath).getParentFile();
        if (parentFile != null) {
            FileUtil.mkdir(parentFile);
        }

        // 写入文件
        FileUtil.writeUtf8String(result.getContent(), fullFilePath);
    }

    @Override
    protected void validateInput(HtmlCodeResult result) {
        super.validateInput(result);

        // content 校验
        if (StrUtil.isBlank(result.getContent())) {
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "HTML 内容不能为空"
            );
        }

        // filePath 校验
        String filePath = result.getFilePath();

        if (StrUtil.isBlank(filePath)) {
            result.setFilePath(DEFAULT_HTML_FILE_NAME);
            return;
        }

        // 防止路径穿越攻击
        if (filePath.contains("..")) {
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "非法文件路径"
            );
        }

        // 必须是 html 文件
        if (!filePath.endsWith(".html")) {
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "HTML 文件必须以 .html 结尾"
            );
        }
    }

    /**
     * 构建安全文件路径
     */
    private String buildSafeFilePath(HtmlCodeResult result) {

        String filePath = result.getFilePath();

        if (StrUtil.isBlank(filePath)) {
            return DEFAULT_HTML_FILE_NAME;
        }

        // 去掉开头 /
        filePath = StrUtil.removePrefix(filePath, "/");

        // windows路径兼容
        filePath = filePath.replace("\\", "/");

        return filePath;
    }
}