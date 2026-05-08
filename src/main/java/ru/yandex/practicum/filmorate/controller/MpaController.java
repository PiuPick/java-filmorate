package ru.yandex.practicum.filmorate.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.mpa.MpaRatingDto;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Comparator;
import java.util.List;


@RestController
@RequestMapping("/mpa")
public class MpaController {
    private final MpaService mpaService;

    public MpaController(MpaService mpaService) {
        this.mpaService = mpaService;
    }

    @GetMapping
    public List<MpaRatingDto> getMpaRatings() {
        return mpaService.getAllMpa()
                .stream()
                .map(MpaMapper::mapToMpaDto)
                .sorted(Comparator.comparing(MpaRatingDto::getId))
                .toList();
    }

    @GetMapping("/{id}")
    public MpaRatingDto getMpaRatingById(@PathVariable int id) {
        return MpaMapper.mapToMpaDto(mpaService.getMpaById(id));
    }
}
