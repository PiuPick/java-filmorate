package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaRatingStorage;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Repository("mpaDbStorage")
public class MpaDbStorage extends BaseDbStorage<MpaRating> implements MpaRatingStorage {
    private static final String FIND_MPA_QUERY = "SELECT * FROM mpa_rating WHERE id = ?";
    private static final String FIND_ALL_MPA_QUERY = "SELECT * FROM mpa_rating";

    public MpaDbStorage(JdbcTemplate jdbc, RowMapper<MpaRating> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Set<MpaRating> getAll() {
        return new HashSet<>(findMany(FIND_ALL_MPA_QUERY));
    }

    @Override
    public MpaRating getById(int id) {
        Optional<MpaRating> mpa = findOne(FIND_MPA_QUERY, id);
        if (mpa.isEmpty()) throw new NotFoundException("MpaRating с id=" + id + " не существует");
        return mpa.get();
    }
}
