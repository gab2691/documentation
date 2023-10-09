package br.documentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FormController {

    @GetMapping("/form")
    public String getFormPage() {
        return "formsInfo";
    }
}
