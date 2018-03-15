package io.github.biezhi.anima.page;

import lombok.Data;

@Data
public class PageRow {

    private int page;
    private int offset;
    private int position;
    private int limit;

    public PageRow(int page, int limit) {
        this.page = page;
        this.limit = limit;

        this.offset = this.page > 0 ? (this.page - 1) * this.limit : 0;
        this.position = this.offset + this.limit * (this.page > 0 ? 1 : 0);
    }

}