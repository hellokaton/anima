package io.github.biezhi.anima;

import io.github.biezhi.anima.model.Person;
import org.junit.Test;

import java.util.List;

import static io.github.biezhi.anima.Anima.select;

/**
 * @author biezhi
 * @date 2018/6/2
 */
public class EnumTest extends BaseTest {

    @Test
    public void testQueryEnum(){
        System.out.println(select().from(Person.class).count());
        List<Person> all = select().from(Person.class).all();
        System.out.println(all);
    }

}
