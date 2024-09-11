package com.ecocow.movie_api.movie.controller;

import com.ecocow.movie_api.common.response.ResponseMessage;
import com.ecocow.movie_api.movie.entity.MovieEntity;
import com.ecocow.movie_api.movie.repository.MovieDTO;
import com.ecocow.movie_api.movie.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @PostMapping("/save/popular-movies")
    public String savePopularMovies() {
        movieService.savePopularMovies();
        return "영화 정보 저장 성공!";
    }

    @GetMapping("/movie/{id}")
    public ResponseEntity<ResponseMessage<MovieDTO>> getMovieById(@PathVariable("id") Long id) {
        ResponseMessage<MovieDTO> responseMessage = movieService.getMovieDTOById(id);
        return ResponseEntity.status(responseMessage.getStatusCode()).body(responseMessage);
    }

    @GetMapping("/recommend/{movieId}")
    public ResponseEntity<List<MovieEntity>> recommendMovies(@PathVariable Long movieId) {

        List<MovieEntity> recommendedMovies = movieService.recommendMoviesByGenre(movieId);
        return ResponseEntity.ok(recommendedMovies);
    }
}
