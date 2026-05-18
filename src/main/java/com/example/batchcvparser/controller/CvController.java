package com.example.batchcvparser.controller;

import com.example.batchcvparser.model.Candidate;
import com.example.batchcvparser.repository.CandidateRepository;
import com.example.batchcvparser.service.FileStorageService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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

        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Xəta: Zəhmət olmasa etibarlı bir fayl yükləyin!"
            ));
        }

        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        boolean isZip = (contentType != null && (contentType.equals("application/zip") || contentType.equals("application/x-zip-compressed")))
                || (fileName != null && fileName.toLowerCase().endsWith(".zip"));

        if (!isZip) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Xəta: Yalnız .zip formatında arxiv faylları qəbul edilir!"
            ));
        }

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
            // Server xətası üçün düzgün status kodu
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Batch başladılarkən daxili xəta baş verdi: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/status/{jobExecutionId}")
    public ResponseEntity<?> getJobStatus(@PathVariable Long jobExecutionId) {
        JobExecution jobExecution = jobExplorer.getJobExecution(jobExecutionId);
        if (jobExecution == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "Xəta: " + jobExecutionId + " ID-li hər hansı bir Batch işi tapılmadı!"
            ));
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

        List<Candidate> filteredCandidates;

        if (skills != null && minExperience != null) {
            filteredCandidates = candidateRepository.findBySkillsContainingIgnoreCaseAndYearsOfExperienceGreaterThanEqual(skills, minExperience);
        } else if (skills != null) {
            filteredCandidates = candidateRepository.findBySkillsContainingIgnoreCase(skills);
        } else if (minExperience != null) {
            filteredCandidates = candidateRepository.findByYearsOfExperienceGreaterThanEqual(minExperience);
        } else {
            filteredCandidates = candidateRepository.findAll();
        }

        return ResponseEntity.ok(filteredCandidates);
    }
}