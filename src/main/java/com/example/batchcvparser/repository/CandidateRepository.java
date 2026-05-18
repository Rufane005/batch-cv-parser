package com.example.batchcvparser.repository;

import com.example.batchcvparser.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    List<Candidate> findBySkillsContainingIgnoreCase(String skills);
    List<Candidate> findByYearsOfExperienceGreaterThanEqual(Integer minExperience);
    List<Candidate> findBySkillsContainingIgnoreCaseAndYearsOfExperienceGreaterThanEqual(String skills, Integer minExperience);
}