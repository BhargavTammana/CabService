package com.cab.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

import com.cab.Model.Cab;
import com.cab.Model.CabType;
import com.cab.Model.TripBooking;

@Component
public class CabServicePricingStrategy implements PricingStrategy {
    
    private static final float NIGHT_SURCHARGE_PERCENTAGE = 0.25f; // 25% surcharge
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public float calculateFare(TripBooking trip, Cab cab) {
        // Get base rate based on cab type
        float baseRate = getBaseRateForCabType(cab.getCarType());
        
        // Calculate base fare (rate * distance)
        float baseFare = baseRate * trip.getDistanceInKm();
        
        // Check if night surcharge applies
        boolean isNightTime = isNightTime(trip.getFromDateTime());
        
        if (isNightTime) {
            float nightSurcharge = baseFare * NIGHT_SURCHARGE_PERCENTAGE;
            return baseFare + nightSurcharge;
        }
        
        return baseFare;
    }
    
    @Override
    public String getPricingType() {
        return "CAB_SERVICE_PRICING";
    }
    
    private float getBaseRateForCabType(String carType) {
        try {
            CabType cabType = CabType.valueOf(carType.toUpperCase());
            return cabType.getBaseRate();
        } catch (IllegalArgumentException e) {
            // Default to GO rate if invalid type
            return CabType.GO.getBaseRate();
        }
    }
    
    private boolean isNightTime(String dateTimeString) {
        try {
            // Parse the date-time string
            LocalDateTime tripDateTime = LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);
            int hour = tripDateTime.getHour();
            
            // Night time is between 12 AM (0) to 6 AM (6) - exclusive
            return hour >= 0 && hour < 6;
        } catch (Exception e) {
            // If parsing fails, assume it's not night time
            return false;
        }
    }
}
