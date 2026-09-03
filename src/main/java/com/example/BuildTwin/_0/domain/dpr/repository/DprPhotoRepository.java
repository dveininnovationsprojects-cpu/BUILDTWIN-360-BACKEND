package com.example.BuildTwin._0.domain.dpr.repository;

import com.example.BuildTwin._0.domain.dpr.model.DprPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DprPhotoRepository extends JpaRepository<DprPhoto, Long> {
    List<DprPhoto> findByDprHeaderId(Long dprHeaderId);
}
