package com.example.BuildTwin._0.domain.labour.service;

import com.example.BuildTwin._0.domain.labour.enums.TradeCategory;
import com.example.BuildTwin._0.domain.labour.model.Contractor;
import com.example.BuildTwin._0.domain.labour.repository.ContractorRepository;
import com.example.BuildTwin._0.domain.labour.dto.ContractorRequestDto;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractorService {

    private final ContractorRepository contractorRepository;

    @Transactional
    public Contractor createContractor(ContractorRequestDto requestDto) {
        if (contractorRepository.existsByContractorCode(requestDto.getContractorCode())) {
            throw new DuplicateResourceException("Contractor already exists with code: " + requestDto.getContractorCode());
        }

        Contractor contractor = Contractor.builder()
                .contractorCode(requestDto.getContractorCode())
                .name(requestDto.getName())
                .companyName(requestDto.getCompanyName())
                .tradeSpecialization(requestDto.getTradeSpecialization())
                .contactNumber(requestDto.getContactNumber())
                .email(requestDto.getEmail())
                .address(requestDto.getAddress())
                .status(requestDto.getStatus() != null ? requestDto.getStatus() : "ACTIVE")
                .build();

        return contractorRepository.save(contractor);
    }

    @Transactional(readOnly = true)
    public Contractor getContractorById(Long id) {
        return contractorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contractor", "id", id));
    }

    @Transactional(readOnly = true)
    public Contractor getContractorByCode(String code) {
        return contractorRepository.findByContractorCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Contractor", "contractorCode", code));
    }

    @Transactional(readOnly = true)
    public List<Contractor> getAllContractors() {
        return contractorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Contractor> getContractorsByTrade(TradeCategory trade) {
        return contractorRepository.findByTradeSpecializationAndStatus(trade, "ACTIVE");
    }

    @Transactional
    public Contractor updateContractor(Long id, ContractorRequestDto requestDto) {
        Contractor existing = getContractorById(id);
        existing.setName(requestDto.getName());
        existing.setCompanyName(requestDto.getCompanyName());
        existing.setTradeSpecialization(requestDto.getTradeSpecialization());
        existing.setContactNumber(requestDto.getContactNumber());
        existing.setEmail(requestDto.getEmail());
        existing.setAddress(requestDto.getAddress());
        if (requestDto.getStatus() != null) {
            existing.setStatus(requestDto.getStatus());
        }

        return contractorRepository.save(existing);
    }
}
