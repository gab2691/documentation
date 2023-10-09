package br.documentation.controller;

import br.documentation.service.JGitService;
import br.documentation.service.ScheduleDocsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class GitController {

    @Autowired
    private JGitService service;

    @Autowired
    private ScheduleDocsService scheduleDOCs;

    @GetMapping("/pullRemoteRepo")
    public ResponseEntity<?> updateRepo() throws IOException {
        /*   scheduleDOCs.updateDocdFromConfluence();*/
        return ResponseEntity.ok().build();
    }

}