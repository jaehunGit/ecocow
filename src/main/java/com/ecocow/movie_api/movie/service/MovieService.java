package com.ecocow.movie_api.movie.service;

import com.ecocow.movie_api.common.response.ResponseMessage;
import com.ecocow.movie_api.movie.entity.MovieEntity;
import com.ecocow.movie_api.movie.entity.MovieGenreEntity;
import com.ecocow.movie_api.movie.repository.MovieDTO;
import com.ecocow.movie_api.movie.repository.MovieDTOList;
import com.ecocow.movie_api.movie.repository.MovieGenreRepository;
import com.ecocow.movie_api.movie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${tmdb.api.key}")
    private String apiKey;
    public void savePopularMovies() {
        int totalMoviesToSave = 100;
        int currentSavedMovies = 0;
        int page = 1;

        while (currentSavedMovies < totalMoviesToSave) {
            String TMDB_URL = "https://api.themoviedb.org/3";
            String url = TMDB_URL + "/movie/popular?page=" + page + "&api_key=" + apiKey;
            MovieDTOList movieDTOList = restTemplate.getForObject(url, MovieDTOList.class);

            System.out.println(url);

            if (movieDTOList != null && movieDTOList.getResults() != null) {
                for (MovieDTO movieDTO : movieDTOList.getResults()) {
                    if (currentSavedMovies >= totalMoviesToSave) {
                        break;
                    }
                    saveMovie(movieDTO);
                    currentSavedMovies++;
                }
            }
            page++;
        }
    }

    private void saveMovie(MovieDTO movieDTO) {
        MovieEntity movie = new MovieEntity();
        movie.setTmdbId((long) movieDTO.getId());
        movie.setTitle(movieDTO.getTitle());
        movie.setOverview(movieDTO.getOverview());
        movie.setReleaseDate(movieDTO.getReleaseDate());
        movie.setPosterPath(movieDTO.getPosterPath());
        movie.setBackdropPath(movieDTO.getBackdropPath());
        movie.setVoteAverage((float) movieDTO.getVoteAverage());
        movie.setVoteCount(movieDTO.getVoteCount());
        movie.setPopularity((float) movieDTO.getPopularity());
        movie.setOriginalLanguage(movieDTO.getOriginalLanguage());

        movieRepository.save(movie);

        for (int genreId : movieDTO.getGenreIds()) {
            saveMovieGenreRelation(movie.getTmdbId(), genreId);
        }
    }

    private void saveMovieGenreRelation(Long movieId, int genreId) {

        MovieGenreEntity movieGenre = new MovieGenreEntity();
        movieGenre.setMovieId(movieId);
        movieGenre.setGenreId((long) genreId);

        movieGenreRepository.save(movieGenre);
    }


    public MovieEntity getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("영화를 찾을 수 없습니다. ID :" + id));
    }

    public double calculateVotePercentage(MovieEntity movie) {
        return (movie.getVoteAverage() / 10) * 100;
    }

    public ResponseMessage<MovieDTO> getMovieDTOById(Long id) {
        MovieEntity movie = getMovieById(id);
        MovieDTO dto = new MovieDTO();
        dto.setId(movie.getId());
        dto.setTdbId(movie.getTmdbId());
        dto.setTitle(movie.getTitle());
        dto.setOverview(movie.getOverview());
        dto.setPosterPath(movie.getPosterPath());
        dto.setBackdropPath(movie.getBackdropPath());
        dto.setVoteAverage(movie.getVoteAverage());
        dto.setVotePercentage(calculateVotePercentage(movie));
        dto.setVoteCount(movie.getVoteCount());
        dto.setPopularity(movie.getPopularity());
        dto.setOriginalLanguage(movie.getOriginalLanguage());
        dto.setReleaseDate(movie.getReleaseDate());

        return ResponseMessage.<MovieDTO>builder()
                .statusCode(HttpStatus.OK)
                .message("영화 상세정보 조회 성공")
                .data(dto)
                .build();
    }

}
