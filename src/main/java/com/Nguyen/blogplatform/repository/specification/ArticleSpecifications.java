package com.Nguyen.blogplatform.repository.specification;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.Nguyen.blogplatform.model.Post;

import jakarta.persistence.criteria.JoinType;

public class ArticleSpecifications {
    public static Specification<Post> filterArticle(String username, String keyword, String categoryName, String tagName) {
          return (root, query, cb) -> {
            var predicates = cb.conjunction();

            // 🔹 Lọc theo user hiện tại
            if (username != null) {
                predicates.getExpressions().add(cb.equal(root.get("user").get("username"), username));
            }

            // 🔹 Lọc theo keyword (title hoặc content)
            if (keyword != null && !keyword.isBlank()) {
                String likeKeyword = "%" + keyword.toLowerCase() + "%";
                predicates.getExpressions().add(
                        cb.or(
                                cb.like(cb.lower(root.get("title")), likeKeyword),
                                cb.like(cb.lower(root.get("content")), likeKeyword)
                        )
                );
            }

            // 🔹 Lọc theo tên category
            if (categoryName != null && !categoryName.isBlank()) {
                var categoriesJoin = root.join("categories", JoinType.LEFT);
                predicates.getExpressions().add(
                        cb.like(cb.lower(categoriesJoin.get("category")), "%" + categoryName.toLowerCase() + "%")
                );
                query.distinct(true); // tránh trùng lặp post do join
            }

            // 🔹 Lọc theo tên tag
            if (tagName != null && !tagName.isBlank()) {
                var tagsJoin = root.join("tags", JoinType.LEFT);
                predicates.getExpressions().add(
                        cb.like(cb.lower(tagsJoin.get("name")), "%" + tagName.toLowerCase() + "%")
                );
                query.distinct(true);
            }

            return predicates;
        };
    }

}

