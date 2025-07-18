package com.Nguyen.blogplatform.repository;

import com.Nguyen.blogplatform.model.Category;
import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, String>, JpaSpecificationExecutor<Post> {

    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    List<Post> findTopNByOrderByCreatedAtDesc(@Param("limit") int limit);
    List<Post> findByFeaturedTrueOrderByCreatedAtDesc(Pageable pageable);
    List<Post> findByCategoriesContaining(Category category);
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:title%")
    List<Post> findByTitleContaining(@Param("title") String title);
    @Query("SELECT p FROM Post p WHERE p.user.username = :username")
    Page<Post> findByUser(String username, Pageable pageable);

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
//    @Query("SELECT p FROM Post p JOIN p.categories c WHERE c.id = :categoryId")
//    List<Post> findByCategoryId(@Param("categoryId") Long categoryId);
//
//    @Query("SELECT p FROM Post p JOIN p.categories c WHERE p.title LIKE %:title% AND c.id = :categoryId")
//    List<Post> findByTitleContainingAndCategoryId(@Param("title") String title, @Param("categoryId") Long categoryId);
@EntityGraph(attributePaths = {"comments", "comments.user"})
Optional<Post> findBySlug(String slug);

    @Modifying
    @Query(value = "UPDATE post SET view = view + 1 WHERE id = :postId", nativeQuery = true)
    void incrementViewCount(@Param("postId") String postId);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.user = :user")
    Long countByUser(@Param("user") User user);

}
