package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.DprPhotoAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DprPhotoAttachmentRepository extends JpaRepository<DprPhotoAttachment, Long> {

    List<DprPhotoAttachment> findByDprId(Long dprId);
}
