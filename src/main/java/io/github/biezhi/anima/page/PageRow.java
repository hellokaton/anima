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