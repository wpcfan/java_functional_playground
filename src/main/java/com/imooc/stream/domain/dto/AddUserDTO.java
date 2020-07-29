package com.imooc.stream.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class AddUserDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String username;
    private final String mobile;
    private final String name;
    private final String email;
}
