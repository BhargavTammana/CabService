# Test script for Trip Booking API

# Test data
$jsonBody = @{
    pickupLocation = "Bangalore"
    fromDateTime = "2025-09-29 15:00:00"
    dropLocation = "Chennai"  
    toDateTime = "2025-09-29 18:00:00"
    distanceInKm = 350.5
} | ConvertTo-Json

Write-Host "JSON Body being sent:"
Write-Host $jsonBody

# Test BookRequest
Write-Host "`nTesting /tripBooking/BookRequest..."
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/tripBooking/BookRequest?cabId=1&uuid=test-uuid" `
                                 -Method POST `
                                 -Body $jsonBody `
                                 -ContentType "application/json"
    Write-Host "Success: $response"
} catch {
    Write-Host "Error: $($_.Exception.Message)"
    Write-Host "Response: $($_.Exception.Response)"
}

# Test calculateFare
Write-Host "`nTesting /tripBooking/calculateFare..."
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/tripBooking/calculateFare?cabId=1&uuid=test-uuid" `
                                 -Method POST `
                                 -Body $jsonBody `
                                 -ContentType "application/json"
    Write-Host "Success: $response"
} catch {
    Write-Host "Error: $($_.Exception.Message)"
}
