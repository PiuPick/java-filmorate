package ru.yandex.practicum.filmorate.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

@Slf4j
@Component
public class FilmValidator {
    public void validate(Film film) {
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
        if (film.getDuration() <= 0) {
            log.error("Ошибка валидации: Фильм с id={} имеет duration={}", film.getId(), film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        if (film.getMpa() == null || film.getMpa().getId() < 1) {
            log.error("Ошибка валидации: Фильм с id={} имеет Mpa={}", film.getId(), film.getMpa());
            throw new ValidationException("Фильм должен иметь корректный Mpa");
        }
    }
}