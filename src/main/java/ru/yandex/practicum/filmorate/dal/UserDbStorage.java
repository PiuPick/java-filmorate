package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DuplicateDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository("userDbStorage")
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {
    private static final String INSERT_USER_QUERY = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String INSERT_FRIEND_QUERY = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";
    private static final String FIND_USER_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String FIND_ALL_USERS_QUERY = "SELECT * FROM users";
    private static final String FIND_ALL_FRIENDS_QUERY = "SELECT friend_id FROM friendship WHERE user_id = ?";
    private static final String FIND_LOGIN_QUERY = "SELECT EXISTS(SELECT 1 FROM users WHERE login = ?)";
    private static final String FIND_EMAIL_QUERY = "SELECT EXISTS(SELECT 1 FROM users WHERE email = ?)";
    private static final String UPDATE_USER_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public User create(User user) {
        if (checkExist(FIND_LOGIN_QUERY, user.getLogin()) || checkExist(FIND_EMAIL_QUERY, user.getEmail()))
            throw new DuplicateDataException("Пользователь с таким логином или email уже существует");

        int id = insert(INSERT_USER_QUERY, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        user.setId(id);

        return user;
    }

    @Override
    public User update(User user) {
        User oldUser = getById(user.getId());

        if (!oldUser.getLogin().equals(user.getLogin()) && checkExist(FIND_LOGIN_QUERY, user.getLogin()))
            throw new DuplicateDataException("Логин уже используется");

        if (!oldUser.getEmail().equals(user.getEmail()) && checkExist(FIND_EMAIL_QUERY, user.getEmail()))
            throw new DuplicateDataException("Email уже используется");

        update(UPDATE_USER_QUERY, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());

        return user;
    }

    @Override
    public User getById(int id) {
        Optional<User> optionalUser = findOne(FIND_USER_QUERY, id);
        if (optionalUser.isEmpty()) throw new NotFoundException("Пользователь с id=" + id + " не найден");

        User user = optionalUser.get();
        user.setFriends(getFriends(id));
        return user;
    }

    @Override
    public List<User> getAll() {
        List<User> users = findMany(FIND_ALL_USERS_QUERY);
        for (User user : users) user.setFriends(getFriends(user.getId()));
        return users;
    }

    @Override
    public void addFriend(int userId, int friendId) {
        update(INSERT_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        update(DELETE_FRIEND_QUERY, userId, friendId);
    }

    private Set<Integer> getFriends(int userId) {
        return new HashSet<>(jdbc.queryForList(FIND_ALL_FRIENDS_QUERY, Integer.class, userId));
    }
}
