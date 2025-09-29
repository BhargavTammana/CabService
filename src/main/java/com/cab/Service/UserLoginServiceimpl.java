package com.cab.Service;

import java.util.Optional;

// import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cab.Exception.AdminException;
import com.cab.Exception.CurrentUserSessionException;
import com.cab.Exception.CustomerException;
import com.cab.Model.Admin;
import com.cab.Model.CurrentUserSession;
import com.cab.Model.Customer;
import com.cab.Model.UserLoginDTO;
import com.cab.Repositary.AdminRepo;
import com.cab.Repositary.CurrentUserSessionRepo;
import com.cab.Repositary.CustomerRepo;

@Service
public class UserLoginServiceimpl implements UserLoginService{

	@Autowired
	private CustomerRepo customerRepo;
	
	@Autowired
	private AdminRepo adminRepo;
	
	@Autowired
	private CurrentUserSessionRepo currRepo;

	@Override
	public CurrentUserSession login(UserLoginDTO dto) throws CustomerException, AdminException {
		
			Optional<Admin> findAdmin = adminRepo.findByEmail(dto.getEmail());
			Optional<Customer> findCustomer = customerRepo.findByEmail(dto.getEmail());
			
			if(findAdmin.isPresent() && findCustomer.isEmpty()) {
				Admin currAdmin = findAdmin.get();
				Optional<CurrentUserSession> validAdminSession = currRepo.findByCurrUserId(currAdmin.getAdminId());
				if(validAdminSession.isPresent()) {
					// Clear the existing session and create a new one instead of throwing an error
					currRepo.delete(validAdminSession.get());
				}
				
				if(currAdmin.getPassword().equals(dto.getPassword())) {
					// String key = RandomStringUtils.randomAlphanumeric(6);
					String key = "ABCD123";
					CurrentUserSession curr = new CurrentUserSession();
					curr.setUuid(key);
					curr.setCurrRole("Admin");
					curr.setCurrStatus("Login Successfull");
					curr.setCurrUserId(currAdmin.getAdminId());
					return currRepo.save(curr);
				}
				else {
					throw new AdminException("Please Enter the correct Password");
				}
			}
			else if(findAdmin.isEmpty() && findCustomer.isPresent()){
					Customer currCustomer = findCustomer.get();
					Optional<CurrentUserSession> validCustomerSession = currRepo.findByCurrUserId(currCustomer.getCustomerId());
					if(validCustomerSession.isPresent()) {
						// Clear the existing session and create a new one instead of throwing an error
						currRepo.delete(validCustomerSession.get());
					}
					
					if(currCustomer.getPassword().equals(dto.getPassword())) {
						// String key = RandomStringUtils.randomAlphanumeric(6);
						String key = "ABCD123";
						CurrentUserSession curr = new CurrentUserSession();
						curr.setUuid(key);
						curr.setCurrRole("Customer");
						curr.setCurrStatus("Login Successfull");
						curr.setCurrUserId(currCustomer.getCustomerId());
						return currRepo.save(curr);
					}
					else {
						throw new CustomerException("Please Enter the correct Password");
					}
				}
			else {
				throw new CustomerException("User is Not Registered");
			}
		}

	@Override
	public String LogOut(String uuid) throws CurrentUserSessionException {
		// TODO Auto-generated method stub
		Optional<CurrentUserSession> validAdminOrCustomer = currRepo.findByUuid(uuid);
		if(validAdminOrCustomer.isPresent()) {
			
			currRepo.delete(validAdminOrCustomer.get());
			return "User Logged Out Successfully";
			
		}
		else {
			throw new CurrentUserSessionException("User Not Logged In with this Credentials");
		}
	}

	
	
	

	
}
