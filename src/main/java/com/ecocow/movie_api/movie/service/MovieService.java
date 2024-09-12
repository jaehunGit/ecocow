package com.ecocow.movie_api.movie.service;

import com.ecocow.movie_api.common.response.MovieNotFoundException;
import com.ecocow.movie_api.common.response.ResponseMessage;
import com.ecocow.movie_api.movie.entity.GenreEntity;
import com.ecocow.movie_api.movie.entity.MovieEntity;
import com.ecocow.movie_api.movie.entity.MovieGenreEntity;
import com.ecocow.movie_api.movie.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final GenreRepository genreRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${tmdb.api.key}")
    private String apiKey;
    String TMDB_URL = "https://api.themoviedb.org/3";
    public void savePopularMovies() {
        int totalMoviesToSave = 100;
        int currentSavedMovies = 0;
        int page = 1;

        while (currentSavedMovies < totalMoviesToSave) {
            String url = TMDB_URL + "/movie/popular?page=" + page + "&api_key=" + apiKey + "&language=ko-KR";
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

        List<Long> genreIds = movieGenreRepository.findByMovieId(movie.getId())
                .stream()
                .map(MovieGenreEntity::getGenreId)
                .toList();

        List<String> genreNames = genreIds.stream()
                .map(genreId -> genreRepository.findById(genreId)
                        .map(GenreEntity::getName)
                        .orElse("Unknown"))
                .collect(Collectors.toList());


        dto.setGenres(genreNames);;

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

                List<Long> movieGenreIds = movieGenreRepository.findByMovieId(recommendedMovieId)
                        .stream().map(MovieGenreEntity::getGenreId).toList();

                List<String> genreNames = movieGenreIds.stream()
                        .map(genreId -> genreRepository.findById(genreId)
                                .map(GenreEntity::getName)
                                .orElse("Unknown"))
                        .collect(Collectors.toList());

                movie.setGenres(genreNames);

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

    public void updateMoviesToKorean() {
        List<MovieEntity> movies = movieRepository.findAll();

        for (MovieEntity movie : movies) {
            String url = TMDB_URL + "/movie/" + movie.getId() + "?api_key=" + apiKey + "&language=ko-KR";
            MovieDTO movieDTO = restTemplate.getForObject(url, MovieDTO.class);

            if (movieDTO != null) {
                movie.setTitle(movieDTO.getTitle());
                movie.setOverview(movieDTO.getOverview());
                movie.setPosterPath(movieDTO.getPosterPath());
                movie.setBackdropPath(movieDTO.getBackdropPath());
                movie.setOriginalLanguage(movieDTO.getOriginalLanguage());
                movieRepository.save(movie);
            }
        }
    }

    public void updateGenresFromTMDb() {
        List<Long> genreIds = movieGenreRepository.findDistinctGenreIds();

        for (Long genreId : genreIds) {
            String url = "https://api.themoviedb.org/3/genre/" + genreId + "?api_key=" + apiKey + "&language=ko-KR";
            GenreDTO genreDTO = restTemplate.getForObject(url, GenreDTO.class);

            if (genreDTO != null) {
                saveGenre(genreDTO.getId(), genreDTO.getName());
            }
        }
    }

    private void saveGenre(int genreId, String genreName) {
        if (!genreRepository.existsById((long) genreId)) {
            GenreEntity genreEntity = new GenreEntity();
            genreEntity.setId((long) genreId);
            genreEntity.setName(genreName);
            genreRepository.save(genreEntity);
        }
    }

}
