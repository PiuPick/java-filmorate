package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MpaRatingRowMapper implements RowMapper<MpaRating> {
    @Override
    public MpaRating mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        MpaRating mpa = new MpaRating();
        mpa.setId(resultSet.getInt("id"));
        mpa.setName(resultSet.getString("name"));
        mpa.setDescription(resultSet.getString("description"));
        return mpa;
    }
}
