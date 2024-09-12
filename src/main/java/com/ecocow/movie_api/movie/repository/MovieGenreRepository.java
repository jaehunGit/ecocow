package com.ecocow.movie_api.movie.repository;

import com.ecocow.movie_api.movie.entity.MovieGenreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieGenreRepository extends JpaRepository<MovieGenreEntity, Long> {

    List<MovieGenreEntity> findByMovieId(Long movieId);

    List<MovieGenreEntity> findByGenreIdIn(List<Long> genreIds);

    @Query("SELECT DISTINCT m.genreId FROM movie_genre m")
    List<Long> findDistinctGenreIds();
}
