package br.documentation.controller;


import br.documentation.dto.ConfigDTO;
import br.documentation.service.DocumentationService;
import br.documentation.service.HttpService;
import br.documentation.service.JGitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@CrossOrigin
@RestController
public class DocumentationController {

    @Autowired
    private HttpService httpService;

    @Autowired
    private JGitService jGitService;

    @Autowired
    private DocumentationService documentationService;

    @GetMapping("/documentation")
    public ResponseEntity<String> getDocumentation() {
        documentationService.updateDocdFromConfluence();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/infoUser")
    public ResponseEntity<String> processingUserInfo(@RequestBody ConfigDTO configDTO){
        return ResponseEntity.ok().build();
    }
}
