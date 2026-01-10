package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.DuplicateDataException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserControllerTests {
    private UserController userController;
    private User user;

    @BeforeEach
    void setUp() {
        userController = new UserController();

        user = new User();
        user.setName("leo");
        user.setLogin("login");
        user.setEmail("email@mail.ru");
        user.setBirthday(LocalDate.of(2000, 1, 1));
    }

    @Test
    void createUserWithEmptyEmailShouldThrowValidationException() {
        user.setEmail("");
        ValidationException exception = assertThrows(ValidationException.class, () -> userController.createUser(user));
        assertEquals("Электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
    }

    @Test
    void createUserWithEmailWithoutAtShouldThrowValidationException() {
        user.setEmail("email.ru");
        ValidationException exception = assertThrows(ValidationException.class, () -> userController.createUser(user));
        assertEquals("Электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
    }

    @Test
    void createUserWithEmptyLoginShouldThrowValidationException() {
        user.setLogin("");
        ValidationException exception = assertThrows(ValidationException.class, () -> userController.createUser(user));
        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void createUserWithLoginContainingSpacesShouldThrowValidationException() {
        user.setLogin("l o g i n ");
        ValidationException exception = assertThrows(ValidationException.class, () -> userController.createUser(user));
        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void createUserWithFutureBirthdayShouldThrowValidationException() {
        user.setBirthday(LocalDate.now().plusDays(1));
        ValidationException exception = assertThrows(ValidationException.class, () -> userController.createUser(user));
        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    void createUserWithEmptyNameShouldSetNameToLogin() {
        user.setName("");
        User createdUser = userController.createUser(user);
        assertEquals("login", createdUser.getName());
    }

    @Test
    void createUserWithDuplicateLoginShouldThrowDuplicatedDataException() {
        userController.createUser(user);

        User userDuplicateLogin = new User();
        userDuplicateLogin.setLogin("login");
        userDuplicateLogin.setEmail("another@mail.ru");

        assertThrows(DuplicateDataException.class, () -> userController.createUser(userDuplicateLogin));
    }

    @Test
    void createUserWithDuplicateEmailShouldThrowDuplicatedDataException() {
        userController.createUser(user);

        User userDuplicateEmail = new User();
        userDuplicateEmail.setLogin("anotherLogin");
        userDuplicateEmail.setEmail("email@mail.ru");

        assertThrows(DuplicateDataException.class, () -> userController.createUser(userDuplicateEmail));
    }

    @Test
    void createUserWithNullEmailShouldThrowValidationException() {
        user.setEmail(null);
        ValidationException exception = assertThrows(ValidationException.class, () -> userController.createUser(user));
        assertEquals("Электронная почта не может быть пустой и должна содержать символ @",
                exception.getMessage());
    }

    @Test
    void createUserWithNullLoginShouldThrowValidationException() {
        user.setLogin(null);
        ValidationException exception = assertThrows(ValidationException.class, () -> userController.createUser(user));
        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }
}
