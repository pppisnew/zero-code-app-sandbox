package com.peng.zerocodeappsandbox.common;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CursorPageResponse<T> implements Serializable {

    /**
     * 当前页数据
     */
    private List<T> records;

    /**
     * 是否还有下一页
     */
    private boolean hasMore;

    /**
     * 下一页游标（当前页最后一条的 createTime）
     */
    private LocalDateTime nextCursor;

    /**
     * 下一页游标 id（当前页最后一条的 id）
     */
    private Long nextCursorId;

    private static final long serialVersionUID = 1L;
}
