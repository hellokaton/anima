package com.hellokaton.anima.model;

import com.hellokaton.anima.Model;
import com.hellokaton.anima.enums.VipLevel;
import com.hellokaton.anima.annotation.Column;
import com.hellokaton.anima.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author biezhi
 * @date 2018/6/2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person extends Model {

    private String name;

    @Column(name = "sex")
    private Gender gender;

    private VipLevel vipLevel;

}
