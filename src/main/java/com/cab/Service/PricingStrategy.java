package com.cab.Service;

import com.cab.Model.Cab;
import com.cab.Model.TripBooking;

public interface PricingStrategy {
    float calculateFare(TripBooking trip, Cab cab);
    String getPricingType();
}
