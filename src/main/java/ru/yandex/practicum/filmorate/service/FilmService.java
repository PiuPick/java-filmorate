package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validation.FilmValidator;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final FilmValidator filmValidator;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       FilmValidator filmValidator) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.filmValidator = filmValidator;
    }

    public FilmDto createFilm(NewFilmRequest newFilm) {
        Film film = FilmMapper.mapToFilm(newFilm);
        filmValidator.validate(film);
        return FilmMapper.mapToFilmDto(filmStorage.create(film));
    }

    public FilmDto updateFilm(UpdateFilmRequest updatedFilm) {
        Film film = FilmMapper.mapToFilm(updatedFilm);
        filmValidator.validate(film);
        return FilmMapper.mapToFilmDto(filmStorage.update(film));
    }

    public List<FilmDto> getFilms() {
        return filmStorage.getAll()
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public FilmDto getFilmById(int id) {
        return FilmMapper.mapToFilmDto(filmStorage.getById(id));
    }

    public void addLike(int filmId, int userId) {
        filmStorage.getById(filmId);
        userStorage.getById(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(int filmId, int userId) {
        filmStorage.getById(filmId);
        userStorage.getById(userId);
        filmStorage.deleteLike(filmId, userId);
    }

    public List<FilmDto> getPopularFilms(int limit) {
        return filmStorage.getPopular(limit)
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }
}
