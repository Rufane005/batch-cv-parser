package com.example.batchcvparser.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "candidates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private int yearsOfExperience;
    private String skills;
    private String preferredLocation;
    private String filePath;
}