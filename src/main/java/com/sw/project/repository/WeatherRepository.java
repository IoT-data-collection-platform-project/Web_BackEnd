package com.sw.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sw.project.entity.WeatherData;

import java.util.List;

public interface WeatherRepository extends JpaRepository<WeatherData, Long> {

    boolean existsByTm(String tm);

    List<WeatherData> findAllByOrderByTmDesc();
}