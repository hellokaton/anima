package io.github.biezhi.anima;

import io.github.biezhi.anima.annotation.BelongsTo;
import io.github.biezhi.anima.annotation.HasMany;
import io.github.biezhi.anima.annotation.HasOne;
import io.github.biezhi.anima.model.OrderInfo;
import io.github.biezhi.anima.model.UserDto;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static io.github.biezhi.anima.Anima.select;

/**
 * OneToMany
 * BelongsTo
 * hasOne
 * hasMany
 * hasAndBelongsToMany
 *
 * @author biezhi
 * @date 2018/3/19
 */
public class RelateTest extends BaseTest {

    @Test
    public void testBelongsTo() {
        OrderInfo orderInfo = select().from(OrderInfo.class).order("id desc").one();
        Assert.assertNotNull(orderInfo);
        Assert.assertNotNull(orderInfo.getUser());

        orderInfo = select().from(OrderInfo.class).exclude(BelongsTo.class).order("id desc").one();
        Assert.assertNotNull(orderInfo);
        Assert.assertNull(orderInfo.getUser());
    }

    @Test
    public void testBelongsTos() {
        List<OrderInfo> orders = select().from(OrderInfo.class).order("id desc").limit(4);
        Assert.assertNotNull(orders);
        Assert.assertNotNull(orders.stream().filter(order -> order.getId().equals(4L)).map(OrderInfo::getUser));

        OrderInfo orderInfo = select().from(OrderInfo.class).exclude(BelongsTo.class).byId(3);
        Assert.assertNotNull(orderInfo);
        Assert.assertNotNull(orderInfo.getAddress());
        Assert.assertNull(orderInfo.getUser());

        orderInfo = select().from(OrderInfo.class).exclude(HasOne.class).byId(3);
        Assert.assertNotNull(orderInfo);
        Assert.assertNotNull(orderInfo.getUser());
        Assert.assertNull(orderInfo.getAddress());

    }

    @Test
    public void testHasOne() {
        OrderInfo orderInfo = select().from(OrderInfo.class).byId(3);
        Assert.assertNotNull(orderInfo);
        Assert.assertNotNull(orderInfo.getAddress());
        System.out.println(orderInfo.getAddress());

        orderInfo = select().from(OrderInfo.class).exclude(HasOne.class).byId(3);
        Assert.assertNotNull(orderInfo);
        Assert.assertNull(orderInfo.getAddress());
    }

    @Test
    public void testHasMany() {
        UserDto userDto = select().from(UserDto.class).byId(1);
        Assert.assertNotNull(userDto);
        Assert.assertNotNull(userDto.getOrders());
        System.out.println(userDto.getOrders());

        userDto = select().from(UserDto.class).exclude(HasMany.class).byId(1);
        Assert.assertNotNull(userDto);
        Assert.assertNull(userDto.getOrders());
    }

}
