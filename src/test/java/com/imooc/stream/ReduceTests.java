package com.imooc.stream;

import com.imooc.stream.domain.User;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReduceTests {
    private static final User[] arrayOfUsers = {
            User.builder().id(1L).username("zhangsan").name("张三").enabled(true).age(30).mobile("13000000001").build(),
            User.builder().id(2L).username("lisi").name("李四").enabled(false).age(32).mobile("13000000002").build(),
            User.builder().id(3L).username("wangwu").name("王五").enabled(true).age(41).mobile("13000000003").build(),
    };

    private List<User> userList;

    @BeforeEach
    void setup() {
        userList = Arrays.asList(arrayOfUsers);
    }

    @Test
    public void givenUsers_thenReduceToMaxId() {
        Optional<User> userOptional = userList.stream()
//                .filter(user -> !user.isEnabled()) 如果不足两个，是不会进入 reduce 函数的
                .reduce((acc, curr) -> acc.getId() > curr.getId() ? acc : curr);
        assertTrue(userOptional.isPresent());
        assertEquals(3L, userOptional.get().getId());
    }

    @Test
    public void givenUsers_thenCompareReduceAndCollect() {
        // 设计上，reduce 应该和不可变对象一起工作。
        // 如果使用可变对象，也可以得到结果，但是不是线程安全的
        // 而且通常意义上来说， reduce 的性能要弱于 collect
        // 但 reduce 是一个非常灵活的选项，在各个语言和框架中有广泛应用
        Integer sumByReduce = userList
                .stream()
                .map(User::getAge)
                .reduce(0, (Integer acc, Integer curr) -> Integer.sum(acc, curr));
        assertEquals(103, sumByReduce);
        // collect 仅可以使用可变的容器
        MutableInt sumByCollect = userList.stream().collect(
                MutableInt::new,
                (MutableInt container, User user) -> container.add(user.getAge()),
                MutableInt::add);
        assertEquals(103, sumByCollect.getValue());
    }

    @Test
    public void givenUsers_thenReduceToCount() {
        Integer count = userList.stream().reduce(0, (acc, curr) -> acc + 1, Integer::sum);
        assertEquals(3, count);
    }

    @Test
    public void givenUsers_thenReduceToList() {
        List<User> list = userList.parallelStream().reduce(
                Collections.emptyList(),
                (acc, curr) -> Stream.concat(
                        acc.stream(),
                        Stream.of(curr)
                ).collect(toList()),
                // combiner 这个函数的作用主要是考虑并行流
                // 并行流的情况下，一个流会分成多个分片进行处理
                // 每一个分片会产生一个临时的中间结果
                // combiner 的作用是把这些中间结果再合并成一个最终结果
                (left, right) -> {
                    return Stream.concat(left.stream(), right.stream()).collect(toList());
                }
        );
        assertEquals(3, list.size());
    }
}
