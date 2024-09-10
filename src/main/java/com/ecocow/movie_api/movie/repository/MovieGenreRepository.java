package com.ecocow.movie_api.movie.repository;

import com.ecocow.movie_api.movie.entity.MovieGenreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieGenreRepository extends JpaRepository<MovieGenreEntity, Long> {
    List<MovieGenreEntity> findByMovieId(Long tmdbId);
}
