package com.imooc.stream;

import com.imooc.stream.domain.User;
import com.imooc.stream.domain.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Predicate;

import static java.util.stream.Collectors.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class BasicOperatorTests {

    private static final User[] arrayOfUsers = {
        User.builder().id(1L).username("zhangsan").name("张三").enabled(true).mobile("13000000001").build(),
        User.builder().id(2L).username("lisi").name("李四").enabled(false).mobile("13000000002").build(),
        User.builder().id(3L).username("wangwu").name("王五").enabled(true).mobile("13100000000").build(),
    };

    private List<User> userList;

    @BeforeEach
    void setup() {
        userList = Arrays.asList(arrayOfUsers);
    }

    @Test
    public void givenUsers_whenForEach_thenChangeGender() {
        for (User user: arrayOfUsers) {
            user.setEnabled(true);
        }
        assertTrue(userList.get(1).isEnabled());
    }

    @Test
    public void givenUsers_whenForEach_thenChangeGenderUsingStream() {
        userList.stream().forEach(user -> user.setEnabled(true));
        assertTrue(userList.get(1).isEnabled());
    }

    @Test
    public void givenUsers_whenForEachOrdered_thenPrintInfo() {
        List<User> toSort = new ArrayList<>();
        for (User user : userList) {
            toSort.add(user);
        }
        toSort.sort(Comparator.comparing(User::getUsername));
        for (User user : toSort) {
            log.debug("用户信息：{}", user);
        }
    }

    @Test
    public void givenUsers_whenForEachOrdered_thenPrintInfoUsingStream() {
        userList.stream()
                .sorted(Comparator.comparing(User::getUsername))
                .forEachOrdered(user -> log.debug("用户信息：{}", user));
    }

    @Test
    public void givenUsers_whenMatchUsername_thenFindFirst() {
        User first = null;
        for (User user: arrayOfUsers) {
            if (user.getUsername().equals("lisi")) {
                first = user;
                break;
            }
        }
        assertNotNull(first);
        assertEquals("lisi", first.getUsername());
    }

    @Test
    public void givenUsers_whenMatchUsername_thenFindFirstUsingStream() {
        val first = userList.stream()
                .filter(user -> user.getUsername().equals("lisi"))
                .findFirst();
        assertTrue(first.isPresent());
        assertEquals("lisi", first.get().getUsername());
    }

    @Test
    public void givenUsers_whenMatchUsername_thenFindAnyUsingStream() {
        // findAny 在并行任务中，不确保返回的是哪个，除了这点，它和 findFirst 没有区别
        val first = userList.stream().findAny();
        assertTrue(first.isPresent());
        assertEquals("zhangsan", first.get().getUsername());
    }

    @Test
    public void givenUsers_withAnyMatch_thenReturnTrue() {
        boolean existed = false;
        for (User user : userList) {
            if (user.getMobile().startsWith("130")) {
                existed = true;
                break;
            }
        }
        assertTrue(existed);
        boolean nonExisted = false;
        for (User user : userList) {
            if (user.getMobile().startsWith("132")) {
                nonExisted = true;
                break;
            }
        }
        assertFalse(nonExisted);
    }

    @Test
    public void givenUsers_withAnyMatch_thenReturnTrueUsingStream() {
        val existed = userList.stream()
                .anyMatch(user -> user.getMobile().startsWith("130"));
        assertTrue(existed);
    }

    @Test
    public void givenUsers_withNoneMatch_thenReturnTrue() {
        boolean notMatched = true;
        for (User user : userList) {
            if (user.getMobile().startsWith("130")) {
                notMatched = false;
                break;
            }
        }
        assertFalse(notMatched);
        boolean matched = true;
        for (User user : userList) {
            if (user.getMobile().startsWith("132")) {
                matched = false;
                break;
            }
        }
        assertTrue(matched);
    }

    @Test
    public void givenUsers_withNoneMatch_thenReturnTrueUsingStream() {
        boolean notMatched = userList.stream()
                .noneMatch(user -> user.getMobile().startsWith("130"));
        assertFalse(notMatched);
        boolean matched = userList.stream()
                .noneMatch(user -> user.getMobile().startsWith("132"));
        assertTrue(matched);
    }

    @Test
    public void givenUsers_withAllMatch_thenReturnTrueUsingStream() {
        boolean allMatched = userList.stream()
                .allMatch(user -> user.getMobile().startsWith("13"));
        assertTrue(allMatched);
        boolean notMatched = userList.stream()
                .allMatch(user -> user.getMobile().startsWith("130"));
        assertFalse(notMatched);
    }

    @Test
    public void givenUsers_withMap_thenTransformUsingStream() {
        List<String> userDTOS = userList.stream()
                .map(user -> UserDTO.builder()
                        .username(user.getUsername())
                        .name(user.getName())
                        .enabled(user.isEnabled() ? "激活" : "禁用")
                        .mobile(user.getMobile())
                        .build()
                )
                .map(UserDTO::getMobile)
                .collect(toList());
        assertEquals(3, userDTOS.size());
    }

    @Test
    public void givenUsers_whenFilterUsername_thenGetCount() {
        long count = 0L;
        for (User user : userList) {
            if (user.getMobile().startsWith("130")) {
                count++;
            }
        }
        assertEquals(2, count);
    }

    @Test
    public void givenUsers_whenFilterUsername_thenGetCountUsingStream() {
        val count = userList.stream()
                .filter(user -> user.getMobile().startsWith("130"))
                .count();
        assertEquals(2, count);
        Predicate<User> userMobileStartWith130 = (user) -> user.getMobile().startsWith("130");
        val countWithPredicate = userList.stream()
                .filter(userMobileStartWith130)
                .count();
        assertEquals(2, countWithPredicate);
    }

    @Test
    public void givenUsers_thenTestOtherTerminalOperatorsUsingStream() {
        Optional<User> userByMaxUserId = userList.stream().max(Comparator.comparing(User::getId));
        assertTrue(userByMaxUserId.isPresent());
        assertEquals(3L, userByMaxUserId.get().getId());
        Optional<User> userByMinUsername = userList.stream().min(Comparator.comparing(User::getUsername));
        assertTrue(userByMinUsername.isPresent());
        assertEquals("wangwu", userByMaxUserId.get().getUsername());
    }
}
