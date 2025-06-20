package com.Nguyen.blogplatform.repository;


import com.Nguyen.blogplatform.model.Rating;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByPostAndUser(Post post, User user);
}
