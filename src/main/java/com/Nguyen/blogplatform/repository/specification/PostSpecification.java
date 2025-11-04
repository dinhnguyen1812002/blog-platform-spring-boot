package com.Nguyen.blogplatform.repository.specification;

import com.Nguyen.blogplatform.model.Post;
import org.springframework.data.jpa.domain.Specification;

public class PostSpecification {
//    public static Specification<Post> hasTitle(String title) {
//        return (root, query, criteriaBuilder) ->
//                title == null ? criteriaBuilder.conjunction() : criteriaBuilder.like(root.get("title"), "%" + title + "%");
//    }
//
//    public static Specification<Post> hasCategoryId(Long categoryId) {
//        return (root, query, criteriaBuilder) ->
//                categoryId == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("categoryId"), categoryId);
//    }

    public static Specification<Post> hasTitle(String title) {
        return (root, query, cb) -> {
            if (title == null || title.isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(root.get("title"), "%" + title + "%");
        };
    }

    public static Specification<Post> hasCategoryId(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.join("categories").get("id"), categoryId);
        };
    }

    public static Specification<Post> hasCategorySlug(String categorySlug) {
        return (root, query, cb) -> {
            if (categorySlug == null || categorySlug.isEmpty()) {
                return cb.conjunction();
            }
            return cb.equal(root.join("categories").get("slug"), categorySlug);
        };
    }


    public static Specification<Post> hasTagSlug(String tagSlug) {
        return (root, query, cb) -> {
            if (tagSlug == null || tagSlug.isEmpty()) {
                return cb.conjunction();
            }
            return cb.equal(root.join("tags").get("slug"), tagSlug);
        };
    }

    public static Specification<Post> isPublished() {
        return (root, query, cb) -> cb.isTrue(root.get("is_publish"));
    }

    public static Specification<Post> isFeatured(Boolean featured) {
        return (root, query, cb) -> {
            if (featured == null) {
                return cb.conjunction(); // ignore filter
            }
            return cb.equal(root.get("featured"), featured);
        };
    }

    public static Specification<Post> isFeatured() {
        return (root, query, cb) -> cb.isTrue(root.get("featured"));
    }


    public static Specification<Post> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isEmpty()) {
                return cb.conjunction();
            }
            String likePattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), likePattern),
                    cb.like(cb.lower(root.get("content")), likePattern)
            );
        };
    }


}
