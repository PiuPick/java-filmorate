package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicateDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();

    private int getNextId() {
        log.info("Начало создания нового id");
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);

        log.info("Создан новый id={}", currentMaxId + 1);
        return ++currentMaxId;
    }

    private void validateFilm(Film film) {
        log.info("Начало валидации фильма");

        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации: Фильм с id={} имеет name=\"{}\"", film.getId(), film.getName());
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Ошибка валидации: Фильм с id={} имеет description.length={}",
                    film.getId(),
                    film.getDescription().length());
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Ошибка валидации: Фильм с id={} имеет releaseDate={}", film.getId(), film.getReleaseDate());
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() < 0) {
            log.error("Ошибка валидации: Фильм с id={} имеет duration={}", film.getId(), film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        films.values().stream()
                .filter(filmFromList ->
                        filmFromList.getId() != film.getId() &&
                                filmFromList.getName().equals(film.getName()))
                .findFirst()
                .ifPresent(duplicateFilm -> {
                    log.error(
                            "Ошибка дублирования данных: фильм с id={} конфликтует по name=\"{}\" с фильмом id={}",
                            film.getId(),
                            film.getName(),
                            duplicateFilm.getId()
                    );
                    throw new DuplicateDataException("Фильм с таким названием уже есть");
                });
        log.info("Выполнена валидация фильма");
    }

    @PostMapping
    public Film createFilm(@RequestBody Film newFilm) {
        log.info("Начало создания фильма");

        newFilm.setId(getNextId());
        validateFilm(newFilm);
        films.put(newFilm.getId(), newFilm);
        log.info("Новый фильм name=\"{}\" с id={} добавлен в каталог", newFilm.getName(), newFilm.getId());

        return newFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film updateFilm) {
        log.info("Начало обновления фильма с id={}", updateFilm.getId());
        if (films.containsKey(updateFilm.getId())) {
            validateFilm(updateFilm);
            films.put(updateFilm.getId(), updateFilm);
            log.info("Обновленный фильм добавлен в каталог");
        } else {
            log.error("Ошибка: фильм с id={} в каталоге не найден", updateFilm.getId());
            throw new NotFoundException("Фильм с id=" + updateFilm.getId() + " не найден");
        }
        return updateFilm;
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }
}
