package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.dal.MpaDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.DuplicateDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmRowMapper.class, GenreRowMapper.class, MpaRatingRowMapper.class, UserRowMapper.class,
        FilmDbStorage.class, GenreDbStorage.class, MpaDbStorage.class, UserDbStorage.class})
class FilmorateStorageIntegrationTest {

    @Autowired
    private FilmDbStorage filmStorage;
    @Autowired
    private UserDbStorage userStorage;
    @Autowired
    private GenreDbStorage genreStorage;
    @Autowired
    private MpaDbStorage mpaStorage;

    private User user1;
    private User user2;
    private Film film1;
    private Film film2;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setName("leo");
        user.setLogin("login");
        user.setEmail("email@mail.ru");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user1 = userStorage.create(user);

        user = new User();
        user.setName("bob");
        user.setLogin("zadira");
        user.setEmail("zadira_bob@mail.ru");
        user.setBirthday(LocalDate.of(2012, 6, 7));
        user2 = userStorage.create(user);

        Film film = new Film();
        film.setName("Матрица");
        film.setDescription("Матрица - это система... система есть наш враг");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        MpaRating mpa = new MpaRating();
        mpa.setId(3);
        film.setMpa(mpa);

        Genre genre1 = new Genre();
        genre1.setId(2);
        Genre genre2 = new Genre();
        genre2.setId(5);
        HashSet<Genre> genreSet = new HashSet<>();
        genreSet.add(genre1);
        genreSet.add(genre2);
        film.setGenres(genreSet);

        film1 = filmStorage.create(film);

        film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Мы привыкли смотреть в небо и восхищаться нашим местом среди звёзд");
        film.setReleaseDate(LocalDate.of(2014, 11, 1));
        film.setDuration(120);

        mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        genre1 = new Genre();
        genre1.setId(1);
        genreSet = new HashSet<>();
        genreSet.add(genre1);
        film.setGenres(genreSet);

        film2 = filmStorage.create(film);
    }

    @Test
    void filmCreateShouldReturnFilmWithIdAndSavedFields() {
        Film film = filmStorage.getById(film1.getId());

        assertThat(film).isNotNull();
        assertThat(film.getName()).isEqualTo("Матрица");
        assertThat(film.getMpa().getId()).isEqualTo(3);
        assertThat(film.getGenres()).hasSize(2);
    }

    @Test
    void filmUpdateShouldChangeFieldsAndGenres() {
        film1.setName("Король лев");
        film1.getMpa().setId(4);
        Genre newGenre = new Genre();
        newGenre.setId(3);
        film1.setGenres(Set.of(newGenre));

        filmStorage.update(film1);
        Film updFilm = filmStorage.getById(film1.getId());

        assertThat(updFilm.getName()).isEqualTo("Король лев");
        assertThat(updFilm.getMpa().getId()).isEqualTo(4);
        assertThat(updFilm.getGenres())
                .extracting(Genre::getId)
                .containsExactly(3);
    }

    @Test
    void filmGetByIdShouldThrowNotFoundExceptionWhenNotExists() {
        assertThatThrownBy(() -> filmStorage.getById(999))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void filmGetAllShouldReturnAllFilms() {
        List<Film> all = filmStorage.getAll();
        assertThat(all).hasSize(2);
    }

    @Test
    void filmAddLikeShouldAddLikeToFilm() {
        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film1.getId(), user2.getId());

        Film film = filmStorage.getById(film1.getId());
        assertThat(film.getLikes()).containsExactlyInAnyOrder(user1.getId(), user2.getId());
    }

    @Test
    void filmDeleteLikeShouldRemoveLike() {
        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film1.getId(), user2.getId());
        filmStorage.deleteLike(film1.getId(), user1.getId());

        Film film = filmStorage.getById(film1.getId());
        assertThat(film.getLikes()).containsExactly(user2.getId());
    }

    @Test
    void filmGetPopularShouldReturnSortedFilmsByLikesWithLimit() {
        filmStorage.addLike(film2.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user2.getId());
        filmStorage.addLike(film1.getId(), user1.getId());

        List<Film> popular = filmStorage.getPopular(10);
        assertThat(popular).hasSize(2);
        assertThat(popular.get(0).getId()).isEqualTo(film2.getId());
        assertThat(popular.get(1).getId()).isEqualTo(film1.getId());

        List<Film> limited = filmStorage.getPopular(1);
        assertThat(limited).hasSize(1);
        assertThat(limited.getFirst().getId()).isEqualTo(film2.getId());
    }

    @Test
    void userCreateShouldReturnUserWithIdAndEmptyFriends() {
        User user = userStorage.getById(user1.getId());
        assertThat(user.getEmail()).isEqualTo("email@mail.ru");
        assertThat(user.getLogin()).isEqualTo("login");
        assertThat(user.getFriends()).isEmpty();
    }

    @Test
    void userCreateShouldThrowDuplicateDataExceptionWhenLoginOrEmailExists() {
        User duplicateLogin = new User();
        duplicateLogin.setEmail("dup@mail.ru");
        duplicateLogin.setLogin("login");
        duplicateLogin.setName("dup");
        duplicateLogin.setBirthday(LocalDate.now());

        assertThatThrownBy(() -> userStorage.create(duplicateLogin))
                .isInstanceOf(DuplicateDataException.class);

        User duplicateEmail = new User();
        duplicateEmail.setEmail("email@mail.ru");
        duplicateEmail.setLogin("notDup");
        duplicateEmail.setName("dup");
        duplicateEmail.setBirthday(LocalDate.now());

        assertThatThrownBy(() -> userStorage.create(duplicateEmail))
                .isInstanceOf(DuplicateDataException.class);
    }

    @Test
    void userUpdateShouldChangeFieldsAndCheckUniqueness() {
        user1.setName("Hakuna matata");
        user1.setEmail("timon_i_pumba@mail.ru");
        userStorage.update(user1);

        User updated = userStorage.getById(user1.getId());
        assertThat(updated.getName()).isEqualTo("Hakuna matata");
        assertThat(updated.getEmail()).isEqualTo("timon_i_pumba@mail.ru");
    }

    @Test
    void userUpdateShouldThrowDuplicateDataExceptionWhenChangeToExistingLoginOrEmail() {
        User conflictLogin = userStorage.getById(user1.getId());
        conflictLogin.setLogin("zadira");
        assertThatThrownBy(() -> userStorage.update(conflictLogin))
                .isInstanceOf(DuplicateDataException.class);

        User conflictEmail = userStorage.getById(user1.getId());
        conflictEmail.setEmail("zadira_bob@mail.ru");
        assertThatThrownBy(() -> userStorage.update(conflictEmail))
                .isInstanceOf(DuplicateDataException.class);
    }

    @Test
    void userGetByIdShouldThrowNotFoundExceptionWhenNotExists() {
        assertThatThrownBy(() -> userStorage.getById(999))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void userGetAllShouldReturnAllUsers() {
        List<User> all = userStorage.getAll();
        assertThat(all).hasSize(2);
    }

    @Test
    void userAddFriendShouldAddFriendId() {
        userStorage.addFriend(user1.getId(), user2.getId());

        User user = userStorage.getById(user1.getId());
        assertThat(user.getFriends()).containsExactly(user2.getId());
    }

    @Test
    void userDeleteFriendShouldRemoveFriendId() {
        userStorage.addFriend(user1.getId(), user2.getId());
        userStorage.deleteFriend(user1.getId(), user2.getId());

        User user = userStorage.getById(user1.getId());
        assertThat(user.getFriends()).isEmpty();
    }

    @Test
    void userFriendsShouldNotBeMutualAutomatically() {
        userStorage.addFriend(user1.getId(), user2.getId());
        User friend = userStorage.getById(user2.getId());
        assertThat(friend.getFriends()).doesNotContain(user1.getId());
    }

    @Test
    void genreGetAllShouldReturnSixGenres() {
        Set<Genre> genres = genreStorage.getAll();
        assertThat(genres).hasSize(6);
    }

    @Test
    void genreGetByIdShouldReturnCorrectGenre() {
        Genre genre = genreStorage.getById(1);
        assertThat(genre.getName()).isEqualTo("Комедия");
    }

    @Test
    void mpaGetAllShouldReturnFiveRatings() {
        Set<MpaRating> ratings = mpaStorage.getAll();
        assertThat(ratings).hasSize(5);
    }

    @Test
    void mpaGetByIdShouldReturnCorrectMpa() {
        MpaRating mpa = mpaStorage.getById(1);
        assertThat(mpa.getName()).isEqualTo("G");
    }

    @Test
    void genreGetByFilmIdShouldReturnCorrectGenresForFilm() {
        Set<Genre> genres = genreStorage.getByFilmId(film1.getId());
        assertThat(genres)
                .hasSize(2)
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(2, 5);
    }

    @Test
    void genreGetByFilmIdShouldReturnEmptyForFilmWithoutGenres() {
        film2.setGenres(new HashSet<>());
        filmStorage.update(film2);

        Set<Genre> genres = genreStorage.getByFilmId(film2.getId());
        assertThat(genres).isEmpty();
    }

    @Test
    void genreAddGenreToFilmShouldPersistNewGenreAssociation() {
        genreStorage.addGenreToFilm(film2.getId(), 4);
        Set<Genre> genres = genreStorage.getByFilmId(film2.getId());
        assertThat(genres)
                .hasSize(2)
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(1, 4);
    }
}
