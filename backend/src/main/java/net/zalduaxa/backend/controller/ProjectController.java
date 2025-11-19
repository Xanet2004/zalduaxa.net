package net.zalduaxa.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.zalduaxa.backend.model.projectType.ProjectType;
import net.zalduaxa.backend.model.projectType.ProjectTypeRepository;

@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/project")
public class ProjectController {
    @Autowired
    ProjectTypeRepository projectTypeRepo;

    @GetMapping(value = "/projectTypes", produces = { "application/json", "application/xml" })
    public ResponseEntity<List<ProjectType>> getProjectTypes(){
        List<ProjectType> projectTypes = projectTypeRepo.findAll();

        if (projectTypes.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return new ResponseEntity<>(projectTypes, HttpStatus.OK);
        }
    }
}
