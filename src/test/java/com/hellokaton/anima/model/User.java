package com.hellokaton.anima.model;

import com.hellokaton.anima.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends Model {

    private Integer id;

    private String userName;

    private Integer age;

    public User(String userName, Integer age) {
        this.userName = userName;
        this.age = age;
    }

}