package io.github.biezhi.anima.model;

import io.github.biezhi.anima.Model;
import io.github.biezhi.anima.annotation.Column;
import io.github.biezhi.anima.enums.Gender;
import io.github.biezhi.anima.enums.VipLevel;
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
