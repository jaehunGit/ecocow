package com.ecocow.movie_api.movie.service;

import com.ecocow.movie_api.common.response.MovieNotFoundException;
import com.ecocow.movie_api.common.response.ResponseMessage;
import com.ecocow.movie_api.movie.entity.MovieEntity;
import com.ecocow.movie_api.movie.entity.MovieGenreEntity;
import com.ecocow.movie_api.movie.repository.MovieDTO;
import com.ecocow.movie_api.movie.repository.MovieDTOList;
import com.ecocow.movie_api.movie.repository.MovieGenreRepository;
import com.ecocow.movie_api.movie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

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

        movie.setId(movieDTO.getId());
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
            saveMovieGenreRelation(movie.getId(), genreId);
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
                .orElseThrow(() -> new MovieNotFoundException("영화를 찾을 수 없습니다. ID: " + id));
    }

    public double calculateVotePercentage(MovieEntity movie) {
        return (movie.getVoteAverage() / 10) * 100;
    }

    public ResponseMessage<MovieDTO> getMovieDTOById(Long id) {
        MovieEntity movie = getMovieById(id);
        MovieDTO dto = new MovieDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setOverview(movie.getOverview());
        dto.setPosterPath(movie.getPosterPath());
        dto.setBackdropPath(movie.getBackdropPath());
        dto.setVoteAverage(movie.getVoteAverage());
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

    @Cacheable("recommendedMovies")
    public List<MovieEntity> recommendMoviesByGenre(Long movieId) {
        List<MovieGenreEntity> movieGenres = movieGenreRepository.findByMovieId(movieId);

        List<Long> genreIds = movieGenres.stream()
                .map(MovieGenreEntity::getGenreId)
                .toList();

        Map<Long, Integer> movieGenreMatchCount = new HashMap<>();

        List<MovieGenreEntity> moviesInSameGenres = movieGenreRepository.findByGenreIdIn(genreIds);

        for (MovieGenreEntity movieGenre : moviesInSameGenres) {
            Long otherMovieId = movieGenre.getMovieId();
            if (!otherMovieId.equals(movieId)) {
                movieGenreMatchCount.put(otherMovieId, movieGenreMatchCount.getOrDefault(otherMovieId, 0) + 1);
            }
        }

        List<MovieEntity> recommendedMovies = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : movieGenreMatchCount.entrySet()) {
            Long recommendedMovieId = entry.getKey();
            Integer matchingGenres = entry.getValue();
            MovieEntity movie = movieRepository.findById(recommendedMovieId).orElse(null);
            if (movie != null) {
                movie.setMatchingGenres(matchingGenres);
                recommendedMovies.add(movie);
            }
        }

        recommendedMovies.sort((m1, m2) -> {
            int genreComparison = Integer.compare(m2.getMatchingGenres(), m1.getMatchingGenres());
            if (genreComparison == 0) {
                return Float.compare(m2.getPopularity(), m1.getPopularity());
            }
            return genreComparison;
        });


        return recommendedMovies;
    }

}
