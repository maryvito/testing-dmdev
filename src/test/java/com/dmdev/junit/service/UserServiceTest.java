package com.dmdev.junit.service;

import com.dmdev.junit.dto.User;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceTest {

    UserService userService;

    @BeforeAll
    void before(){
        System.out.println("Before all: " + this);
    }

    @BeforeEach
    void prepare(){
        System.out.println("before each: " + this);
        userService = new UserService();
    }

    @Test
    void usersEmptyIfNoUserAdded(){
        System.out.println("Test 1: " + this);

        var users = userService.getAll();
        assertTrue(users.isEmpty(), () -> "User list should be empty");
    }

    @Test
    void usersSizeIfUserAdded(){
        System.out.println("Test 2: " + this);
        userService.add(new User());
        userService.add(new User());

        var users = userService.getAll();
        assertEquals(2, users.size());
    }

    @AfterEach
    void deleteDataFromDatabase(){
        System.out.println("After each: " + this);
    }

    @AfterAll
    void closeConnectionPool(){
        System.out.println("Before all: " + this);
    }
}
