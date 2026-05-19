package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Repository("genreDbStorage")
public class GenreDbStorage extends BaseDbStorage<Genre> implements GenreStorage {
    private static final String INSERT_FILM_GENRE_QUERY = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String FIND_GENRE_QUERY = "SELECT * FROM genre WHERE id = ?";
    private static final String FIND_ALL_GENRES_QUERY = "SELECT * FROM genre";
    private static final String FIND_GENRES_BY_FILM_ID_QUERY = "SELECT g.* FROM genre g JOIN film_genre fg ON fg.genre_id = g.id WHERE fg.film_id = ?";

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Set<Genre> getAll() {
        return new HashSet<>(findMany(FIND_ALL_GENRES_QUERY));
    }

    @Override
    public Set<Genre> getByFilmId(int filmId) {
        return new HashSet<>(findMany(FIND_GENRES_BY_FILM_ID_QUERY, filmId));
    }

    @Override
    public Genre getById(int id) {
        Optional<Genre> genre = findOne(FIND_GENRE_QUERY, id);
        if (genre.isEmpty()) throw new NotFoundException("Жанр с id=" + id + " не найден");
        return genre.get();
    }

    @Override
    public void addGenreToFilm(int filmId, int genreId) {
        update(INSERT_FILM_GENRE_QUERY, filmId, genreId);
    }
}
