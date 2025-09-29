package com.cab.Controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cab.Model.CabType;

@RestController
@RequestMapping("/pricing")
public class PricingController {

    @GetMapping("/cabTypes")
    public ResponseEntity<List<String>> getSupportedCabTypes() {
        List<String> cabTypes = Arrays.stream(CabType.values())
                .map(CabType::name)
                .collect(Collectors.toList());
        return new ResponseEntity<>(cabTypes, HttpStatus.OK);
    }

    @GetMapping("/rates")
    public ResponseEntity<String> getCabRates() {
        StringBuilder rates = new StringBuilder();
        rates.append("Cab Service Pricing Structure:\n\n");
        
        for (CabType cabType : CabType.values()) {
            rates.append(cabType.name())
                 .append(": ₹")
                 .append(cabType.getBaseRate())
                 .append("/km\n");
        }
        
        rates.append("\nNight Surcharge: 25% extra between 12:00 AM to 6:00 AM");
        
        return new ResponseEntity<>(rates.toString(), HttpStatus.OK);
    }
}
