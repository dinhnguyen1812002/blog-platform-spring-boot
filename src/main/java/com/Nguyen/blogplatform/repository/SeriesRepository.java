package com.Nguyen.blogplatform.repository;

import com.Nguyen.blogplatform.model.Series;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface SeriesRepository extends JpaRepository<Series, String> {
}
