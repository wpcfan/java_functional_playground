package com.imooc.stream;

import com.imooc.stream.domain.User;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.text.Collator;
import java.util.*;

import static java.util.stream.Collectors.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@ExtendWith(SpringExtension.class)
public class SortedTests {

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
    public void givenTwoNumber_withoutStream_thenSumSuccess() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
        int sum = 0;
        for (Integer number : numbers) {
            sum += number;
        }
        assertEquals(45, sum);
    }

    @Test
    public void givenTwoNumber_withReduce_thenSumSuccess() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
        int sum = numbers.stream().reduce(0, (a, b) -> a + b);
        assertEquals(45, sum);
    }

    @Test
    public void givenCollections_withoutStream_thenSort() {
        List<String> list = Arrays.asList("One", "Abc", "BCD");
        log.debug("未排序: {}", list);
        assertEquals("One", list.get(0));
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return a.compareTo(b);
            }
        });
        log.debug("排序后: {}", list);
        assertEquals("Abc", list.get(0));
    }

    @Test
    public void givenCollections_withStream_thenSort() {
        List<String> list = Arrays.asList("One", "Abc", "BCD");
        val sortedListDefaultSortOperator = list.stream()
                .sorted()
                .collect(toList());
        assertEquals("Abc", sortedListDefaultSortOperator.get(0));
        val sortedListWithFunction = list.stream()
                .sorted((a, b) -> a.compareTo(b))
                .collect(toList());
        assertEquals("Abc", sortedListWithFunction.get(0));
        val sortedListWithMethodReference = list.stream()
                .sorted(String::compareTo)
                .collect(toList());
        assertEquals("Abc", sortedListWithMethodReference.get(0));
        val descSortedListWithComparator = list.stream()
                .sorted(Comparator.reverseOrder())
                .collect(toList());
        assertEquals("One", descSortedListWithComparator.get(0));
        val descSortedListWithComparatorComparing = userList.stream()
                .sorted(Comparator.comparing(
                        user -> user.getUsername(),
                        (a, b) -> a.compareTo(b))
                )
                .collect(toList());
        assertEquals("lisi", descSortedListWithComparatorComparing.get(0).getUsername());
        Collator colZhCN = Collator.getInstance(Locale.SIMPLIFIED_CHINESE);
        val chineseList = userList.stream()
                .sorted(Comparator.comparing(User::getName, colZhCN))
                .collect(toList());
        assertEquals("李四", chineseList.get(0).getName());
    }
}
