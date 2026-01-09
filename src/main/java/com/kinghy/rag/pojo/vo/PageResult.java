package com.kinghy.rag.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果封装类
 * 
 * @author yunzhongxiaoma
 * @param <T> 数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页码
     */
    private int current;

    /**
     * 每页大小
     */
    private int size;

    /**
     * 总页数
     */
    private long pages;

    /**
     * 是否有下一页
     */
    private boolean hasNext;

    /**
     * 是否有上一页
     */
    private boolean hasPrevious;

    /**
     * 创建分页结果
     * 
     * @param records 数据列表
     * @param total 总记录数
     * @param current 当前页码
     * @param size 每页大小
     * @param <T> 数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> records, long total, int current, int size) {
        long pages = (total + size - 1) / size; // 向上取整
        
        return PageResult.<T>builder()
                .records(records)
                .total(total)
                .current(current)
                .size(size)
                .pages(pages)
                .hasNext(current < pages)
                .hasPrevious(current > 1)
                .build();
    }

    /**
     * 创建空的分页结果
     */
    public static <T> PageResult<T> empty(int current, int size) {
        return PageResult.<T>builder()
                .records(List.of())
                .total(0)
                .current(current)
                .size(size)
                .pages(0)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }
}