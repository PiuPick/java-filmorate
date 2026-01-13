package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final UserValidator userValidator;

    public UserService(UserStorage userStorage, UserValidator userValidator) {
        this.userStorage = userStorage;
        this.userValidator = userValidator;
    }

    public User createUser(User user) {
        if ((user.getName() == null || user.getName().isBlank()) && user.getLogin() != null) {
            log.info("Пользователю с id={} присвоен name=login=\"{}\"", user.getId(), user.getLogin());
            user.setName(user.getLogin());
        }
        userValidator.validate(user);
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        userStorage.getById(user.getId());
        userValidator.validate(user);
        return userStorage.update(user);
    }

    public List<User> getUsers() {
        return userStorage.getAll();
    }

    public void addFriend(int userId, int friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        userStorage.update(user);
        userStorage.update(friend);
    }

    public void deleteFriend(int userId, int friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        userStorage.update(user);
        userStorage.update(friend);
    }

    public Set<User> getCommonFriends(int userId, int friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        Set<Integer> userFriendsIds = user.getFriends();
        Set<Integer> friendFriendsIds = friend.getFriends();

        Set<User> commonFriends = new HashSet<>();

        for (Integer id : userFriendsIds) {
            if (friendFriendsIds.contains(id)) {
                commonFriends.add(userStorage.getById(id));
            }
        }

        return commonFriends;
    }

    public Set<User> getFriends(int userId) {
        User user = userStorage.getById(userId);
        Set<User> friends = new HashSet<>();
        for (Integer friendId : user.getFriends()) {
            friends.add(userStorage.getById(friendId));
        }
        return friends;
    }

    public User getUserById(int id) {
        return userStorage.getById(id);
    }
}
