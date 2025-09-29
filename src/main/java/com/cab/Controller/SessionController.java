package com.cab.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cab.Repositary.CurrentUserSessionRepo;

@RestController
@RequestMapping("/session")
public class SessionController {

    @Autowired
    private CurrentUserSessionRepo sessionRepo;

    @DeleteMapping("/clearAll")
    public ResponseEntity<String> clearAllSessions() {
        sessionRepo.deleteAll();
        return new ResponseEntity<>("All sessions cleared successfully", HttpStatus.OK);
    }
}
