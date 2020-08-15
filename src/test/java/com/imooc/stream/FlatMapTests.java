package com.imooc.stream;

import com.imooc.stream.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class FlatMapTests {
    private static final User[] arrayOfUsers = {
            User.builder()
                    .id(1L)
                    .username("zhangsan")
                    .name("张三")
                    .age(30)
                    .enabled(true)
                    .mobile("13000000001")
                    .roles(List.of("ROLE_ADMIN", "ROLE_USER"))
                    .build(),
            User.builder()
                    .id(2L)
                    .username("lisi")
                    .name("李四")
                    .age(32)
                    .enabled(false)
                    .mobile("13000000002")
                    .roles(List.of("ROLE_ADMIN"))
                    .build(),
            User.builder()
                    .id(3L)
                    .username("wangwu")
                    .name("王五")
                    .age(41)
                    .enabled(true)
                    .mobile("13000000003")
                    .roles(List.of("ROLE_USER"))
                    .build(),
    };

    private List<User> userList;

    static class ThirdPartyApi {
        static Optional<Profile> findByUsername(String username) {
            return Arrays.stream(arrayOfUsers)
                    .filter(user -> !"zhangsan".equals(username) && user.getUsername().equals(username))
                    .findAny()
                    .map(user -> new Profile(user.getUsername(), "Hello, " + user.getName()));
        }
    }

    @AllArgsConstructor
    @Data
    static class Profile {
        private String username;
        private String greeting;
    }

    @BeforeEach
    void setup() {
        userList = Arrays.asList(arrayOfUsers);
    }

    @Test
    public void givenUsersWithRoles_whenParentChild_withoutFlatMap() {
        val users = userList.stream()
                .map(User::getRoles)
                .peek(roles -> log.debug("roles {}", roles))
                .collect(toList());
        log.debug("users: {}", users);
    }

    @Test
    public void givenUsersWithRoles_withFlatMap() {
        val users = userList.stream()
                .flatMap(user -> user.getRoles().stream())
                .peek(role -> log.debug("roles {}", role))
                .collect(toList());
        log.debug("users: {}", users);
    }

    @Test
    public void givenUsers_withOptional_thenWithStream() {
        val profiles = userList.stream()
                .map(user -> ThirdPartyApi.findByUsername(user.getUsername()))
                .peek(profile -> log.debug("profile: {}", profile))
                .collect(toList());
        log.debug("profiles: {}", profiles);
    }

    @Test
    public void givenUsers_withOptional_thenFlatMapWithStream() {
        val profiles = userList.stream()
                .map(user -> ThirdPartyApi.findByUsername(user.getUsername()))
                .flatMap(Optional::stream)
                .peek(profile -> log.debug("profile: {}", profile))
                .collect(toList());
        log.debug("profiles: {}", profiles);
    }

    @Test
    public void givenUsers_withOptional_thenDealElseWithStream() {
        String greeting = ThirdPartyApi.findByUsername("zhangsan")
                .map(Profile::getGreeting)
                .orElse("未知用户");
        assertEquals("未知用户", greeting);
    }

    @Test
    public void givenUsersWithRoles_whenFlatMap_thenGroupByRole() {
        Map<String, List<User>> usersByRole = new HashMap<>();
        for (User user : userList) {
            for (String role : user.getRoles()) {
                Pair<String, User> pair = Pair.of(role, user);
                User second = pair.getSecond();
                usersByRole.computeIfAbsent(
                        pair.getFirst(),
                        new Function<String, List<User>>() {
                            @Override
                            public List<User> apply(String k) {
                                return new ArrayList<>();
                            }
                        }
                ).add(second);
            }
        }
        assertEquals(2, usersByRole.size());
        assertEquals(2, usersByRole.get("ROLE_ADMIN").size());
    }

    @Test
    public void givenUsersWithRoles_whenFlatMap_thenGroupByRoleWithStream() {
        val usersByRole = userList.stream()
                .flatMap(user -> user.getRoles().stream().map(role -> Pair.of(role, user)))
                .collect(groupingBy(
                        Pair::getFirst,
                        mapping(Pair::getSecond, toList())
                ));
        log.debug("usersByRole {}", usersByRole);
        assertEquals(2, usersByRole.size());
        assertEquals(2, usersByRole.get("ROLE_ADMIN").size());
    }
}
