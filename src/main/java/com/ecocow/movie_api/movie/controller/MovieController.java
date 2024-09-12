package com.ecocow.movie_api.movie.controller;

import com.ecocow.movie_api.common.response.ResponseMessage;
import com.ecocow.movie_api.movie.entity.MovieEntity;
import com.ecocow.movie_api.movie.repository.MovieDTO;
import com.ecocow.movie_api.movie.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Movie API", description = "영화 정보를 제공하는 API")
@RestController
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @PostMapping("/save/popular-movies")
    @Operation(summary = "인기 영화 저장", description = "TMDB에서 인기 영화를 가져와 MySQL DB에 저장합니다.")
    public String savePopularMovies() {
        movieService.savePopularMovies();
        return "영화 정보 저장 성공!";
    }

    @PostMapping("/update/movie")
    @Operation(summary = "영화 영어정보를 한글로 업데이트", description = "Movie 테이블에 있는 전체 정보를 한글로 업데이트합니다.")
    public String updateKorean() {
        movieService.updateMoviesToKorean();

        return "업데이트 성공";
    }


    @PostMapping("/save/genre/name")
    @Operation(summary = "장르 ID를 통해 장르명을 저장", description = "Movie_genre 테이블에 있는 장르 ID를 통해 장르명을 저장합니다.")
    public String saveGenreName() {
        movieService.updateGenresFromTMDb();
        return "장르명 저장 성공";
    }

    @GetMapping("/movie/{id}")
    @Operation(summary = "영화 상세 조회", description = "영화 ID를 사용하여 영화 상세 정보를 조회합니다.")
    public ResponseEntity<ResponseMessage<MovieDTO>> getMovieById(@PathVariable("id") Long id) {
        ResponseMessage<MovieDTO> responseMessage = movieService.getMovieDTOById(id);
        return ResponseEntity.status(responseMessage.getStatusCode()).body(responseMessage);
    }

    @GetMapping("/recommend/{movieId}")
    @Operation(summary = "영화 추천", description = "영화의 ID를 기반으로 유사한 장르의 영화를 추천합니다.")
    public ResponseEntity<List<MovieEntity>> recommendMovies(@PathVariable Long movieId) {

        List<MovieEntity> recommendedMovies = movieService.recommendMoviesByGenre(movieId);
        return ResponseEntity.ok(recommendedMovies);
    }
}
