package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Set;

public interface GenreStorage {
    Set<Genre> getAll();

    Set<Genre> getByFilmId(int filmId);

    Genre getById(int id);

    void addGenreToFilm(int filmId, int genreId);
}
