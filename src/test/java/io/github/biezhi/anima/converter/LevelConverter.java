package io.github.biezhi.anima.converter;

import io.github.biezhi.anima.enums.VipLevel;
import org.sql2o.converters.Converter;

/**
 * @author biezhi
 * @date 2018/9/18
 */
public class LevelConverter implements Converter<VipLevel> {

    @Override
    public VipLevel convert(Object val) {
        Integer intValue = (Integer) val;

        switch (intValue) {
            case 1:
                return VipLevel.VIP1;
            case 2:
                return VipLevel.VIP1;
            case 3:
                return VipLevel.VIP3;
            default:
                return null;
        }
    }

    @Override
    public Object toDatabaseParam(VipLevel val) {
        return val.getCode();
    }

}
