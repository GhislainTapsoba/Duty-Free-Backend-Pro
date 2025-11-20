package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.Settings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettingsRepository extends JpaRepository<Settings, Long> {

    Optional<Settings> findByKey(String key);

    List<Settings> findByCategory(String category);

    boolean existsByKey(String key);
}
