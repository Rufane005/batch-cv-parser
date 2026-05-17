package com.example.batchcvparser.config;

import com.example.batchcvparser.model.Candidate;
import com.example.batchcvparser.repository.CandidateRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
@Configuration
public class BatchConfig {

    @Bean
    public Job importCvJob(JobRepository jobRepository, Step step1) {
        return new JobBuilder("importCvJob", jobRepository)
                .start(step1)
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      CandidateRepository repository) {
        return new StepBuilder("cv-parse-step", jobRepository)
                .<Resource, Candidate>chunk(10, transactionManager)
                .reader(multiResourceItemReader(null))
                .processor(cvItemProcessor())
                .writer(chunk -> repository.saveAll(chunk.getItems()))
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(10)
                .build();
    }
    @Bean
    public CvItemProcessor cvItemProcessor() {
        return new CvItemProcessor();
    }

    @Bean
    @StepScope
    public MultiResourceItemReader<Resource> multiResourceItemReader(
            @Value("#{jobParameters['fullPath']}") String fullPath) {

        MultiResourceItemReader<Resource> reader = new MultiResourceItemReader<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        try {
            Resource[] resources = resolver.getResources("file:" + fullPath + "/*.{pdf,docx}");
            reader.setResources(resources);
        } catch (IOException e) {
            reader.setResources(new Resource[0]);
        }

        reader.setDelegate(new ResourcePassThroughReader());
        return reader;
    }

    public static class ResourcePassThroughReader implements org.springframework.batch.item.file.ResourceAwareItemReaderItemStream<Resource> {
        private Resource resource;

        @Override
        public Resource read() {
            Resource temp = resource;
            resource = null;
            return temp;
        }

        @Override
        public void setResource(Resource resource) {
            this.resource = resource;
        }

        @Override public void open(org.springframework.batch.item.ExecutionContext executionContext) {}
        @Override public void update(org.springframework.batch.item.ExecutionContext executionContext) {}
        @Override public void close() {}
    }
}