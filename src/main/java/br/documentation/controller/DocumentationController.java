package br.documentation.controller;


import br.documentation.dto.ConfigDTO;
import br.documentation.dto.DocumentRequestDTO;
import br.documentation.dto.SidenavDTO;
import br.documentation.service.DocumentationService;
import br.documentation.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin
@RestController
public class DocumentationController {

    private final DocumentationService documentationService;
    private final FileService fileService;

    @Autowired
    public DocumentationController(DocumentationService documentationService, FileService fileService){
        this.documentationService = documentationService;
        this.fileService = fileService;
    }

    @PutMapping("/documentation")
    public ResponseEntity<String> getDocumentation() {
        documentationService.updateDocdFromConfluence();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/createNewDoc")
    public ResponseEntity<String> creatingDoc(@RequestBody DocumentRequestDTO documentRequestDTO){
        log.info("Creating document from URL: {}", documentRequestDTO.getUrl());
        if (documentRequestDTO == null || documentRequestDTO.getUrl() == null || documentRequestDTO.getUrl().trim().isEmpty()
                || documentRequestDTO.getFolder() == null || documentRequestDTO.getFolder().trim().isEmpty()){

            log.warn("URL {} or FOLDER {} must not be null or empty", documentRequestDTO.getUrl(), documentRequestDTO.getFolder());
            throw new IllegalArgumentException("URL or FOLDER must not be null or empty");
        }
        documentationService.savingEspecificDOC(documentRequestDTO.getUrl(), documentRequestDTO.getFolder());
        log.info("Document created successfully from URL: {} in folder {}", documentRequestDTO.getUrl(), documentRequestDTO.getFolder());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/infoUser")
    public ResponseEntity<String> processingUserInfo(@RequestBody ConfigDTO configDTO){
        return ResponseEntity.ok().build();
    }

    @GetMapping("treeView")
    public ResponseEntity<SidenavDTO> getTree(){
        return ResponseEntity.ok().body(fileService.transformTreeViewFileToDTO());
    }

}
