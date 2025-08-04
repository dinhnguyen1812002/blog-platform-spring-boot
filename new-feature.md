
# New Features Plan

## 1. Get 5 Featured Authors

*   **`UserRepository`:**
    *   Create a new method `findTop5AuthorsByPostCount` to find users with the "AUTHOR" role and sort them by the number of posts they have written.
*   **`AuthorServices`:**
    *   Create a new method `getFeaturedAuthors` to call the new repository method and return the top 5 authors.
*   **`AuthorController`:**
    *   Create a new endpoint `/api/v1/author/featured` to expose this functionality.

## 2. Get 5 Posts with the Highest Ratings

*   **`PostRepository`:**
    *   Create a new method `findTop5ByOrderByAverageRatingDesc` to find posts with the highest average ratings.
*   **`PostService`:**
    *   Create a new method `getTopRatedPosts` to call the new repository method and return the top 5 posts.
*   **`PostController`:**
    *   Create a new endpoint `/api/v1/post/top-rated` to expose this functionality.

## 3. Traffic Analysis for Each Author

*   **DTO:**
    *   Create a new DTO `AuthorTrafficResponse` to hold author traffic data (total posts, total views, total likes).
*   **`AuthorServices`:**
    *   Create a new method `getAuthorTraffic` to calculate the traffic data for a given author.
*   **`AuthorController`:**
    *   Create a new endpoint `/api/v1/author/{authorId}/traffic` to get traffic data for a specific author.

## 4. Tag and Category Analysis for Trending Topics

*   **DTOs:**
    *   Create new DTOs `TagUsageResponse` and `CategoryUsageResponse` to hold usage data.
*   **`TagServices` & `CategoryServices`:**
    *   Create new methods `getTagUsage` and `getCategoryUsage` to calculate the usage of each tag and category.
*   **`TagController` & `CategoryController`:**
    *   Create new endpoints `/api/v1/tags/usage` and `/api/v1/categories/usage` to expose this data for chart generation.

## 5. Trending Posts for Charting

*   **`PostRepository`:**
    *   Create a new method `findTrendingPosts` to find posts with the most views in the last 7 days.
*   **`PostService`:**
    *   Create a new method `getTrendingPosts` to call the new repository method.
*   **`PostController`:**
    *   Create a new endpoint `/api/v1/post/trending` to expose this data for chart generation.
