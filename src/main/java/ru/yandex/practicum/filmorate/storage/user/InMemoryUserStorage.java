package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicateDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();

    private int getNextId() {
        log.info("Начало создания нового id");
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);

        log.info("Создан новый id={}", currentMaxId + 1);
        return ++currentMaxId;
    }

    private void validateUser(User user) {
        log.info("Начало валидации пользователя");

        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Ошибка валидации: пользователь с id={} имеет email=\"{}\"", user.getId(), user.getEmail());
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Ошибка валидации: пользователь с id={} имеет login=\"{}\"", user.getId(), user.getLogin());
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        users.values().stream()
                .filter(userFromList -> userFromList.getId() != user.getId() &&
                        (userFromList.getLogin().equals(user.getLogin()) ||
                                userFromList.getEmail().equals(user.getEmail())))
                .findFirst()
                .ifPresent(duplicateUser -> {
                    log.error("Ошибка дублирования данных: пользователь с id={} конфликтует с пользователем id={}",
                            user.getId(),
                            duplicateUser.getId());
                    throw new DuplicateDataException("Пользователь с таким логином или email уже существует");
                });
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Ошибка валидации: пользователь с id={} имеет birthday=\"{}\"", user.getId(), user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Пользователю с id={} присвоен name=login=\"{}\"", user.getId(), user.getLogin());
            user.setName(user.getLogin());
        }
    }

    @Override
    public User create(User newUser) {
        log.info("Начало создания пользователя");

        newUser.setId(getNextId());
        validateUser(newUser);
        users.put(newUser.getId(), newUser);
        log.info("Новый пользователь name=\"{}\" с id={} добавлен в каталог", newUser.getName(), newUser.getId());

        return newUser;
    }

    @Override
    public User update(User updatedUser) {
        log.info("Начало обновления пользователя с id={}", updatedUser.getId());
        if (users.containsKey(updatedUser.getId())) {
            validateUser(updatedUser);
            users.put(updatedUser.getId(), updatedUser);
            log.info("Обновленный пользователь добавлен в каталог");
        } else {
            log.error("Ошибка: пользователь с id={} в каталоге не найден", updatedUser.getId());
            throw new NotFoundException("Пользователь с id=" + updatedUser.getId() + " не найден");
        }
        return updatedUser;
    }

    @Override
    public User getById(int id) {
        log.info("Начало поиска пользователя с id={}", id);
        if (users.containsKey(id)) {
            log.info("Пользователь с id={} найден", id);
            return users.get(id);
        } else {
            log.error("Ошибка: пользователь с id={} не найден", id);
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
    }

    @Override
    public List<User> getAll() {
        return List.copyOf(users.values());
    }
}
