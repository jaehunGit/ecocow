package com.ecocow.movie_api.movie.controller;

import com.ecocow.movie_api.movie.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @PostMapping("/save/popular-movies")
    public String savePopularMovies() {
        movieService.savePopularMovies();
        return "Movies saved successfully!";
    }
}
