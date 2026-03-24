package com.Nguyen.blogplatform.repository;

import com.Nguyen.blogplatform.model.NotificationTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, String> {

    Optional<NotificationTemplate> findByCode(String code);

    Optional<NotificationTemplate> findByCodeAndIsActiveTrue(String code);

    List<NotificationTemplate> findByType(String type);

    List<NotificationTemplate> findByTypeAndIsActiveTrue(String type);

    Page<NotificationTemplate> findByIsActiveTrue(Pageable pageable);

    Page<NotificationTemplate> findByTypeAndIsActiveTrue(String type, Pageable pageable);

    boolean existsByCode(String code);

    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.isActive = true AND (nt.code LIKE %:search% OR nt.name LIKE %:search% OR nt.type LIKE %:search%)")
    Page<NotificationTemplate> searchActiveTemplates(@Param("search") String search, Pageable pageable);
}
