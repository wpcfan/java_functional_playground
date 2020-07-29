package com.imooc.stream.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Try<Exception, R> {
    private final Exception failure;
    private final R success;
}
