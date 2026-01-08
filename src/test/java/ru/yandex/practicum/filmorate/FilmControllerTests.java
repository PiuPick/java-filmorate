package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.DuplicateDataException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilmControllerTests {
    private FilmController filmController;
    private Film film;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();

        film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
    }

    @Test
    void createFilmWithEmptyNameShouldThrowValidationException() {
        film.setName("");
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.createFilm(film));
        assertEquals("Название не может быть пустым", exception.getMessage());
    }

    @Test
    void createFilmWithTooLongDescriptionShouldThrowValidationException() {
        film.setDescription("А".repeat(201));
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.createFilm(film));
        assertEquals("Максимальная длина описания — 200 символов", exception.getMessage());
    }

    @Test
    void createFilmWithEarlyReleaseDateShouldThrowValidationException() {
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.createFilm(film));
        assertEquals("Дата релиза — не раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void createFilmWithNegativeDurationShouldThrowValidationException() {
        film.setDuration(-10);
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.createFilm(film));
        assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    void createDuplicateFilmShouldThrowDuplicatedDataException() {
        filmController.createFilm(film);

        Film filmDuplicate = new Film();
        filmDuplicate.setName("Фильм");
        filmDuplicate.setReleaseDate(LocalDate.of(2000, 1, 1));

        assertThrows(DuplicateDataException.class, () -> filmController.createFilm(filmDuplicate));
    }
}
