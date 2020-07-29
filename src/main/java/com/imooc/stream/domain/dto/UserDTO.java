package com.imooc.stream.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String mobile;
    private String name;
    private String enabled;
}
