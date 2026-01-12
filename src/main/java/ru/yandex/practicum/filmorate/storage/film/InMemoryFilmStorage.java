package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
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

    @Override
    public Film create(Film film) {
        log.info("Начало создания фильма");

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Новый фильм name=\"{}\" с id={} добавлен в каталог", film.getName(), film.getId());

        return film;
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        log.info("Обновленный фильм добавлен в каталог");
        return film;
    }

    @Override
    public Film getById(int id) {
        log.info("Начало поиска фильма с id={}", id);
        if (films.containsKey(id)) {
            log.info("Фильм с id={} найден", id);
            return films.get(id);
        } else {
            log.error("Ошибка: фильм с id={} в каталоге не найден", id);
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
    }

    @Override
    public List<Film> getAll() {
        return List.copyOf(films.values());
    }
}
