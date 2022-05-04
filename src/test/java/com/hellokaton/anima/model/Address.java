package com.hellokaton.anima.model;

import com.hellokaton.anima.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author biezhi
 * @date 2018/3/19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Address extends Model {

    private Long   orderId;
    private String city;
    private String street;

}
