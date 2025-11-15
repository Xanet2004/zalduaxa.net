package net.zalduaxa.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.zalduaxa.backend.model.Project;
import net.zalduaxa.backend.model.ProjectRepository;

@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/api")
public class FrontendController {
    @Autowired
    ProjectRepository project_repository;

    @GetMapping(value = "/projects", produces = { "application/json", "application/xml" })
    public ResponseEntity<List<Project>> getProjectTypes(){
        List<Project> project_list = project_repository.findAll();

        if (project_list.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return new ResponseEntity<>(project_list, HttpStatus.OK);
        }
    }
}
