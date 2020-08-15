package com.imooc.stream;

import com.imooc.stream.domain.User;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class CollectorsTests {

    private static final User[] arrayOfUsers = {
            User.builder().id(1L).username("zhangsan").name("张三").age(30).enabled(true).mobile("13000000001").build(),
            User.builder().id(2L).username("lisi").name("张三").age(32).enabled(false).mobile("13000000002").build(),
            User.builder().id(3L).username("wangwu").name("王五").age(41).enabled(true).mobile("13000000003").build(),
    };

    private List<User> userList;

    @BeforeEach
    void setup() {
        userList = Arrays.asList(arrayOfUsers);
    }

    @Test
    public void givenUsers_withToSet_thenSuccess() {
       Set<String> usernames = userList.stream()
               .map(User::getName)
               .collect(toSet());
       assertEquals(2, usernames.size());
    }

    @Test
    public void givenUsers_withToMap_thenSuccess() {
        Map<String, User> userMap = userList.stream()
                .collect(toMap(
                        User::getUsername,
                        user -> user
                ));
        assertTrue(userMap.containsKey("lisi"));
        Map<String, User> duplicateMap = Stream.concat(userList.stream(), userList.stream())
                .peek(user -> log.debug("username, {}", user.getUsername()))
                .collect(toMap(
                        User::getUsername,
                        user -> user,
                        (existing, replace) -> existing
                ));
        assertEquals(3, duplicateMap.keySet().size());
        TreeMap<String, User> sortedMap = Stream.concat(userList.stream(), userList.stream())
                .peek(user -> log.debug("username, {}", user.getUsername()))
                .collect(toMap(
                        User::getUsername,
                        user -> user,
                        (existing, replace) -> existing,
                        TreeMap::new
                ));
        assertEquals("lisi", sortedMap.keySet().stream().findFirst().get());
    }

    @Test
    public void givenUsers_withToCollection_thenSuccess() {
        Comparator<User> byAge = Comparator.comparing(User::getAge);
        TreeSet<User> users = userList.stream()
                .collect(toCollection(() -> new TreeSet<>(byAge)));
        assertEquals(30, users.stream().map(User::getAge).findFirst().orElse(-1));
    }

    @Test
    public void givenUsers_withSimpleSolarFunction_thenGetResult() {
        double avg = userList.stream().collect(averagingDouble(User::getAge));
        assertEquals((30 + 32 + 41) /3.0, avg);
        int sum = userList.stream().collect(summingInt(User::getAge));
        assertEquals((30 + 32 + 41), sum);
        DoubleSummaryStatistics stat = userList.stream().collect(summarizingDouble(User::getAge));
        assertEquals((30 + 32 + 41.0), stat.getSum());
        assertEquals((30 + 32 + 41) /3.0, stat.getAverage());
    }

    @Test
    public void givenUsers_whenGroupingByAge_thenGetStatWithStream() {
        Map<Integer, DoubleSummaryStatistics> map = userList.stream().collect(
                groupingBy(
                        user -> (int) Math.floor(user.getAge() / 10.0) * 10,
                        summarizingDouble(User::getAge)
                )
        );
        log.debug("result: {}", map);
        assertEquals(2, map.get(30).getCount());
        assertEquals(32, map.get(30).getMax());
        assertEquals(30, map.get(30).getMin());
        assertEquals(31, map.get(30).getAverage());
        assertEquals(62, map.get(30).getSum());    }

    @Test
    public void givenUsers_whenGroupingByAge_thenGetListWithStream() {
        Map<Integer, List<UserDto>> result = userList.stream().collect(
                groupingBy(
                        user -> (int) Math.floor(user.getAge() / 10.0) * 10,
                        mapping(
                                user -> new UserDto(
                                    user.getId(),
                                    user.getUsername() + ":" + user.getName()
                                ),
                                toList()
                        )
                )
        );
        log.debug("result: {}", result);
    }

    @Test
    public void givenStrings_thenMappingAndFiltering_theChainThemTogether() {
        List<String> strings = List.of("bb", "ddd", "cc", "a");
        Map<Integer, TreeSet<String>> result = strings.stream()
                .collect(groupingBy(
                        String::length,
                        mapping(
                                String::toUpperCase,
                                filtering(
                                        s -> s.length() > 1,
                                        toCollection(TreeSet::new)
                                )
                        )
                ));
        log.debug("result: {}", result);
    }

    @Test
    public void givenUsers_whenGroupingByAgeAndCollectingAndThen_thenGetCustomWithStream() {
        // collectingAndThen 其实就是在最后在做一个单一操作
        Map<Integer, UserStat> map = userList.stream().collect(
                groupingBy(
                        user -> (int)Math.floor(user.getAge() / 10.0) * 10,
                        collectingAndThen(
                                toList(),
                                list -> {
                                    double average = list.stream().collect(averagingDouble(User::getAge));
                                    return new UserStat(average, list);
                                })
                )
        );
        log.debug("map {}", map);
        assertEquals(2, map.get(30).getUsers().size());
        assertEquals(31.0, map.get(30).getAverage());
    }

    @Test
    public void givenUsers_withJoining_thenGetString() {
        Map<String, String>  requestParams = Map.of(
                "name", "张三",
                "username", "zhangsan",
                "email", "zhangsan@local.dev"
        );
        val url = requestParams.keySet().stream()
                .map(key -> key + "=" + requestParams.get(key))
                .sorted()
                .collect(joining(
                        "&",
                        "http://local.dev/api?",
                        ""
                ));
        assertEquals("http://local.dev/api?email=zhangsan@local.dev&name=张三&username=zhangsan", url);
    }

    @Test
    public void customCollector_whenUsingCollectorOf_then() {
        val map = userList.stream().collect(Collector.of(
                buildContainer(),
                addElementToContainer(),
                buildReplaceStrategyWhenDuplicate()
        ));
        assertNotNull(map.keySet().stream().findFirst());
        assertEquals("lisi", map.keySet().stream().findFirst().get());
    }

    private Supplier<TreeMap<String, User>> buildContainer() {
        return (Supplier<TreeMap<String, User>>) TreeMap::new;
    }

    private BiConsumer<TreeMap<String, User>, User> addElementToContainer() {
        return (container, user) -> container.put(user.getUsername(), user);
    }

    private BinaryOperator<TreeMap<String, User>> buildReplaceStrategyWhenDuplicate() {
        return (result1, result2) -> {
            result1.putAll(result2);
            return result1;
        };
    }

    @AllArgsConstructor
    @Getter
    static class UserStat {
        private final double average;
        private final List<User> users;
    }

    @AllArgsConstructor
    @Getter
    static class UserDto {
        private final Long id;
        private final String nickName;
    }
}
