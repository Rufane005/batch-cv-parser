package com.example.batchcvparser.config;

import com.example.batchcvparser.model.Candidate;
import org.apache.tika.Tika;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CvItemProcessor implements ItemProcessor<Resource, Candidate> {

    private final Tika tika = new Tika();

    @Override
    public Candidate process(Resource resource) throws Exception {
        String rawText;
        try (InputStream is = resource.getInputStream()) {
            rawText = tika.parseToString(is);
        }

        Candidate candidate = new Candidate();

        candidate.setFullName(resource.getFilename());

        extractDetails(rawText, candidate);

        return candidate;
    }

    private void extractDetails(String text, Candidate candidate) {
        if (text == null || text.isEmpty()) return;

        String[] lines = text.split("\\r?\\n");

        Pattern namePattern = Pattern.compile("(?i)full\\s*name\\s*:\\s*(.*)");
        Pattern locationPattern = Pattern.compile("(?i)preferred\\s*location\\s*:\\s*(.*)");
        Pattern skillsPattern = Pattern.compile("(?i)skills\\s*:\\s*(.*)");
        Pattern expPattern = Pattern.compile("(?i)years\\s*of\\s*experience\\s*:\\s*(\\d+)");

        for (String line : lines) {
            String trimmedLine = line.trim();

            Matcher nameMatcher = namePattern.matcher(trimmedLine);
            if (nameMatcher.find()) {
                candidate.setFullName(nameMatcher.group(1).trim());
                continue;
            }

            Matcher locMatcher = locationPattern.matcher(trimmedLine);
            if (locMatcher.find()) {
                candidate.setPreferredLocation(locMatcher.group(1).trim());
            }

            Matcher skillsMatcher = skillsPattern.matcher(trimmedLine);
            if (skillsMatcher.find()) {
                candidate.setSkills(skillsMatcher.group(1).trim());
            }

            Matcher expMatcher = expPattern.matcher(trimmedLine);
            if (expMatcher.find()) {
                try {
                    candidate.setYearsOfExperience(Integer.parseInt(expMatcher.group(1).trim()));
                } catch (NumberFormatException e) {
                    candidate.setYearsOfExperience(0);
                }
            }
        }
    }
}