package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film createFilm(Film newFilm);

    Film updateFilm(Film updatedFilm);

    List<Film> getFilms();
}
