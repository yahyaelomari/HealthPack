package com.yahyaelomari.healthpack.clinical.repository;

import com.yahyaelomari.healthpack.clinical.domain.Condition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConditionRepository extends JpaRepository<Condition, UUID> {

    List<Condition> findByEncounterId(UUID encounterId);
}
