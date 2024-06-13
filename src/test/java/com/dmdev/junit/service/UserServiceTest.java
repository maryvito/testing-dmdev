package com.dmdev.junit.service;

import com.dmdev.junit.dao.UserDao;
import com.dmdev.junit.dto.User;
import com.dmdev.junit.paramresolver.UserServiceParamResolver;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith({
        MockitoExtension.class
})
public class UserServiceTest {

    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETR = User.of(2, "Petr", "111");

    @Captor
    private ArgumentCaptor<Integer> argumentCaptor;
    @Mock
    private UserDao userDao;
    @InjectMocks
    private UserService userService;

    public UserServiceTest(TestInfo testInfo) {
        System.out.println();
    }


    @BeforeAll
    void before(){
        System.out.println("Before all: " + this);
    }

    @BeforeEach
    void prepare(){
        System.out.println("before each: " + this);
        Mockito.doReturn(true).when(userDao).delete(IVAN.getId());
        //this.userDao = Mockito.spy(new UserDao());
        //userService = new UserService(userDao);
    }

    @Test
    void throwExceptionIfDatabaseIsNotAvailable() {
         doThrow(RuntimeException.class).when(userDao).delete(IVAN.getId());

        assertThrows(RuntimeException.class, () -> userService.delete(IVAN.getId()));
    }

    @Test
    void shouldDeleteExistedUser(){
        userService.add(IVAN);

        //Mockito.doReturn(true).when(userDao).delete(Mockito.any());

        /*Mockito.when(userDao.delete(IVAN.getId()))
                .thenReturn(true)
                . thenReturn(false);*/

        var deleteResult = userService.delete(IVAN.getId());
        System.out.println(userService.delete(IVAN.getId()));
        System.out.println(userService.delete(IVAN.getId()));

        //var argumentCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(userDao, Mockito.times(3)).delete(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue()).isEqualTo(IVAN.getId());

        assertThat(deleteResult).isTrue();
    }

    @Test
    @Order(1)
    @DisplayName("users ...")
    void usersEmptyIfNoUserAdded(){
        System.out.println("Test 1: " + this);
        var users = userService.getAll();

        MatcherAssert.assertThat(users, IsEmptyCollection.empty());
        //assertTrue(users.isEmpty(), () -> "User list should be empty");
    }

    @Test
    void usersConvertedToMapById(){
        userService.add(IVAN, PETR);

        Map<Integer, User> users = userService.getAllConvertedById();

        MatcherAssert.assertThat(users, IsMapContaining.hasKey(IVAN.getId()));
        /*assertAll(
                () -> assertThat(users).containsKeys(IVAN.getId(), PETR.getId()),
                () -> assertThat(users).containsValues(IVAN, PETR)
        );*/
    }


    @Test
    void usersSizeIfUserAdded(){
        System.out.println("Test 2: " + this);
        userService.add(IVAN);
        userService.add(PETR);

        var users = userService.getAll();

        assertThat(users).hasSize(2);
        //assertEquals(2, users.size());
    }

    @AfterEach
    void deleteDataFromDatabase(){
        System.out.println("After each: " + this);
    }

    @AfterAll
    void closeConnectionPool(){
        System.out.println("Before all: " + this);
    }

    @Nested
    @Tag("login")
    @Timeout(value = 200, unit = TimeUnit.MILLISECONDS)
    class LoginTest{

        @Test
//  @org.junit.Test(expected = IllegalArgumentException.class)
        void throwExceptionIfUsernameOrPasswordIsNull(){
            assertAll(
                    () -> {
                        var exception = assertThrows(IllegalArgumentException.class, () -> userService.login(null, "dummy"));
                        assertThat(exception.getMessage()).isEqualTo("username or password is null");
                    },
                    () -> assertThrows(IllegalArgumentException.class, () -> userService.login("dummy", null))
            );
        }

        @Test
        @Tag("login")
        void loginSuccessIfUserExists(){
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login(IVAN.getUsername(), IVAN.getPassword());

            assertThat(maybeUser).isPresent();
            maybeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
            //assertTrue(maybeUser.isPresent());
            //maybeUser.ifPresent(user -> assertEquals(IVAN, user));
        }

        @Test
        @Disabled("flaky, need to see")
        void loginFailIfUserDoesNotExist() {
            userService.add(IVAN);

            var maybeUser = userService.login("dummy", IVAN.getPassword());


            assertTrue(maybeUser.isEmpty());
        }

        @Test
        void checkLoginFunctionalityPerformance() {
            System.out.println(Thread.currentThread().getName());
            var result = assertTimeoutPreemptively(Duration.ofMillis(200L), () -> {
                System.out.println(Thread.currentThread().getName());
                Thread.sleep(100L);
                return userService.login("dummy", IVAN.getPassword());
            });
        }



//        @Test
        @RepeatedTest(value = 5, name = RepeatedTest.LONG_DISPLAY_NAME)
        void loginFailIfPasswordIsNotCorrect(RepetitionInfo repetitionInfo){
            userService.add(IVAN);
            var maybeUser = userService.login(IVAN.getUsername(), "dummy");

            assertTrue(maybeUser.isEmpty());
        }

        @ParameterizedTest(name = "{arguments} test")
        //@ArgumentsSource()
        @NullSource
        @EmptySource
        @ValueSource(strings = {
                "Ivan", "Petr"
        })
     //   @EnumSource
       // @MethodSource("com.dmdev.junit.service.UserServiceTest#getArgumentsForLoginTest")
       // @CsvFileSource(resources = "/login-test-data.csv", delimiter = ',', numLinesToSkip = 1)
        @CsvSource({
                "Ivan,123",
                "Petr,111"
        })
        void loginParameterizedTests(String username, String password){
            userService.add(IVAN, PETR);

            var maybeUser = userService.login(username, password);
           // assertThat(maybeUser).isEqualTo(user);
        }

    }

    static Stream<Arguments> getArgumentsForLoginTest(){
        return Stream.of(
                Arguments.of("Ivan", "123", Optional.of(IVAN)),
                Arguments.of("Petr", "111", Optional.of(PETR)),
                Arguments.of("Petr", "dummy", Optional.empty()),
                Arguments.of( "dummy", "123", Optional.empty())
        );
    }
}
