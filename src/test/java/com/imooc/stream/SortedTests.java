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
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        val sortedList = list.stream().sorted().collect(toList());
        assertEquals("Abc", sortedList.get(0));
        val sortedListByFunc = list.stream().sorted((a, b) -> {
            return a.compareTo(b);
        }).collect(toList());
        assertEquals("Abc", sortedListByFunc.get(0));
        val sortedListByMethodReference = list.stream().sorted(String::compareTo).collect(toList());
        assertEquals("Abc", sortedListByMethodReference.get(0));
        val sortedListByComparatorFunc = list.stream().sorted(Comparator.naturalOrder()).collect(toList());
        assertEquals("Abc", sortedListByComparatorFunc.get(0));
        val descSortedList = list.stream().sorted(Comparator.reverseOrder()).collect(toList());
        assertEquals("One", descSortedList.get(0));
        val descSortedListByFunc = list.stream().sorted((a, b) -> {
            return b.compareTo(a);
        }).collect(toList());
        assertEquals("One", descSortedListByFunc.get(0));
        val sortedUsers = userList.stream()
                .sorted((a, b) -> a.getUsername().compareTo(b.getUsername()))
                .collect(toList());
        assertEquals("lisi", sortedUsers.get(0).getUsername());
        val sortedUsersByComparator = userList.stream()
                .sorted(Comparator.comparing(
                        user -> user.getUsername(),
                        (a, b) -> a.compareTo(b)
                ))
                .collect(toList());
        assertEquals("lisi", sortedUsersByComparator.get(0).getUsername());
        Collator sortedByZhCN = Collator.getInstance(Locale.SIMPLIFIED_CHINESE);
        val sortedUsersByChinese = userList.stream()
                .sorted(Comparator.comparing(
                        User::getName,
                        sortedByZhCN
                ))
                .collect(toList());
        assertEquals("李四", sortedUsersByChinese.get(0).getName());
    }
}
