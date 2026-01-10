package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmStorage filmStorage;

    public FilmController(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    @PostMapping
    public Film createFilm(@RequestBody Film newFilm) {
        return filmStorage.createFilm(newFilm);
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film updatedFilm) {
        return filmStorage.updateFilm(updatedFilm);
    }

    @GetMapping
    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }
}
