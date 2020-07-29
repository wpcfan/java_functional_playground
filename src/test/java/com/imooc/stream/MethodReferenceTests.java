package com.imooc.stream;

import com.imooc.stream.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// chap02
@Slf4j
public class MethodReferenceTests {

    @Test
    public void givenNewInstance_thenReferenceConstructorInFunction() {
//        Supplier<User> supplier = () -> new User();
        Supplier<User> supplier = User::new;
        assertNotNull(supplier.get());
    }

    @Test
    public void givenInstance_thenReferenceInFunction() {
        User user = User.builder().username("zhangsan").build();
//        Supplier<String> supplier = () -> user.getUsername();
        Supplier<String> supplier = user::getUsername;
        assertEquals("zhangsan", supplier.get());
    }

    @Test
    public void givenStringIndex_thenReferenceInFunction() {
//        BiFunction<String, String, Integer> paramRef = (a, b) -> a.indexOf(b);
        BiFunction<String, String, Integer> paramRef = String::indexOf;
        assertEquals(-1, paramRef.apply("Hello", "World"));
    }

    @Test
    public void givenStaticMethod_thenReferenceInFunction() {
//        Greeting greeting = (a, b) -> Player.sayHello(a, b);
        Greeting greeting = Player::sayHello;
        assertEquals("Hello: World", greeting.sayHello("Hello", "World"));
    }

    interface Greeting {
        String sayHello(String s1, String s2);
    }

    static class Player {
        static String sayHello(String s1, String s2) {
            return s1 + ": " + s2;
        }
    }
}
