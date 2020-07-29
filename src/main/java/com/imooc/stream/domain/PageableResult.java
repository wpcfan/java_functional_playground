package com.imooc.stream.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Getter
public class PageableResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final long total;
    private final List<T> data;
}
