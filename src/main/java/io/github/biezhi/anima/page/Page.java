/**
 * Copyright (c) 2018, biezhi 王爵 (biezhi.me@gmail.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.biezhi.anima.page;

import lombok.Data;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class Page<T> {

    /**
     * current pageNum number
     */
    private int pageNum = 1;

    /**
     * How many pages per pageNum
     */
    private int limit = 10;

    /**
     * prev pageNum number
     */
    private int prevPage = 1;

    /**
     * next pageNum number
     */
    private int  nextPage   = 1;

    /**
     * total pageNum count
     */
    private int  totalPages = 1;

    /**
     * total row count
     */
    private long totalRows  = 0L;

    /**
     * row list
     */
    private List<T> rows;

    /**
     * is first pageNum
     */
    private boolean isFirstPage = false;

    /**
     * is last pageNum
     */
    private boolean isLastPage = false;

    /**
     * has prev pageNum
     */
    private boolean hasPrevPage = false;

    /**
     * has next pageNum
     */
    private boolean hasNextPage = false;

    /**
     * navigation pageNum number
     */
    private int navPages = 8;

    /**
     * all navigation pageNum number
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
        // set basic params
        this.totalRows = total;
        this.limit = limit;
        this.totalPages = (int) ((this.totalRows - 1) / this.limit + 1);

        // automatic correction based on the current number of the wrong input
        if (pageNum < 1) {
            this.pageNum = 1;
        } else if (pageNum > this.totalPages) {
            this.pageNum = this.totalPages;
        } else {
            this.pageNum = pageNum;
        }

        // calculation of navigation pageNum after basic parameter setting
        this.calcNavigatePageNumbers();

        // and the determination of pageNum boundaries
        judgePageBoudary();
    }

    private void calcNavigatePageNumbers() {
        // when the total number of pages is less than or equal to the number of navigation pages
        if (this.totalPages <= navPages) {
            navPageNums = new int[totalPages];
            for (int i = 0; i < totalPages; i++) {
                navPageNums[i] = i + 1;
            }
        } else {
            // when the total number of pages is greater than the number of navigation pages
            navPageNums = new int[navPages];
            int startNum = pageNum - navPages / 2;
            int endNum   = pageNum + navPages / 2;
            if (startNum < 1) {
                startNum = 1;
                for (int i = 0; i < navPages; i++) {
                    navPageNums[i] = startNum++;
                }
            } else if (endNum > totalPages) {
                endNum = totalPages;
                for (int i = navPages - 1; i >= 0; i--) {
                    navPageNums[i] = endNum--;
                }
            } else {
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