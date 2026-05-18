package com.example.batchcvparser.config.listener;

import com.example.batchcvparser.model.Candidate;
import org.springframework.batch.core.SkipListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

public class CvStepSkipListener implements SkipListener<Resource, Candidate> {

    private static final Logger log = LoggerFactory.getLogger(CvStepSkipListener.class);

    @Override
    public void onSkipInRead(Throwable t) {
        log.error("Fayl oxunarkən xəta baş verdi və keçildi: {}", t.getMessage());
    }

    @Override
    public void onSkipInWrite(Candidate item, Throwable t) {
        log.error("Namizəd bazaya yazılarkən xəta baş verdi: {} - Səbəb: {}", item.getFullName(), t.getMessage());
    }

    @Override
    public void onSkipInProcess(Resource item, Throwable t) {
        log.warn("⚠️ FAYL BURAXILDI (SKIPPED) -> Korlanmış və ya oxunmaz fayl: {}, Səbəb: {}",
                item.getFilename(), t.getMessage());
    }
}