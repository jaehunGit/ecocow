package com.ecocow.movie_api.movie.repository;

import com.ecocow.movie_api.movie.entity.GenreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface GenreRepository extends JpaRepository<GenreEntity, Long> {
}
