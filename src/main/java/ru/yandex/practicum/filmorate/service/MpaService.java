package ru.yandex.practicum.filmorate.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaRatingStorage;

import java.util.Set;

@Slf4j
@Service
public class MpaService {
    private final MpaRatingStorage mpaStorage;

    public MpaService(MpaRatingStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public Set<MpaRating> getAllMpa() {
        return mpaStorage.getAll();
    }

    public MpaRating getMpaById(int id) {
        return mpaStorage.getById(id);
    }
}
