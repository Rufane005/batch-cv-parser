package com.example.batchcvparser.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "candidates", indexes = {
        @Index(name = "idx_candidate_skills", columnList = "skills"),
        @Index(name = "idx_candidate_experience", columnList = "yearsOfExperience")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "years_of_experience")
    private int yearsOfExperience;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(name = "preferred_location")
    private String preferredLocation;

    @Column(name = "file_path")
    private String filePath;
}