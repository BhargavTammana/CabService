package com.cab.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cab.Model.Cab;
import com.cab.Model.TripBooking;

@Service
public class PricingService {
    
    private final Map<String, PricingStrategy> pricingStrategies;
    
    @Autowired
    public PricingService(List<PricingStrategy> strategies) {
        this.pricingStrategies = strategies.stream()
            .collect(Collectors.toMap(
                PricingStrategy::getPricingType, 
                Function.identity()
            ));
    }
    
    public float calculateFare(TripBooking trip, Cab cab) {
        return calculateFare(trip, cab, "CAB_SERVICE_PRICING");
    }
    
    public float calculateFare(TripBooking trip, Cab cab, String pricingType) {
        PricingStrategy strategy = pricingStrategies.get(pricingType);
        if (strategy == null) {
            // Fallback to default pricing strategy
            strategy = pricingStrategies.get("CAB_SERVICE_PRICING");
        }
        return strategy.calculateFare(trip, cab);
    }
    
    public String getPricingBreakdown(TripBooking trip, Cab cab) {
        CabServicePricingStrategy strategy = (CabServicePricingStrategy) pricingStrategies.get("CAB_SERVICE_PRICING");
        
        float baseFare = getBaseRateForCabType(cab.getCarType()) * trip.getDistanceInKm();
        float totalFare = strategy.calculateFare(trip, cab);
        
        StringBuilder breakdown = new StringBuilder();
        breakdown.append("Cab Type: ").append(cab.getCarType()).append("\n");
        breakdown.append("Distance: ").append(trip.getDistanceInKm()).append(" km\n");
        breakdown.append("Base Rate: ₹").append(getBaseRateForCabType(cab.getCarType())).append("/km\n");
        breakdown.append("Base Fare: ₹").append(baseFare).append("\n");
        
        if (totalFare > baseFare) {
            breakdown.append("Night Surcharge (25%): ₹").append(totalFare - baseFare).append("\n");
        }
        
        breakdown.append("Total Fare: ₹").append(totalFare);
        
        return breakdown.toString();
    }
    
    private float getBaseRateForCabType(String carType) {
        try {
            return com.cab.Model.CabType.valueOf(carType.toUpperCase()).getBaseRate();
        } catch (IllegalArgumentException e) {
            return com.cab.Model.CabType.GO.getBaseRate();
        }
    }
}
