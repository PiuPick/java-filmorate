package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicateDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private final Set<String> logins = new HashSet<>();
    private final Set<String> emails = new HashSet<>();

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

        if (logins.contains(user.getLogin()) || emails.contains(user.getEmail())) {
            throw new DuplicateDataException("Пользователь с таким логином или email уже существует");
        }
        logins.add(user.getLogin());
        emails.add(user.getEmail());

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Новый пользователь name=\"{}\" с id={} добавлен в каталог", user.getName(), user.getId());

        return user;
    }

    @Override
    public User update(User user) {
        User oldUser = users.get(user.getId());
        if (oldUser == null) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        if (!oldUser.getLogin().equals(user.getLogin()) && logins.contains(user.getLogin())) {
            throw new DuplicateDataException("Логин уже используется");
        }
        if (!oldUser.getEmail().equals(user.getEmail()) && emails.contains(user.getEmail())) {
            throw new DuplicateDataException("Email уже используется");
        }

        logins.remove(oldUser.getLogin());
        emails.remove(oldUser.getEmail());

        logins.add(user.getLogin());
        emails.add(user.getEmail());

        users.put(user.getId(), user);
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

    @Override
    public void addFriend(int userId, int friendId) {
        User user = getById(userId);
        user.getFriends().add(friendId);
        update(user);
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        User user = getById(userId);
        user.getFriends().remove(friendId);
        update(user);
    }

    @Override
    public Set<User> getFriendsByUserId(int userId) {
        User user = getById(userId);
        Set<User> friends = new HashSet<>();
        for (Integer friendId : user.getFriends()) {
            friends.add(getById(friendId));
        }
        return friends;
    }
}
