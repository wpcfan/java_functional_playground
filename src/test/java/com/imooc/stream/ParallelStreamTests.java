package com.imooc.stream;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParallelStreamTests {

    @Test
    public void givenUsers_whenComparingWithParallel_thenParallelWinsUsingStream() {
        val countStart = Instant.now();
        long count = Stream.iterate(0, n -> n + 1)
                .limit(100_000)
                .filter(ParallelStreamTests::isPrime)
                .peek(x -> System.out.format("%s\t", x))
                .count();
        val timeForCount = Instant.now().toEpochMilli() - countStart.toEpochMilli();
        val countParallelStart = Instant.now();
        long countParallel = Stream.iterate(0, n -> n + 1)
                .limit(100_000)
                .parallel()
                .filter(ParallelStreamTests::isPrime)
                .peek(x -> System.out.format("%s\t", x))
                .count();
        val timeParallelForCount = Instant.now().toEpochMilli() - countParallelStart.toEpochMilli();
        assertTrue(timeParallelForCount < timeForCount);
        assertEquals(count, countParallel);
    }

    public static boolean isPrime(int number) {
        if (number <= 1) return false;
        return !IntStream.rangeClosed(2, number / 2)
                    .anyMatch(i -> number % i == 0);
    }
}
