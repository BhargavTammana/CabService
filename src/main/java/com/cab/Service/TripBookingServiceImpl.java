package com.cab.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cab.Exception.CabException;
import com.cab.Exception.CurrentUserSessionException;
import com.cab.Exception.TripBookingException;
import com.cab.Model.Cab;
import com.cab.Model.CurrentUserSession;
import com.cab.Model.Customer;
import com.cab.Model.Driver;
import com.cab.Model.TripBooking;
import com.cab.Model.TripBookingDTO;
import com.cab.Repositary.CabRepo;
import com.cab.Repositary.CurrentUserSessionRepo;
import com.cab.Repositary.CustomerRepo;
import com.cab.Repositary.DriverRepo;
import com.cab.Repositary.TripBookingRepo;

@Service
public class TripBookingServiceImpl implements TripBookingService{

	
	@Autowired
	private TripBookingRepo tripBookingRepo;
	
	@Autowired
	private CustomerRepo customerRepo;
	
	@Autowired
	private CabRepo cabRepo;
	
	@Autowired
	private CurrentUserSessionRepo currRepo;
	
	@Autowired
	private DriverRepo driverRepo;
	
	@Autowired
	private PricingService pricingService;
	
	
	@Override
	public List<Cab> searchByLocation(String pickUpLocation, String uuid)
			throws TripBookingException, CurrentUserSessionException {
				
		Optional<CurrentUserSession> validUser = currRepo.findByUuid(uuid);
		if(validUser.isPresent()) {
			List<Cab> allCab = cabRepo.findAll();
		    List<Cab> availableCab = new ArrayList<>();
		    for(Cab cab : allCab) {
		    	if(cab.getCabCurrStatus().equalsIgnoreCase("Available") && cab.getCurrLocation().equalsIgnoreCase(pickUpLocation)) {
		    		availableCab.add(cab);
		    	}
		    }
		    if(availableCab.isEmpty()) {
		    	throw new TripBookingException("No Cab Available in this Location");
		    }
		    else {
		    	return availableCab;
		    }
		}
		else {
			throw new CurrentUserSessionException("User Not Login");
		}
	}


	@Override
	public TripBooking BookRequest(Integer cabId, TripBooking tripBooking, String uuid)
			throws TripBookingException ,CabException , CurrentUserSessionException{
		
		Optional<CurrentUserSession> validUser = currRepo.findByUuid(uuid);
		if(validUser.isPresent()) {
			CurrentUserSession currUser = validUser.get();
			Optional<Customer> cust = customerRepo.findById(currUser.getCurrUserId());
			Customer customer = cust.get();
			List<TripBooking> allTripByCustomer = customer.getTripBooking();
			if(isTripOverlap(tripBooking, allTripByCustomer)==true) {
				
				throw new TripBookingException("You have already booked an another Trip in the same Time");
			}
			else {
				Optional<Cab> addCab = cabRepo.findById(cabId);
				if(addCab.isPresent()) {
					System.out.println("Cab found: " + addCab.get());
					Cab newCab = addCab.get();
					if(newCab.getCabCurrStatus().equalsIgnoreCase("Available") &&
							newCab.getCurrLocation().equalsIgnoreCase(tripBooking.getPickupLocation())) {

						newCab.setCabCurrStatus("Pending");
						tripBooking.setCab(newCab);
						tripBooking.setCustomer(customer);

						tripBooking.setCurrStatus("Pending");

						
						// Save the trip booking first
						TripBooking savedTrip = tripBookingRepo.save(tripBooking);

						
						// Now update the customer's trip list and save
						allTripByCustomer.add(savedTrip);
						customerRepo.save(customer);
						
						return savedTrip;
						
					}
					else {
						throw new CabException("This Cab is not available currently for location or avability purpose");
					}
				}
				else {
					throw new CabException("No Cab Present with the given Credentials");
				}
			}
		}
		else {
			throw new CurrentUserSessionException("User is Not Login");
		}
	}
	
	

	public List<Driver> getAvailableDrivers(String pickUpLocation) {
	    return driverRepo.findByCurrLocationAndCurrDriverStatus(pickUpLocation, "available");
	}

	
	
	public boolean isTripOverlap(TripBooking newTripBooking, List<TripBooking> existingTrips) {
	    if (newTripBooking.getFromDateTime() == null || newTripBooking.getToDateTime() == null) {
	        return false; 
	    }
	    
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	    LocalDateTime newTripFromDT = LocalDateTime.parse(newTripBooking.getFromDateTime(), formatter);
	    LocalDateTime newTripToDT = LocalDateTime.parse(newTripBooking.getToDateTime(), formatter);
	    for (TripBooking existingTripBooking : existingTrips) {
	        if (existingTripBooking.getFromDateTime() == null || existingTripBooking.getToDateTime() == null) {
	            continue;
	        }
	        LocalDateTime existingTripFromDT = LocalDateTime.parse(existingTripBooking.getFromDateTime(), formatter);
	        LocalDateTime existingTripToDT = LocalDateTime.parse(existingTripBooking.getToDateTime(), formatter);
	        if (newTripFromDT.isBefore(existingTripToDT) && newTripToDT.isAfter(existingTripFromDT)) {
	            return true;
	        }
	    }    
	    return false;
	}



	@Override
	public TripBooking AssignDriverByAdmin(Integer TripBookingId, String uuid)
			throws TripBookingException, CabException, CurrentUserSessionException {
		Optional<CurrentUserSession> validUser = currRepo.findByUuidAndRole(uuid);
		if(validUser.isPresent()) {
			Optional<TripBooking> optionalTrip = tripBookingRepo.findById(TripBookingId);
			if(optionalTrip.isPresent()) {
				TripBooking trip = optionalTrip.get();
			    Customer customer = trip.getCustomer();
			    List<TripBooking> allTrips = customer.getTripBooking();
			    
			    List<Driver> allDrivers = driverRepo.findByCurrLocationAndCurrDriverStatus(trip.getPickupLocation(), "available");
			    
			    
			    if(allDrivers.isEmpty()) {
			        System.out.println("No exact match found, trying case-insensitive search...");
			        List<Driver> allAvailableDrivers = driverRepo.findAll();
			        List<Driver> matchingDrivers = new ArrayList<>();
			        
			        for(Driver driver : allAvailableDrivers) {
			            if(driver.getCurrLocation() != null && driver.getCurrDriverStatus() != null) {
			                boolean locationMatch = driver.getCurrLocation().trim().equalsIgnoreCase(trip.getPickupLocation().trim());
			                // Check for various forms of "available" status
			                String driverStatus = driver.getCurrDriverStatus().trim().toLowerCase();
			                boolean statusMatch = driverStatus.equals("available") || driverStatus.equals("AVAILABLE");
			                
			               
			                
			                if(locationMatch && statusMatch) {
			                    matchingDrivers.add(driver);
			                }
			            }
			        }
			        allDrivers = matchingDrivers;
			      
			    }

			    
			    if(allDrivers.isEmpty()) {
			    	
			    	trip.setCurrStatus("cancelled");
			    	for(TripBooking tb : allTrips) {
			    		if(tb.getTripBookingId()==trip.getTripBookingId()) {
			    			tb.setCurrStatus("cancelled");
			    		}
			    	}
			    	customer.setTripBooking(allTrips);
			    	 throw new TripBookingException("No driver is available for this trip.");
			    }
			    else {
			    	Driver assignDriver = allDrivers.get(0); 
			    	
			    	
				    // Update driver status
				    assignDriver.setCurrDriverStatus("Booked");
				    
				    // Clear any existing cab relationship for this driver first
				    if(assignDriver.getCab() != null) {
				        assignDriver.getCab().setDriver(null);
				        cabRepo.save(assignDriver.getCab());
				        assignDriver.setCab(null);
				    }
				    
				    // Get the trip's cab and clear its current driver if any
				    Cab tripCab = trip.getCab();
				    if(tripCab.getDriver() != null && !tripCab.getDriver().equals(assignDriver)) {
				        Driver oldDriver = tripCab.getDriver();
				        oldDriver.setCab(null);
				        oldDriver.setCurrDriverStatus("Available");
				        driverRepo.save(oldDriver);
				    }
				    
				    // Now establish the new relationship
				    assignDriver.setCab(tripCab);
				    tripCab.setDriver(assignDriver);
				    tripCab.setCabCurrStatus("Booked");
				    
				    // Update trip details
				    trip.setCurrStatus("confirmed");
				    trip.setDriver(assignDriver);
				    
				    // Add trip to driver's trip list
				    List<TripBooking> allTripByDrv = assignDriver.getTrips();
				    if(allTripByDrv == null) {
				        allTripByDrv = new ArrayList<>();
				        assignDriver.setTrips(allTripByDrv);
				    }
				    if(!allTripByDrv.contains(trip)) {
				        allTripByDrv.add(trip);
				    }

				    // Save in the correct order
				    cabRepo.save(tripCab);
				    driverRepo.save(assignDriver);
				    tripBookingRepo.save(trip);
				    
				    System.out.println("Successfully assigned driver " + assignDriver.getDriverId() + " to trip " + trip.getTripBookingId());
				    return trip;
			    }
			}
			else {
				throw new TripBookingException("No trip is booked with provided tripBookingId.");
			}
		}
		else {
			throw new CurrentUserSessionException("User is not logged in or is not an admin.");
		}
	}

	@Override
	public TripBookingDTO viewBookingById(Integer TripBookingId, String uuid)
			throws TripBookingException, CabException, CurrentUserSessionException {
		Optional<CurrentUserSession> validUser = currRepo.findByUuid(uuid);
		if(validUser.isPresent()) {
			Optional<TripBooking> tp = tripBookingRepo.findById(TripBookingId);
			if(tp.isPresent()) {
				TripBooking trip = tp.get();
				TripBookingDTO showTrip = new TripBookingDTO();
				showTrip.setTripBookingId(TripBookingId);
				showTrip.setPickupLocation(trip.getPickupLocation());
				showTrip.setFromDateTime(trip.getFromDateTime());
				showTrip.setDropLocation(trip.getDropLocation());
				showTrip.setToDateTime(trip.getToDateTime());
				showTrip.setDistanceInKm(trip.getDistanceInKm());
				showTrip.setDriverName(trip.getDriver().getUserName());
				showTrip.setLicenceNo(trip.getDriver().getLicenceNo());
				showTrip.setRating(trip.getDriver().getRating());
				showTrip.setCarType(trip.getCab().getCarType());
				showTrip.setCarName(trip.getCab().getCarName());
				showTrip.setCarNumber(trip.getCab().getCarNumber());
				showTrip.setPerKmRate(trip.getCab().getPerKmRate());
				showTrip.setFare(pricingService.calculateFare(trip, trip.getCab()));
				showTrip.setTripStatus(trip.getCurrStatus());
				return showTrip;
			}
			else {
				throw new TripBookingException("No trip is booked with provided tripBookingId.");
			}
		}
		else {
			throw new CurrentUserSessionException("User is not logged in");
		}
	    
	}


	@Override
	public String MarkTripAsCompleted(Integer TripBookingId, String uuid)
			throws TripBookingException, CurrentUserSessionException {
		Optional<CurrentUserSession> validUser = currRepo.findByUuid(uuid);
		if(validUser.isPresent()) {
			Optional<TripBooking> tp = tripBookingRepo.findById(TripBookingId);
			if(tp.isPresent()) {
				TripBooking trip = tp.get();
				trip.setCurrStatus("Completed");
				tripBookingRepo.save(trip);
				Customer cust = trip.getCustomer();
				List<TripBooking> allTrip = cust.getTripBooking();
				for(TripBooking tb : allTrip) {
					if(tb.getTripBookingId() == trip.getTripBookingId()) {
						tb.setCurrStatus("completed");
					}
				}
				customerRepo.save(cust);
				trip.getCab().setCabCurrStatus("AVAILABLE");
				cabRepo.save(trip.getCab());
				trip.getDriver().setCurrDriverStatus("Available");
				trip.getDriver().setCab(null);
			    trip.getCab().setDriver(null);
				driverRepo.save(trip.getDriver());
				return "Thank you your Trip has been Completed";
			}
			else {
				throw new TripBookingException("No trip is booked with provided tripBookingId.");
			}
		}
		else {
			throw new CurrentUserSessionException("User is not logged in");
		}
	}

	@Override
	public Float calculateTripFare(Integer cabId, TripBooking tripBooking, String uuid) 
			throws TripBookingException, CabException, CurrentUserSessionException {
		Optional<CurrentUserSession> validUser = currRepo.findByUuid(uuid);
		if(validUser.isPresent()) {
			Optional<Cab> cab = cabRepo.findById(cabId);
			if(cab.isPresent()) {
				Cab selectedCab = cab.get();
				// Validate cab type
				if(!isValidCabType(selectedCab.getCarType())) {
					throw new CabException("Invalid cab type. Supported types: GO, XL, BIKE");
				}
				return pricingService.calculateFare(tripBooking, selectedCab);
			} else {
				throw new CabException("No Cab Present with the given ID");
			}
		} else {
			throw new CurrentUserSessionException("User Not Login");
		}
	}
	
	private boolean isValidCabType(String carType) {
		try {
			com.cab.Model.CabType.valueOf(carType.toUpperCase());
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}


	

	

}




