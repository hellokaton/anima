package io.github.biezhi.anima.model;

import io.github.biezhi.anima.Model;
import io.github.biezhi.anima.annotation.BelongsTo;
import io.github.biezhi.anima.annotation.Column;
import io.github.biezhi.anima.annotation.HasOne;
import io.github.biezhi.anima.annotation.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author biezhi
 * @date 2018/3/17
 */
@Table(name = "order_info")
@NoArgsConstructor
@Data
public class OrderInfo extends Model {

    private Long id;

    private Integer uid;

    @Column(name = "productname")
    private String productName;

    private LocalDateTime createTime;

    public OrderInfo(Integer uid, String productName) {
        this.uid = uid;
        this.productName = productName;
        this.createTime = LocalDateTime.now();
    }

    @BelongsTo(fk = "uid")
    private User user;

    @HasOne(fk = "order_id")
    private Address address;

}
