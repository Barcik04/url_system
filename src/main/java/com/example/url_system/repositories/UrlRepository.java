package com.example.url_system.repositories;


import com.example.url_system.dtos.StatsUrlDto;
import com.example.url_system.models.Url;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByCode(String code);
    boolean existsByCode(String code);
    Page<Url> findAllByUser_Id(Long id, Pageable pageable);
    Optional<Url> findByCodeAndUser_Id(String code,  Long userId);

}
