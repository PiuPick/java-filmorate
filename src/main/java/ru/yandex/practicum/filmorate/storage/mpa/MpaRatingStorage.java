package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Set;

public interface MpaRatingStorage {
    Set<MpaRating> getAll();

    MpaRating getById(int id);
}
