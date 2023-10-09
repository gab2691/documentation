package br.documentation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduleDocsService {

    @Autowired
    private DocumentationService documentationService;

    @Scheduled(cron = "0 0 9 * * *")
    public void updateDocsAt9AM() {
        documentationService.updateDocdFromConfluence();
    }

    @Scheduled(cron = "0 30 16 * * *")
    public void updateDocsAt4PM() {
        documentationService.updateDocdFromConfluence();
    }
}