package com.example.batchcvparser.controller;

import com.example.batchcvparser.model.Candidate;
import com.example.batchcvparser.repository.CandidateRepository;
import com.example.batchcvparser.service.FileStorageService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cv")
public class CvController {

    private final JobLauncher jobLauncher;
    private final Job importCvJob;
    private final FileStorageService fileStorageService;
    private final JobExplorer jobExplorer;
    private final CandidateRepository candidateRepository;

    public CvController(JobLauncher jobLauncher, Job importCvJob,
                        FileStorageService fileStorageService, JobExplorer jobExplorer,
                        CandidateRepository candidateRepository) {
        this.jobLauncher = jobLauncher;
        this.importCvJob = importCvJob;
        this.fileStorageService = fileStorageService;
        this.jobExplorer = jobExplorer;
        this.candidateRepository = candidateRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadZip(@RequestParam("file") MultipartFile file) {
        try {
            String uploadPath = fileStorageService.unzipFile(file);

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("fullPath", uploadPath)
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(importCvJob, jobParameters);

            return ResponseEntity.ok(Map.of(
                    "message", "Batch prosesi ugurla basladi!",
                    "jobExecutionId", execution.getId(),
                    "status", execution.getStatus().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/status/{jobExecutionId}")
    public ResponseEntity<?> getJobStatus(@PathVariable Long jobExecutionId) {
        JobExecution jobExecution = jobExplorer.getJobExecution(jobExecutionId);
        if (jobExecution == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
                "jobId", jobExecutionId,
                "status", jobExecution.getStatus().toString(),
                "startTime", jobExecution.getStartTime() != null ? jobExecution.getStartTime() : "Baslamayib",
                "endTime", jobExecution.getEndTime() != null ? jobExecution.getEndTime() : "Davam edir",
                "exitStatus", jobExecution.getExitStatus().getExitCode(),
                "steps", jobExecution.getStepExecutions().stream().map(step -> Map.of(
                        "stepName", step.getStepName(),
                        "readCount", step.getReadCount(),
                        "writeCount", step.getWriteCount(),
                        "skipCount", step.getReadSkipCount()
                )).toList()
        ));
    }

    // 3. YENİ: Export Candidates as Downloadable CSV File (Must-Have Feature)
    @GetMapping("/export")
    public void exportCandidatesToCSV(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=candidates.csv");

        response.getWriter().write('\uFEFF');

        List<Candidate> candidates = candidateRepository.findAll();
        response.getWriter().println("ID,Full Name,Preferred Location,Skills,Years of Experience");

        for (Candidate candidate : candidates) {
            response.getWriter().println(String.format("%d,\"%s\",\"%s\",\"%s\",%d",
                    candidate.getId(),
                    candidate.getFullName() != null ? candidate.getFullName() : "",
                    candidate.getPreferredLocation() != null ? candidate.getPreferredLocation() : "",
                    candidate.getSkills() != null ? candidate.getSkills() : "",
                    candidate.getYearsOfExperience()
            ));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Candidate>> searchCandidates(
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) Integer minExperience) {

        List<Candidate> allCandidates = candidateRepository.findAll();

        List<Candidate> filteredCandidates = allCandidates.stream()
                .filter(c -> skills == null || (c.getSkills() != null && c.getSkills().toLowerCase().contains(skills.toLowerCase())))
                .filter(c -> minExperience == null || c.getYearsOfExperience() >= minExperience)
                .collect(Collectors.toList());

        return ResponseEntity.ok(filteredCandidates);
    }
}