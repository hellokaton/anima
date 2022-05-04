package com.hellokaton.anima;

import com.hellokaton.anima.core.Joins;
import com.hellokaton.anima.enums.OrderBy;
import com.hellokaton.anima.model.Address;
import com.hellokaton.anima.model.OrderInfo;
import com.hellokaton.anima.model.User;
import com.hellokaton.anima.model.UserDto;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static com.hellokaton.anima.Anima.select;

/**
 * Join test
 *
 * @author biezhi
 * @date 2018/4/16
 */
public class JoinTest extends BaseTest {

    @Test
    public void testJoin() {
        // HasOne
        OrderInfo orderInfo = Anima.select().from(OrderInfo.class)
                .join(
                        Joins.with(Address.class).as(OrderInfo::getAddress)
                                .on(OrderInfo::getId, Address::getOrderId)
                )
                .byId(3);

        Assert.assertNotNull(orderInfo);
        Assert.assertNotNull(orderInfo.getAddress());

        orderInfo = Anima.select().from(OrderInfo.class)
                .join(
                        Joins.with(Address.class).as(OrderInfo::getAddress)
                                .on(OrderInfo::getId, Address::getOrderId)
                )
                .join(
                        Joins.with(User.class).as(OrderInfo::getUser)
                                .on(OrderInfo::getUid, User::getId)
                )
                .byId(3);

        Assert.assertNotNull(orderInfo);
        Assert.assertNotNull(orderInfo.getAddress());
        Assert.assertNotNull(orderInfo.getUser());

        // ManyToOne
        orderInfo = Anima.select().from(OrderInfo.class)
                .join(
                        Joins.with(User.class).as(OrderInfo::getUser)
                                .on(OrderInfo::getUid, User::getId)
                )
                .byId(3);

        Assert.assertNotNull(orderInfo);
        Assert.assertNotNull(orderInfo.getUser());
    }

    @Test
    public void testOneToMany(){
        List<UserDto> userDto = Anima.select().from(UserDto.class).join(
                Joins.with(OrderInfo.class).as(UserDto::getOrders)
                        .on(UserDto::getId, OrderInfo::getUid)
        ).all();
        Assert.assertNotNull(userDto);
        Assert.assertNotNull(userDto.get(0).getOrders());
    }
    @Test
    public void testOrderBy() {
        // OneToMany
        UserDto userDto = Anima.select().from(UserDto.class).join(
                Joins.with(OrderInfo.class).as(UserDto::getOrders)
                        .on(UserDto::getId, OrderInfo::getUid)
                        .order(OrderInfo::getId, OrderBy.DESC)
        ).byId(1);

        userDto = Anima.select().from(UserDto.class).join(
                Joins.with(OrderInfo.class).as(UserDto::getOrders)
                        .on(UserDto::getId, OrderInfo::getUid)
                        .order("id asc")
        ).byId(1);

    }
}
