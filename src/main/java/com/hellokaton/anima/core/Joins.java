package com.hellokaton.anima.core;

import com.hellokaton.anima.Model;

/**
 * @author biezhi
 * @date 2018/4/16
 */
public class Joins {

    public static JoinParam with(Class<? extends Model> joinModel) {
        return new JoinParam(joinModel);
    }

}
