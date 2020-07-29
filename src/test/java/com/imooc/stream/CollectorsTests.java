package com.imooc.stream;

import com.imooc.stream.domain.User;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class CollectorsTests {

    private static final User[] arrayOfUsers = {
            User.builder().id(1L).username("zhangsan").name("张三").age(30).enabled(true).mobile("13000000001").build(),
            User.builder().id(2L).username("lisi").name("李四").age(32).enabled(false).mobile("13000000002").build(),
            User.builder().id(3L).username("wangwu").name("王五").age(41).enabled(true).mobile("13000000003").build(),
    };

    private List<User> userList;

    @BeforeEach
    void setup() {
        userList = Arrays.asList(arrayOfUsers);
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
                .collect(Collectors.joining("&", "http://local.dev/api" + "?", ""));
        assertEquals("http://local.dev/api?email=zhangsan@local.dev&name=张三&username=zhangsan", url);
    }

    @Test
    public void givenUsers_withToMap_thenSuccess() {
        Map<String, User> map = userList.stream().collect(toMap(
                (user) -> user.getUsername(),
                user -> user
        ));
        assertEquals(3, map.size());
        Map<String, User> duplicateMap = Stream.concat(userList.stream(), userList.stream())
                .collect(toMap(
                        User::getUsername,
                        user -> user,
                        (existing, replacement) -> existing
                ));
        assertEquals(3, duplicateMap.size());
        TreeMap<String, User> sortedMap = userList.stream().collect(toMap(
                User::getUsername,
                user -> user,
                (existing, replacement) -> existing,
                TreeMap::new
        ));
        assertEquals(3, map.size());
        assertEquals("lisi", sortedMap.keySet().stream().findFirst().get());
    }

    @Test
    public void givenUsers_withToSet_thenSuccess() {
        Set<User> set = userList.stream().collect(toSet());
        assertEquals(3, set.size());
    }

    @Test
    public void givenUsers_withToCollection_thenSuccess() {
        Comparator<User> byAge = Comparator.comparingInt(User::getAge);
//        Supplier<TreeSet<User>> userSupplier = () -> new TreeSet<>(byAge);
        TreeSet<User> setByAge = userList.stream().collect(toCollection(() -> new TreeSet<>(byAge)));
        assertEquals(30, setByAge.stream().findFirst().map(User::getAge).orElse(-1));
    }

    @Test
    public void givenUsers_whenGroupingByAge_thenGetStatWithStream() {
        Map<Integer, DoubleSummaryStatistics> map = userList.stream().collect(
                groupingBy(
                        user -> (int)Math.floor(user.getAge() / 10.0) * 10,
                        summarizingDouble(User::getAge)
                )
        );
        log.debug("map {}", map);
        assertEquals(2, map.get(30).getCount());
        assertEquals(32, map.get(30).getMax());
        assertEquals(30, map.get(30).getMin());
        assertEquals(31, map.get(30).getAverage());
        assertEquals(62, map.get(30).getSum());
    }

    @Test
    public void givenUsers_whenGroupingByAge_thenGetListWithStream() {
        Map<Integer, List<User>> map = userList.stream()
                .collect(
                    groupingBy(
                            user -> (int)Math.floor(user.getAge() / 10.0) * 10,
                            mapping(user -> user, toList())
                    )
                );
        log.debug("map {}", map);
        assertEquals(2, map.get(30).size());
    }

    @Test
    public void givenUsers_whenGroupingByAgeAndCollectingAndThen_thenGetCustomWithStream() {

        Map<Integer, List<User>> originalMap = userList.stream().collect(
                groupingBy(
                        user -> (int)Math.floor(user.getAge() / 10.0) * 10,
                        toList()
                )
        );
        List<UserStat> listUserStat = originalMap.keySet().stream()
                .map(key -> {
                    double average = originalMap.get(key).stream().collect(averagingDouble(User::getAge));
                    return new UserStat(average, originalMap.get(key));
                })
                .collect(toList());

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
    public void givenStrings_thenGroupByChains() {
        List<String> strings = List.of("a", "bb", "cc", "ddd");
        Map<Integer, TreeSet<String>> result = strings.stream()
                .collect(
                        groupingBy(String::length,
                                mapping(String::toUpperCase,
                                        filtering(s -> s.length() > 1,
                                                toCollection(TreeSet::new)))));

    }

    @Test
    public void givenAppData_withTemporalAdjuster_thenGroupByDate() {
        final Map<String, TemporalAdjuster> ADJUSTERS = Map.of(
                "day", TemporalAdjusters.ofDateAdjuster(d -> d),
                "week", TemporalAdjusters.previousOrSame(DayOfWeek.of(1)),
                "month", TemporalAdjusters.firstDayOfMonth(),
                "year", TemporalAdjusters.firstDayOfYear()
        );
        val day0 = AppData.builder()
                .totalUsers(0L)
                .todayNewUsers(0L)
                .build();
        val date0 = LocalDate.parse("2020-02-29");
        day0.setCreatedAt(date0.atStartOfDay(ZoneId.systemDefault()).toInstant());
        val day1 = AppData.builder()
                .totalUsers(10L)
                .todayNewUsers(10L)
                .build();
        val date1 = LocalDate.parse("2020-03-01");
        day1.setCreatedAt(date1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        val day2 = AppData.builder()
                .totalUsers(20L)
                .todayNewUsers(12L)
                .build();
        val date2 = LocalDate.parse("2020-03-02");
        day2.setCreatedAt(date2.atStartOfDay(ZoneId.systemDefault()).toInstant());
        val day3 = AppData.builder()
                .totalUsers(30L)
                .todayNewUsers(16L)
                .build();
        val date3 = LocalDate.parse("2020-03-03");
        day3.setCreatedAt(date3.atStartOfDay(ZoneId.systemDefault()).toInstant());
        val list = Arrays.asList(day0, day1, day2, day3);
        final Map<LocalDate, LongSummaryStatistics> groupByDay = list.stream()
                .collect(
                        groupingBy(
                                appData -> appData.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate().with(ADJUSTERS.get("day")),
                                summarizingLong(AppData::getTodayNewUsers)
                        )
                );
        assertEquals(10L, groupByDay.get(date1).getSum());
        assertEquals(12L, groupByDay.get(date2).getSum());
        assertEquals(16L, groupByDay.get(date3).getSum());

        final Map<LocalDate, LongSummaryStatistics> groupByWeek = list.stream()
                .collect(
                        groupingBy(
                                appData -> appData.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate().with(ADJUSTERS.get("week")),
                                summarizingLong(AppData::getTodayNewUsers)
                        )
                );
        assertEquals(10L, groupByWeek.get(date1.with(ADJUSTERS.get("week"))).getSum());
        assertEquals(28L, groupByWeek.get(date2.with(ADJUSTERS.get("week"))).getSum());
    }

    @Test
    public void customCollector_whenUsingCollectorOf_then() {
        val map = userList.stream().collect(Collector.of(
                buildSupplier(),
                buildBiConsumer(),
                buildBinaryOperator()
        ));
        assertNotNull(map.keySet().stream().findFirst());
        assertEquals("lisi", map.keySet().stream().findFirst().get());
    }

    private Supplier<TreeMap<String, User>> buildSupplier() {
        return (Supplier<TreeMap<String, User>>) TreeMap::new;
    }

    private BiConsumer<TreeMap<String, User>, User> buildBiConsumer() {
        return (result, user) -> result.put(user.getUsername(), user);
    }

    private BinaryOperator<TreeMap<String, User>> buildBinaryOperator() {
        return (result1, result2) -> {
            result1.putAll(result2);
            return result1;
        };
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    static class AppData implements Serializable {
        private Long id;
        private Long totalUsers;
        private Long todayNewUsers;
        private Instant createdAt;
    }

    @AllArgsConstructor
    @Getter
    static class UserStat {
        private final double average;
        private final List<User> users;
    }
}
