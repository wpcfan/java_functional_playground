package com.imooc.stream;

import com.imooc.stream.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class OptionalStreamTests {

    private static final User[] arrayOfUsers = {
            User.builder().id(1L).username("zhangsan").name("张三").enabled(true).mobile("13000000001").build(),
            User.builder().id(2L).username("lisi").name("李四").enabled(false).mobile("13000000002").build(),
            User.builder().id(3L).username("wangwu").name("王五").enabled(true).mobile("13100000000").build(),
    };

    private MockRepo repo;

    @BeforeEach
    void setup() {
        repo = new MockRepo();
    }

    static class MockRepo {
        Optional<User> findByUsername(String username) {
            return Arrays.stream(arrayOfUsers)
                    .filter(user -> user.getUsername().equals(username))
                    .findAny();
        }
    }

    @Test
    public void givenUsers_whenQueryOptional_thenCheckPresent() {
        Optional<User> userOptional = repo.findByUsername("zhangsan");
        assertTrue(userOptional.isPresent());
        Optional<User> zhaoliu = repo.findByUsername("zhaoliu");
        assertTrue(zhaoliu.isEmpty());
    }

    @Test
    public void givenUsers_whenQueryEmpty_thenOrElseThrow() {
        Executable executable = () -> repo.findByUsername("zhaoliu")
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));
        assertThrows(HttpClientErrorException.class, executable);
    }

    @Test
    public void givenUsers_whenQueryEmpty_thenOrElse() {
       String username = repo.findByUsername("zhaoliu")
               .map(User::getUsername)
               .orElse("anonymous");
       assertNotNull(username);
       assertEquals("anonymous", username);
    }

    @Test
    public void givenUsers_whenQueryEmpty_thenOrElseGet() {
        String username = repo.findByUsername("zhaoliu")
                .map(User::getUsername)
                .orElseGet(() -> "anonymous");
        assertNotNull(username);
        assertEquals("anonymous", username);
    }

    @Test
    public void givenUsers_whenQueryEmpty_thenOr() {
        Optional<String> usernameOptional = repo.findByUsername("zhaoliu")
                .map(User::getUsername)
                .or(() -> Optional.of("notExisted"));
        assertTrue(usernameOptional.isPresent());
        assertEquals("notExisted", usernameOptional.get());
    }

    @Test
    public void givenUsers_whenQuerying_thenIfPresent() {
        repo.findByUsername("zhangsan")
                .map(User::getUsername)
                .ifPresent(username -> {
                    log.debug("username: {}", username);
                    assertEquals("zhangsan", username);
                });
    }

    @Test
    public void givenUsers_whenQuerying_thenIfPresentOrElse() {
        repo.findByUsername("zhangsan")
                .map(User::getUsername)
                .ifPresentOrElse(
                        username -> {
                            log.debug("username: {}", username);
                            assertEquals("zhangsan", username);
                        },
                        () -> {
                            log.debug("cannot reach else block");
                        });
        repo.findByUsername("zhaoliu")
                .map(User::getUsername)
                .ifPresentOrElse(
                        username -> {
                            log.debug("cannot reach this block");
                        },
                        () -> {
                            assertTrue(true);
                        });
    }
}
