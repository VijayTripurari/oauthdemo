package com.oauthdemo.oauthdemo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.constants.CustomerConstants;
import com.enums.CustomerStatus;
import com.model.Address;
import com.model.Customer;
import com.publisher.service.CustomerService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OauthdemoApplicationTests {
	
	@Autowired
	CustomerService customerService;

	@Test
	public void testPublisCustomerToTopic() {
		
		Customer customer = new Customer();
		Address address = new Address("HNO 56","PLAZA RESINDENCY","IICT ROAD NO 2","500032");
		customer.setAddress(address);
		customer.setBirthDate("19-11-1999");
		customer.setCountry("India");
		customer.setCountryCode("IN");
		customer.setCustomerNumber("C0000018");
		customer.setEmail("test@gmail.com");
		customer.setFirstName("samplefirstname");
		customer.setLastName("samplelastname");
		customer.setMobileNumber("9876543212");
		customer.setCustomerStatus(CustomerStatus.Open.toString());
		assertEquals(CustomerConstants.SUCCESS_MSG, customerService.publishToTopic(customer).getBody().getStatus());
		
		
	}

}
