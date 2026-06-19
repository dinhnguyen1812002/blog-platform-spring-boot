package com.Nguyen.blogplatform.domain.analytics.dto;



import lombok.Data;

@Data
public class BlogAnalyticsResponse {
   private Integer totalUser;
   private Integer totalPost;
   private Integer totalTag;
   private Integer totalNewsletterSubcriber;
}
