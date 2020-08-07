package com.imooc.stream;

import com.imooc.stream.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// chap01
@Slf4j
public class FunctionIntroTests {

    @AllArgsConstructor
    @Getter
    static class Book {
        private final String title;
        private final String author;
        private final Integer pages;
    }

    @Test
    public void givenListOfBooks_thenExplainTheAdvantageOfFunction() {
        String authorA = "张三";
        String authorB = "李四";
        String authorC = "王五";
        String authorD = "前朝太监";
        List<Book> books = Arrays.asList(
                new Book("C++编程", authorA, 1216),
                new Book("C#编程", authorA, 365),
                new Book("Java编程", authorB, 223),
                new Book("Ruby编程", authorB, 554),
                new Book("21天精通Python", authorB, 607),
                new Book("21天精通Go", authorC, 352),
                new Book("葵花宝典", authorD, 1200),
                new Book("编程圣经", authorA, 320)
        );
        List<Book> booksFiltered = new ArrayList<>();
        for (Book book : books){
            if (! "葵花宝典".equals(book.getTitle())) {
                booksFiltered.add(book);
            }
        }
        booksFiltered.sort(new Comparator<Book>() {
            @Override
            public int compare(Book o1, Book o2) {
                return o2.getPages().compareTo(o1.getPages());
            }
        });
        for (int i=0; i<3; i++) {
            System.out.println(booksFiltered.get(i).getAuthor());
        }
        books.stream()
                .filter(b -> ! "葵花宝典".equals(b.getTitle()))
                .sorted((b1, b2) -> b2.getPages().compareTo(b1.getPages()))
                .limit(3)
                .map(Book::getAuthor)
                .distinct()
                .forEach(System.out::println);
    }

    @Test
    public void givenTwoFunctions_thenComposeThemInNewFunction() {
        Predicate<String> startsWithA = (text) -> text.startsWith("A");
        Predicate<String> endsWithX   = (text) -> text.endsWith("x");
        Predicate<String> startsWithAAndEndsWithX = (text) -> startsWithA.test(text) && endsWithX.test(text);
        String  input  = "A man is standing there with a box";
        assertTrue(startsWithAAndEndsWithX.test(input));
    }

    @Test
    public void givenTwoPredicates_thenComposeThemUsingAnd() {
        Predicate<String> startsWithA = (text) -> text.startsWith("A");
        Predicate<String> endsWithX   = (text) -> text.endsWith("x");
        Predicate<String> startsWithAAndEndsWithX = startsWithA.and(endsWithX);
        String  input  = "A man is standing there with a box";
        assertTrue(startsWithAAndEndsWithX.test(input));
    }

    @Test
    public void givenTwoPredicates_thenComposeThemUsingOr() {
        Predicate<String> startsWithA = (text) -> text.startsWith("A");
        Predicate<String> endsWithX   = (text) -> text.endsWith("x");
        Predicate<String> startsWithOrEndsWithX = startsWithA.or(endsWithX);
        String  input  = "A man";
        assertTrue(startsWithOrEndsWithX.test(input));
    }

    @Test
    public void givenTwoPredicates_thenComposeThemUsingCompose() {
        Function<Integer, Integer> squareOp = (value) -> value * value;
        Function<Integer, Integer> doubleOp = (value) -> 2 * value;
        // 先执行 doubleOp 再执行 squareOp
        Function<Integer, Integer> doubleThenSquare = squareOp.compose(doubleOp);
        assertEquals(36, doubleThenSquare.apply(3));
    }

    @Test
    public void givenTwoPredicates_thenComposeThemUsingAndThen() {
        Function<Integer, Integer> squareOp = (value) -> value * value;
        Function<Integer, Integer> doubleOp = (value) -> 2 * value;
        // 先执行 squareOp 再执行 doubleOp
        Function<Integer, Integer> doubleThenSquare = squareOp.andThen(doubleOp);
        assertEquals(18, doubleThenSquare.apply(3));
    }

    @Test
    public void givenFunctionalInterface_thenFunctionAsArgumentsOrReturnValue() {
        HigherOrderFunctionClass higherOrderFunctionClass = new HigherOrderFunctionClass();
        IFactory<User> factory = higherOrderFunctionClass.createFactory(
                () -> User.builder().id(100L).name("imooc").build(),
                (user) -> {
                    log.debug("用户信息: {}", user);
                    user.setMobile("13012345678");
                }
        );
        User user = factory.create();
        assertEquals("imooc", user.getName());
        assertEquals(100L, user.getId());
        assertEquals("13012345678", user.getMobile());
    }

    interface IFactory<T> {
        T create();
    }

    interface IProducer<T> {
        T produce();
    }

    interface IConfigurator<T> {
        void configure(T t);
    }

    class TestProducer implements IProducer {

        @Override
        public Object produce() {
            return null;
        }
    }

    static class HigherOrderFunctionClass {
        public <T> IFactory<T> createFactory(IProducer<T> producer, IConfigurator<T> configurator) {
            return () -> {
                T instance = producer.produce();
                configurator.configure(instance);
                return instance;
            };
        }
    }
}
