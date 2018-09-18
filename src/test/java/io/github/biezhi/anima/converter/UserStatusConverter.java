package io.github.biezhi.anima.converter;

import org.sql2o.converters.Converter;

/**
 * UserStatus Converter
 *
 * @author biezhi
 * @date 2018/9/18
 */
public class UserStatusConverter implements Converter<UserStatus> {

    @Override
    public UserStatus convert(Object val) {
        if (val == null) {
            return null;
        }
        Integer status = (Integer) val;
        if (status == UserStatus.DISABLE.getStatus()) {
            return UserStatus.DISABLE;
        }
        if (status == UserStatus.NOMARL.getStatus()) {
            return UserStatus.NOMARL;
        }
        return null;
    }

    @Override
    public Object toDatabaseParam(UserStatus val) {
        return val.getStatus();
    }

}
