package com.RevPasswordManager.controller;

import com.RevPasswordManager.dto.GeneratorRequest;
import com.RevPasswordManager.security.GeneratorService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/generator")
public class GeneratorController {

    private final GeneratorService generatorService;

    public GeneratorController(GeneratorService generatorService) {
        this.generatorService = generatorService;
    }

    @PostMapping
    public String generate(@RequestBody GeneratorRequest request) {

        return generatorService.generate(
                request.getLength(),
                request.isIncludeUppercase(),
                request.isIncludeLowercase(),
                request.isIncludeNumbers(),
                request.isIncludeSpecial()
        );
    }
}
