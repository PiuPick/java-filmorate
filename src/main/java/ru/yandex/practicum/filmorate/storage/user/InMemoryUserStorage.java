package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

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

    @Override
    public User create(User user) {
        log.info("Начало создания пользователя");

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Новый пользователь name=\"{}\" с id={} добавлен в каталог", user.getName(), user.getId());

        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        log.info("Обновленный пользователь добавлен в каталог");
        return user;
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
