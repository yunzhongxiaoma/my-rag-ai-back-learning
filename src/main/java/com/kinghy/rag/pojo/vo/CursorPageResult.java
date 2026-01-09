package com.kinghy.rag.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 游标分页结果封装类
 * 
 * @author yunzhongxiaoma
 * @param <T> 数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorPageResult<T> {

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 当前游标
     */
    private String cursor;

    /**
     * 下一页游标
     */
    private String nextCursor;

    /**
     * 每页大小
     */
    private int size;

    /**
     * 是否有下一页
     */
    private boolean hasNext;

    /**
     * 实际返回的记录数
     */
    private int count;

    /**
     * 创建游标分页结果
     * 
     * @param records 数据列表
     * @param cursor 当前游标
     * @param nextCursor 下一页游标
     * @param size 每页大小
     * @param hasNext 是否有下一页
     * @param <T> 数据类型
     * @return 游标分页结果
     */
    public static <T> CursorPageResult<T> of(List<T> records, String cursor, String nextCursor, int size, boolean hasNext) {
        return CursorPageResult.<T>builder()
                .records(records)
                .cursor(cursor)
                .nextCursor(nextCursor)
                .size(size)
                .hasNext(hasNext)
                .count(records.size())
                .build();
    }

    /**
     * 创建空的游标分页结果
     */
    public static <T> CursorPageResult<T> empty(String cursor, int size) {
        return CursorPageResult.<T>builder()
                .records(List.of())
                .cursor(cursor)
                .nextCursor(null)
                .size(size)
                .hasNext(false)
                .count(0)
                .build();
    }
}