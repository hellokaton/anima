package com.hellokaton.anima.mysql;

import com.hellokaton.anima.Anima;
import com.hellokaton.anima.model.OrderInfo;

import static com.hellokaton.anima.Anima.select;

/**
 * @author biezhi
 * @date 2018/3/17
 */
public class OrderInfoTest {

//    @Test
    public void testQuery() {
        OrderInfo orderInfo = Anima.select().from(OrderInfo.class).one();
        System.out.println(orderInfo);
    }

}
