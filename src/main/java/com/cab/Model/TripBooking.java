package com.cab.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class TripBooking {

	
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer tripBookingId;
	
	@Column(name = "pickup_location")
	private String pickupLocation;
	
	@Column(name = "from_date_time")
	private String fromDateTime;
	
	@Column(name = "drop_location")
	private String dropLocation;
	
	@Column(name = "to_date_time")
	private String toDateTime;
	
	@Column(name = "distance_in_km")
	private float distanceInKm;
	@JsonIgnore
	private String currStatus;
	
	@ManyToOne(cascade = CascadeType.REMOVE)
	@JsonIgnore
	private Driver driver;
	
	@ManyToOne(cascade = CascadeType.REMOVE)
	@JsonIgnore
	private Customer customer;
	
	@ManyToOne(cascade = CascadeType.REMOVE)
	@JsonIgnore
	private Cab cab;

}
