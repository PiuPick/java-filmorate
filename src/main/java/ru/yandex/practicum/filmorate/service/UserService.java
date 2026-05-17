package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final UserValidator userValidator;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, UserValidator userValidator) {
        this.userStorage = userStorage;
        this.userValidator = userValidator;
    }

    public UserDto createUser(NewUserRequest newUserReq) {
        if ((newUserReq.getName() == null || newUserReq.getName().isBlank()) && newUserReq.getLogin() != null)
            newUserReq.setName(newUserReq.getLogin());

        User newUser = UserMapper.mapToUser(newUserReq);
        userValidator.validate(newUser);
        return UserMapper.mapToUserDto(userStorage.create(newUser));
    }

    public UserDto updateUser(UpdateUserRequest updUserReq) {
        User updUser = UserMapper.mapToUser(updUserReq);
        userValidator.validate(updUser);
        return UserMapper.mapToUserDto(userStorage.update(updUser));
    }

    public List<UserDto> getUsers() {
        return userStorage.getAll()
                .stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public void addFriend(int userId, int friendId) {
        userStorage.getById(userId);
        userStorage.getById(friendId);
        userStorage.addFriend(userId, friendId);
    }

    public void deleteFriend(int userId, int friendId) {
        userStorage.getById(userId);
        userStorage.getById(friendId);
        userStorage.deleteFriend(userId, friendId);
    }

    public Set<UserDto> getCommonFriends(int userId, int friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        Set<Integer> userFriendsIds = user.getFriends();
        Set<Integer> friendFriendsIds = friend.getFriends();

        Set<UserDto> commonFriends = new HashSet<>();

        for (Integer id : userFriendsIds)
            if (friendFriendsIds.contains(id))
                commonFriends.add(UserMapper.mapToUserDto(userStorage.getById(id)));

        return commonFriends;
    }

    public Set<UserDto> getFriends(int userId) {
        userStorage.getById(userId);
        return userStorage.getFriendsByUserId(userId).stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toSet());
    }

    public UserDto getUserById(int id) {
        return UserMapper.mapToUserDto(userStorage.getById(id));
    }
}
