package com.publisher.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.constants.CustomerConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.Customer;
import com.response.Response;

@Service
public class CustomerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);
	
	@Autowired
	KafkaTemplate<String, Customer> kafkaTemplate;

	public ResponseEntity<Response> publishToTopic(Customer customer) {
		Response response = new Response();
		long startTime = System.currentTimeMillis();
		ObjectMapper mapper = new ObjectMapper();
		LOGGER.info("Publisher Request Time : " + startTime);
		try {
			LOGGER.info("Customer object is received...", customer);
			kafkaTemplate.send(CustomerConstants.TOPIC_NAME, customer);
			response.setStatus(CustomerConstants.SUCCESS_MSG);
			String customerString = mapper.writeValueAsString(customer);
			response.setMessage(customerString);

		} catch (Exception e) {
			response.setStatus(CustomerConstants.ERROR_MSG);
			response.setMessage(CustomerConstants.INVALID_REQUEST);
			response.setError_type(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		long elapsedTime = System.currentTimeMillis() - startTime;
		LOGGER.info("Publisher Response Time : " + elapsedTime);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}
}
