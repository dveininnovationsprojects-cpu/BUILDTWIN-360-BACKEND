package com.example.BuildTwin._0.domain.labour.repository;

import com.example.BuildTwin._0.domain.labour.enums.TradeCategory;
import com.example.BuildTwin._0.domain.labour.model.Contractor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractorRepository extends JpaRepository<Contractor, Long> {

    Optional<Contractor> findByContractorCode(String contractorCode);

    boolean existsByContractorCode(String contractorCode);

    List<Contractor> findByStatus(String status);

    List<Contractor> findByTradeSpecializationAndStatus(TradeCategory tradeSpecialization, String status);

    List<Contractor> findByContractorType(com.example.BuildTwin._0.domain.labour.enums.ContractorType contractorType);

    List<Contractor> findByParentContractorId(Long parentContractorId);
}
