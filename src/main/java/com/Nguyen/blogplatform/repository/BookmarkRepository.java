package com.Nguyen.blogplatform.repository;

import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.model.Bookmark;
import com.Nguyen.blogplatform.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, String> {
    
    Optional<Bookmark> findByUserAndPost(User user, Post post);
    
    @Query("SELECT sp FROM Bookmark sp WHERE sp.user.username = :username ORDER BY sp.savedAt DESC")
    Page<Bookmark> findByUserUsernameOrderBySavedAtDesc(@Param("username") String username, Pageable pageable);
    
    @Query("SELECT sp FROM Bookmark sp WHERE sp.user.username = :username ORDER BY sp.savedAt DESC")
    List<Bookmark> findByUserUsernameOrderBySavedAtDesc(@Param("username") String username);
    
    @Query("SELECT sp FROM Bookmark sp WHERE sp.user = :user ORDER BY sp.savedAt DESC")
    Page<Bookmark> findByUserOrderBySavedAtDesc(@Param("user") User user, Pageable pageable);
    
    @Query("SELECT sp FROM Bookmark sp WHERE sp.user = :user ORDER BY sp.savedAt DESC")
    List<Bookmark> findByUserOrderBySavedAtDesc(@Param("user") User user);
    
    @Query("SELECT COUNT(sp) FROM Bookmark sp WHERE sp.user = :user")
    Long countByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(sp) FROM Bookmark sp WHERE sp.post = :post")
    Long countByPost(@Param("post") Post post);
    
    Boolean existsByUserAndPost(User user, Post post);
    
    void deleteByUserAndPost(User user, Post post);
    
    @Query("SELECT sp.post FROM Bookmark sp WHERE sp.user = :user ORDER BY sp.savedAt DESC")
    List<Post> findPostsByUserOrderBySavedAtDesc(@Param("user") User user);
}
