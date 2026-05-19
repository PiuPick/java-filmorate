package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.dto.mpa.MpaRatingDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilmMapper {
    public static Film mapToFilm(NewFilmRequest film) {
        return mapCommonFields(
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa(),
                film.getGenres()
        );
    }

    public static Film mapToFilm(UpdateFilmRequest updFilm) {
        Film film = mapCommonFields(
                updFilm.getName(),
                updFilm.getDescription(),
                updFilm.getReleaseDate(),
                updFilm.getDuration(),
                updFilm.getMpa(),
                updFilm.getGenres()
        );
        film.setId(updFilm.getId());
        return film;
    }

    private static Film mapCommonFields(String name,
                                        String description,
                                        LocalDate releaseDate,
                                        int duration,
                                        MpaRatingDto mpaRating,
                                        Set<GenreDto> genres) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(duration);

        MpaRating mpa = new MpaRating();
        if (mpaRating != null)
            mpa.setId(mpaRating.getId());
        film.setMpa(mpa);

        if (genres != null && !genres.contains(null)) {
            film.setGenres(genres
                    .stream()
                    .map(GenreMapper::mapToGenre)
                    .collect(Collectors.toSet()));
        }
        return film;
    }

    public static FilmDto mapToFilmDto(Film film) {
        FilmDto filmDto = new FilmDto();
        filmDto.setId(film.getId());
        filmDto.setName(film.getName());
        filmDto.setDescription(film.getDescription());
        filmDto.setReleaseDate(film.getReleaseDate());
        filmDto.setDuration(film.getDuration());

        if (film.getMpa() != null)
            filmDto.setMpa(MpaMapper.mapToMpaDto(film.getMpa()));

        filmDto.setGenres(film.getGenres()
                .stream()
                .map(GenreMapper::mapToGenreDto)
                .sorted(Comparator.comparing(GenreDto::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new)));

        filmDto.setLikes(film.getLikes());

        return filmDto;
    }
}