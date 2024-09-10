package com.ecocow.movie_api.movie.controller;

import com.ecocow.movie_api.common.response.ResponseMessage;
import com.ecocow.movie_api.movie.repository.MovieDTO;
import com.ecocow.movie_api.movie.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/movie/{id}")
    public ResponseEntity<ResponseMessage<MovieDTO>> getMovieById(@PathVariable("id") Long id) {
        ResponseMessage<MovieDTO> responseMessage = movieService.getMovieDTOById(id);
        return ResponseEntity.status(responseMessage.getStatusCode()).body(responseMessage);
    }
}
