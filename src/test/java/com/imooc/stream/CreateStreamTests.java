package com.imooc.stream;

import com.imooc.stream.domain.User;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class CreateStreamTests {
    private static final User[] arrayOfUsers = {
            User.builder().id(1L).username("zhangsan").name("张三").enabled(true).mobile("13000000001").build(),
            User.builder().id(2L).username("lisi").name("李四").enabled(false).mobile("13000000002").build(),
            User.builder().id(3L).username("wangwu").name("王五").enabled(true).mobile("13000000003").build(),
    };

    private List<User> userList;

    @BeforeEach
    void setup() {
        userList = Arrays.asList(arrayOfUsers);
    }

    @Test
    public void givenUsers_createStreamWithArray() {
        val list = Arrays.stream(arrayOfUsers)
                .peek(user -> log.debug("user: {}", user.getUsername()))
                .collect(toList());
        assertEquals(arrayOfUsers.length, list.size());
    }

    @Test
    public void givenUsers_createStreamWithList() {
        val list = userList.stream()
                .peek(user -> log.debug("user: {}", user.getUsername()))
                .collect(toList());
        assertEquals(userList.size(), list.size());
    }

    @Test
    public void givenUsers_createStreamWithStreamOf() {
        val list = Stream.of(arrayOfUsers[0], arrayOfUsers[1], arrayOfUsers[2])
                .peek(user -> log.debug("user: {}", user.getUsername()))
                .collect(toList());
        assertEquals(arrayOfUsers.length, list.size());
    }

    @Test
    public void givenUsers_createStreamWithStreamIterate() {
        val list = Stream.iterate(0, n -> n + 1)
                .limit(10)
                .peek(n -> log.debug("the number is : {}", n))
                .collect(toList());
        assertEquals(10, list.size());
    }

    @Test
    public void givenUsers_createStreamWithStreamGenerate() {
        val list = Stream.generate(() -> Math.random())
                .limit(10)
                .peek(n -> log.debug("the number is : {}", n))
                .collect(toList());
        assertEquals(10, list.size());
    }

    @Test
    public void givenUsers_createStreamWithStreamSplitIterator() {
        val itr = userList.iterator();
        Spliterator<User> spliterator = Spliterators.spliteratorUnknownSize(itr, Spliterator.NONNULL);
        Stream<User> userStream = StreamSupport.stream(spliterator, false);
        val list = userStream
                .peek(user -> log.debug("user: {}", user.getUsername()))
                .collect(toList());
        assertEquals(3, list.size());
    }

    @Test
    public void givenIntegerRange_createStreamWithIntStream() {
        val list = IntStream.range(0,5)
                .boxed()
                .peek(i -> log.debug("the number is {}", i))
                .collect(toList());
        assertEquals(5, list.size());
    }

    @Test
    public void givenUsers_createStreamWithStreamBuilder() {
        Stream.Builder<User> userStreamBuilder = Stream.builder();
        val list = userStreamBuilder
                .add(arrayOfUsers[0])
                .add(arrayOfUsers[1])
                .add(arrayOfUsers[2])
                .build()
                .skip(1) // 跳过 n 个元素
                .peek(user -> log.debug("user: {}", user.getUsername()))
                .collect(toList());
        assertEquals(arrayOfUsers.length - 1 , list.size());
    }

    @Test
    public void givenSentence_createStreamWithNewAPIs() {
        String sentence = "Hello Java Coders";
        Stream<String> wordStream = Pattern.compile("\\W").splitAsStream(sentence);
        val list = wordStream.peek(word -> log.debug("word: {}", word)).collect(toList());
        assertEquals(3, list.size());
    }
}
