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

    @PostMapping("/save/{movieId}")
    public String saveMovie(@PathVariable int movieId) {
//        movieService.saveMovie(movieId);
        return "Movie saved successfully!";
    }
}
