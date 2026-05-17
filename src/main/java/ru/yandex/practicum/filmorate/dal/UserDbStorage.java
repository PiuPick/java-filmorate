package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DuplicateDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Repository("userDbStorage")
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {
    private static final String INSERT_USER_QUERY = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String INSERT_FRIEND_QUERY = "INSERT INTO friendship (user_id, friend_id, status) VALUES (?, ?, 'CONFIRMED')";
    private static final String FIND_USER_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String FIND_ALL_USERS_QUERY = "SELECT * FROM users";
    private static final String FIND_ALL_FRIENDS_QUERY = "SELECT friend_id FROM friendship WHERE user_id = ?";
    private static final String FIND_FRIENDS_BY_USER_ID_QUERY = "SELECT u.id, u.email, u.login, u.name, u.birthday FROM users u JOIN friendship f ON u.id = f.friend_id WHERE f.user_id = ?";
    private static final String FIND_LOGIN_QUERY = "SELECT COUNT(*) FROM users WHERE login = ?";
    private static final String FIND_EMAIL_QUERY = "SELECT COUNT(*) FROM users WHERE email = ?";
    private static final String UPDATE_USER_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public User create(User user) {
        if (jdbc.queryForObject(FIND_LOGIN_QUERY, Integer.class, user.getLogin()) > 0 ||
                jdbc.queryForObject(FIND_EMAIL_QUERY, Integer.class, user.getEmail()) > 0)
            throw new DuplicateDataException("Пользователь с таким логином или email уже существует");

        int id = insert(INSERT_USER_QUERY, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        user.setId(id);

        return user;
    }

    @Override
    public User update(User user) {
        User oldUser = getById(user.getId());

        if (!oldUser.getLogin().equals(user.getLogin()) &&
                jdbc.queryForObject(FIND_LOGIN_QUERY, Integer.class, user.getLogin()) > 0)
            throw new DuplicateDataException("Логин уже используется");

        if (!oldUser.getEmail().equals(user.getEmail()) &&
                jdbc.queryForObject(FIND_EMAIL_QUERY, Integer.class, user.getEmail()) > 0)
            throw new DuplicateDataException("Email уже используется");

        update(UPDATE_USER_QUERY, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());

        return user;
    }

    @Override
    public User getById(int id) {
        Optional<User> optionalUser = findOne(FIND_USER_QUERY, id);
        if (optionalUser.isEmpty()) throw new NotFoundException("Пользователь с id=" + id + " не найден");

        User user = optionalUser.get();
        user.setFriends(new HashSet<>(jdbc.queryForList(FIND_ALL_FRIENDS_QUERY, Integer.class, id)));
        return user;
    }

    @Override
    public List<User> getAll() {
        List<User> users = findMany(FIND_ALL_USERS_QUERY);

        if (!users.isEmpty()) {
            List<Integer> userIds = users.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());

            Map<Integer, Set<Integer>> friendsMap = loadFriendsBatch(userIds);

            for (User user : users)
                user.setFriends(friendsMap.getOrDefault(user.getId(), new HashSet<>()));
        }

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

    @Override
    public Set<User> getFriendsByUserId(int userId) {
        return new HashSet<>(findMany(FIND_FRIENDS_BY_USER_ID_QUERY, userId));
    }

    private Map<Integer, Set<Integer>> loadFriendsBatch(List<Integer> userIds) {
        String inSql = userIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT user_id, friend_id FROM friendship WHERE user_id IN (" + inSql + ")";

        Map<Integer, Set<Integer>> friendsMap = new HashMap<>();
        jdbc.query(sql, rs -> {
            int userId = rs.getInt("user_id");
            int friendId = rs.getInt("friend_id");
            friendsMap.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        }, userIds.toArray());
        return friendsMap;
    }
}
