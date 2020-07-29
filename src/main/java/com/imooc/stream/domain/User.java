package com.imooc.stream.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mooc_users")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 自增长 ID，唯一标识
     */
    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名
     */
    @Getter
    @Setter
    @Column(length = 50, unique = true, nullable = false)
    private String username;

    /**
     * 手机号
     */
    @Getter
    @Setter
    @Column(length = 11, unique = true, nullable = false)
    private String mobile;

    /**
     * 姓名
     */
    @Getter
    @Setter
    @Column(length = 50)
    private String name;

    /**
     * 是否激活，默认激活
     */
    @Getter
    @Setter
    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * 密码哈希
     */
    @JsonIgnore
    @Getter
    @Setter
    @Column(name = "password_hash", length = 80, nullable = false)
    private String password;

    /**
     * 电邮地址
     */
    @Getter
    @Setter
    @Column(unique = true, nullable = false)
    private String email;

    @Getter
    @Setter
    private int age;

    @Getter
    public List<String> roles;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        return id != null && id.equals(((User) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", mobile='" + mobile + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                "}";
    }
}