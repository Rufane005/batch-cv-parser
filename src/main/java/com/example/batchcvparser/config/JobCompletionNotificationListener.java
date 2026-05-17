package com.example.batchcvparser.config;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    private final JavaMailSender mailSender;

    public JobCompletionNotificationListener(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            System.out.println("!!! BATCH JOB BİTDİ! İndi HR-a email göndərilir...");

            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo("hr_team@example.com");
                message.setSubject("Toplu CV Oxuma Prosesi Tamamlandı");
                message.setText("Hörmətli HR komandası,\n\n" +
                        "Sistemə yüklədiyiniz toplu CV (.zip) faylının emalı uğurla başa çatdı.\n" +
                        "Uğurla oxunan namizədləri idarəetmə panelindən və ya Excel export API-sindən yükləyə bilərsiniz.");

                mailSender.send(message);
                System.out.println("Email uğurla göndərildi!");
            } catch (Exception e) {
                System.out.println("Email göndərilərkən xəta baş verdi (SMTP sazlanmayıb): " + e.getMessage());
            }
        }
    }
}
