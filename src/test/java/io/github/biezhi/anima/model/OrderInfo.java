package io.github.biezhi.anima.model;

import io.github.biezhi.anima.Model;
import io.github.biezhi.anima.annotation.Column;
import io.github.biezhi.anima.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author biezhi
 * @date 2018/3/17
 */
@Table(name = "order_info")
@Data
public class OrderInfo extends Model {

    private Long id;

    @Column(name = "productname")
    private String productName;

    private LocalDateTime createTime;

}
