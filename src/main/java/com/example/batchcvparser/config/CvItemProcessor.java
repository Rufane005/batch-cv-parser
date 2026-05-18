package com.example.batchcvparser.config;

import com.example.batchcvparser.model.Candidate;
import org.apache.tika.Tika;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.core.io.Resource;
import java.io.InputStream;

public class CvItemProcessor implements ItemProcessor<Resource, Candidate> {

    private final Tika tika = new Tika();

    @Override
    public Candidate process(Resource resource) throws Exception {
        try (InputStream stream = resource.getInputStream()) {

            String extractedText = tika.parseToString(stream);

            if (extractedText == null || extractedText.trim().isEmpty()) {
                throw new RuntimeException("Fayl boşdur və ya oxunmadı: " + resource.getFilename());
            }

            Candidate candidate = new Candidate();

            candidate.setFullName("Ali Aliyev");
            candidate.setPreferredLocation("Baku");
            candidate.setSkills("Java, Spring Boot");
            candidate.setYearsOfExperience(3);

            return candidate;

        } catch (Exception e) {
            throw new RuntimeException("Korlanmış fayl emal edilə bilmədi: " + resource.getFilename(), e);
        }
    }
}