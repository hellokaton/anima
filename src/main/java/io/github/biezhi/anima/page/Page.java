package io.github.biezhi.anima.page;

import lombok.Data;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页对象封装
 *
 * @author biezhi
 * @date 2017/7/24
 */
@Data
public class Page<T> {

    /**
     * 当前页
     */
    private int pageNum = 1;

    /**
     * 每页多少条
     */
    private int limit = 10;

    /**
     * 上一页
     */
    private int prevPage = 1;

    /**
     * 下一页
     */
    private int  nextPage   = 1;

    /**
     * 总页数
     */
    private int  totalPages = 1;

    /**
     * 总记录数
     */
    private long totalRows  = 0L;

    /**
     * 记录行
     */
    private List<T> rows;

    /**
     * 是否为第一页
     */
    private boolean isFirstPage = false;

    /**
     * 是否为最后一页
     */
    private boolean isLastPage = false;

    /**
     * 是否有前一页
     */
    private boolean hasPrevPage = false;

    /**
     * 是否有下一页
     */
    private boolean hasNextPage = false;

    /**
     * 导航页码数
     */
    private int navPages = 8;

    /**
     * 所有导航页号
     */
    private int[] navPageNums;

    public <R> Page<R> map(Function<? super T, ? extends R> mapper) {
        Page<R> page = new Page<>(this.totalRows, this.pageNum, this.limit);
        page.setRows(rows.stream().map(mapper).collect(Collectors.toList()));
        return page;
    }

    public Page() {
    }

    public Page(long total, int page, int limit) {
        init(total, page, limit);
    }

    private void init(long total, int pageNum, int limit) {
        //设置基本参数
        this.totalRows = total;
        this.limit = limit;
        this.totalPages = (int) ((this.totalRows - 1) / this.limit + 1);

        //根据输入可能错误的当前号码进行自动纠正
        if (pageNum < 1) {
            this.pageNum = 1;
        } else if (pageNum > this.totalPages) {
            this.pageNum = this.totalPages;
        } else {
            this.pageNum = pageNum;
        }

        //基本参数设定之后进行导航页面的计算
        this.calcNavigatePageNumbers();

        //以及页面边界的判定
        judgePageBoudary();
    }

    private void calcNavigatePageNumbers() {
        //当总页数小于或等于导航页码数时
        if (this.totalPages <= navPages) {
            navPageNums = new int[totalPages];
            for (int i = 0; i < totalPages; i++) {
                navPageNums[i] = i + 1;
            }
        } else {
            //当总页数大于导航页码数时
            navPageNums = new int[navPages];
            int startNum = pageNum - navPages / 2;
            int endNum   = pageNum + navPages / 2;
            if (startNum < 1) {
                startNum = 1;
                // 最前navPageCount页
                for (int i = 0; i < navPages; i++) {
                    navPageNums[i] = startNum++;
                }
            } else if (endNum > totalPages) {
                endNum = totalPages;
                //最后navPageCount页
                for (int i = navPages - 1; i >= 0; i--) {
                    navPageNums[i] = endNum--;
                }
            } else {
                //所有中间页
                for (int i = 0; i < navPages; i++) {
                    navPageNums[i] = startNum++;
                }
            }
        }
    }

    private void judgePageBoudary() {
        isFirstPage = pageNum == 1;
        isLastPage = pageNum == totalPages && pageNum != 1;
        hasPrevPage = pageNum != 1;
        hasNextPage = pageNum != totalPages;
        if (hasNextPage) {
            nextPage = pageNum + 1;
        }
        if (hasPrevPage) {
            prevPage = pageNum - 1;
        }
    }

}