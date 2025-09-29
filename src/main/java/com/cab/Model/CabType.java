package com.cab.Model;

public enum CabType {
    GO(12.0f),    // Base rate for Go
    XL(18.0f),    // Base rate for XL  
    BIKE(8.0f);   // Base rate for Bike
    
    private final float baseRate;
    
    CabType(float baseRate) {
        this.baseRate = baseRate;
    }
    
    public float getBaseRate() {
        return baseRate;
    }
}
