package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaRatingStorage;

import java.util.*;
import java.util.stream.Collectors;

@Repository("filmDbStorage")
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private static final String INSERT_LIKE_QUERY = "INSERT INTO film_like (film_id, user_id, created_at) VALUES (?, ?, LOCALTIMESTAMP)";
    private static final String INSERT_FILM_QUERY = "INSERT INTO film (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String FIND_FILM_QUERY = "SELECT * FROM film WHERE id = ?";
    private static final String FIND_ALL_FILMS_QUERY = "SELECT * FROM film";
    private static final String FIND_POPULAR_FILMS_QUERY = "SELECT f.* FROM film f LEFT JOIN film_like fl ON f.id = fl.film_id GROUP BY f.id ORDER BY COUNT(fl.user_id) DESC LIMIT ?";
    private static final String FIND_LIKES_QUERY = "SELECT user_id FROM film_like WHERE film_id = ?";
    private static final String UPDATE_FILM_QUERY = "UPDATE film SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?";
    private static final String DELETE_FILM_GENRE_QUERY = "DELETE FROM film_genre WHERE film_id = ?";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM film_like WHERE film_id = ? AND user_id = ?";
    private final MpaRatingStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final RowMapper<MpaRating> mpaMapper;
    private final RowMapper<Genre> genreMapper;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper,
                         MpaRatingStorage mpaStorage, GenreStorage genreStorage,
                         RowMapper<MpaRating> mpaMapper, RowMapper<Genre> genreMapper) {
        super(jdbc, mapper);
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.mpaMapper = mpaMapper;
        this.genreMapper = genreMapper;
    }

    @Override
    public Film create(Film film) {
        mpaStorage.getById(film.getMpa().getId());

        int id = insert(INSERT_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId());

        film.setId(id);

        for (Genre genre : film.getGenres()) {
            genreStorage.getById(genre.getId());
            genreStorage.addGenreToFilm(id, genre.getId());
        }

        return getById(id);
    }

    @Override
    public Film update(Film film) {
        getById(film.getId());
        mpaStorage.getById(film.getMpa().getId());

        update(UPDATE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        update(DELETE_FILM_GENRE_QUERY, film.getId());
        for (Genre genre : film.getGenres()) {
            genreStorage.getById(genre.getId());
            genreStorage.addGenreToFilm(film.getId(), genre.getId());
        }

        return getById(film.getId());
    }

    @Override
    public Film getById(int id) {
        Optional<Film> filmOptional = findOne(FIND_FILM_QUERY, id);
        if (filmOptional.isEmpty()) throw new NotFoundException("Фильм с id=" + id + " не найден");

        Film film = filmOptional.get();
        film.setMpa(mpaStorage.getById(film.getMpa().getId()));
        film.setGenres(genreStorage.getByFilmId(film.getId()));
        film.setLikes(new HashSet<>(jdbc.queryForList(FIND_LIKES_QUERY, Integer.class, film.getId())));
        return film;
    }

    @Override
    public List<Film> getAll() {
        List<Film> films = findMany(FIND_ALL_FILMS_QUERY);
        enrichFilmsBatch(films);
        return films;
    }

    @Override
    public void addLike(int filmId, int userId) {
        update(INSERT_LIKE_QUERY, filmId, userId);
    }

    @Override
    public void deleteLike(int filmId, int userId) {
        update(DELETE_LIKE_QUERY, filmId, userId);
    }

    @Override
    public List<Film> getPopular(int limit) {
        List<Film> films = findMany(FIND_POPULAR_FILMS_QUERY, limit);
        enrichFilmsBatch(films);
        return films;
    }

    private void enrichFilmsBatch(List<Film> films) {
        if (films.isEmpty()) return;

        List<Integer> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());

        Map<Integer, MpaRating> mpaMap = loadMpaForFilms(filmIds);
        Map<Integer, Set<Genre>> genresMap = loadGenresForFilms(filmIds);
        Map<Integer, Set<Integer>> likesMap = loadLikesForFilms(filmIds);

        for (Film film : films) {
            film.setMpa(mpaMap.getOrDefault(film.getMpa().getId(), film.getMpa()));
            film.setGenres(genresMap.getOrDefault(film.getId(), new HashSet<>()));
            film.setLikes(likesMap.getOrDefault(film.getId(), new HashSet<>()));
        }
    }

    private Map<Integer, MpaRating> loadMpaForFilms(List<Integer> filmIds) {
        String inSql = filmIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT * FROM mpa_rating WHERE id IN (" + inSql + ")";
        List<MpaRating> mpas = jdbc.query(sql, mpaMapper, filmIds.toArray());
        return mpas.stream().collect(Collectors.toMap(MpaRating::getId, mpa -> mpa));
    }

    private Map<Integer, Set<Genre>> loadGenresForFilms(List<Integer> filmIds) {
        String inSql = filmIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT fg.film_id, g.id, g.name FROM film_genre fg JOIN genre g ON fg.genre_id = g.id WHERE fg.film_id IN (" + inSql + ")";

        Map<Integer, Set<Genre>> genres = new HashMap<>();
        jdbc.query(sql, rs -> {
            int filmId = rs.getInt("film_id");
            Genre genre = genreMapper.mapRow(rs, rs.getRow());
            genres.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
        }, filmIds.toArray());
        return genres;
    }

    private Map<Integer, Set<Integer>> loadLikesForFilms(List<Integer> filmIds) {
        String inSql = filmIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT film_id, user_id FROM film_like WHERE film_id IN (" + inSql + ")";

        Map<Integer, Set<Integer>> likesMap = new HashMap<>();
        jdbc.query(sql, rs -> {
            int filmId = rs.getInt("film_id");
            int userId = rs.getInt("user_id");
            likesMap.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        }, filmIds.toArray());
        return likesMap;
    }
}
