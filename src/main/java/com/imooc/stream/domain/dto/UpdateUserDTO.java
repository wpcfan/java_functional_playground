package com.imooc.stream.domain.dto;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class UpdateUserDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String mobile;
    private final String name;
    private final String email;
}
