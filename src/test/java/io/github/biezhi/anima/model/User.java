package io.github.biezhi.anima.model;

import io.github.biezhi.anima.Model;
import io.github.biezhi.anima.annotation.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author biezhi
 * @date 2018/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends Model {

    private Integer id;

    @Column(name = "user_name")
    private String userName;

    private Integer age;

    public User(String userName, Integer age) {
        this.userName = userName;
        this.age = age;
    }

}