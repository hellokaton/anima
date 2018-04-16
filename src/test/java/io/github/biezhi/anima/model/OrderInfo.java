package io.github.biezhi.anima.model;

import io.github.biezhi.anima.Model;
import io.github.biezhi.anima.annotation.Column;
import io.github.biezhi.anima.annotation.Ignore;
import io.github.biezhi.anima.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author biezhi
 * @date 2018/3/17
 */
@Table(name = "order_info")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
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

    @Ignore
    private User user;

    @Ignore
    private Address address;

}
