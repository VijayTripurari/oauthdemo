package com.controller;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.constants.CustomerConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.AuditLog;
import com.model.Customer;
import com.model.ErrorLog;
import com.publisher.service.CustomerService;
import com.repository.AuditLogRepository;
import com.repository.CustomerRepository;
import com.repository.ErrorLogRepository;
import com.response.Response;

@RestController
@RequestMapping("/")
public class CustomerController {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomerController.class);
	@Autowired
	CustomerService customerService;
	
	@Autowired
	Environment env;
	
	Customer customerFromTopic = null;
	
	@Autowired
	CustomerRepository customerRepository;
	
	@Autowired
	AuditLogRepository auditLogRepository;
	
	@Autowired
	ErrorLogRepository errorLogRepository;

	@PostMapping("/publishcustomer")
	public ResponseEntity<Response> postCustomer(@RequestBody @Valid final Customer customer,
			@RequestHeader("Authorization") String authorization, BindingResult bindingResult) {
		String url = env.getProperty("publisher.url") + authorization;
		RestTemplate restTemplate = new RestTemplate();
		Response response = new Response();
		HttpEntity<Customer> request = new HttpEntity<>(customer);
		ResponseEntity<Response> responseEntity;
		try {
			if (bindingResult.hasErrors()) {
				List<FieldError> fieldErrors = bindingResult.getFieldErrors();
				List<String> errorMessage = new ArrayList<>();
				for (FieldError e : fieldErrors) {
					errorMessage.add(" @ " + e.getField().toUpperCase() + " :  " + e.getDefaultMessage());
				}
				throw new InvalidRequestException(errorMessage.toString());
			}
			responseEntity = restTemplate.exchange(url, HttpMethod.POST, request, Response.class);
		} catch (HttpClientErrorException e) {
			response.setStatus(e.getStatusCode().toString());
			response.setMessage(CustomerConstants.UN_AUTH_REQ);
			response.setError_type(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			response.setStatus(CustomerConstants.ERROR_MSG);
			response.setMessage(CustomerConstants.INVALID_REQUEST);
			response.setError_type(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<Response>(responseEntity.getBody(), HttpStatus.OK);
	}

	@PostMapping("/customer/details")
	public ResponseEntity<Response> publishToTopic(@RequestBody final Customer customer) {

		return customerService.publishToTopic(customer);

	}
	
	@GetMapping("/subscribecustomer")
	public ResponseEntity<Response> subscribeToCustomer(@RequestHeader("Authorization") String authorization)
	{
		String url = env.getProperty("subscriber.url") + authorization;
		RestTemplate restTemplate = new RestTemplate();
		Response response = new Response();
		HttpHeaders requestHeaders = new HttpHeaders();
		HttpEntity<String> httpEntity = new HttpEntity<>(requestHeaders);
		ResponseEntity<Response> responseEntity;
		try {
		  responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Response.class);
		}
		 catch (HttpClientErrorException e) {
				response.setStatus(e.getStatusCode().toString());
				response.setMessage(CustomerConstants.UN_AUTH_REQ);
				response.setError_type(e.getMessage());
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			} catch (Exception e) {
				response.setStatus(CustomerConstants.ERROR_MSG);
				response.setMessage(CustomerConstants.INVALID_REQUEST);
				response.setError_type(e.getMessage());
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
		return new ResponseEntity<Response>(responseEntity.getBody(), HttpStatus.OK);
	}
	
	
	
	@GetMapping("/customer/subscribe")
	public ResponseEntity<Response> consumeCustomerJson() throws JsonProcessingException
	{
		long startTime = System.currentTimeMillis();
		LOGGER.info("Consumer Request Time : " + startTime);
		Response response = new Response();
		ObjectMapper mapper = new ObjectMapper();
		response.setStatus(CustomerConstants.SUCCESS_MSG);
		String customerString = mapper.writeValueAsString(customerFromTopic);
		response.setMessage(customerString);
		long elapsedTime = System.currentTimeMillis() - startTime;
		LOGGER.info("Consumer Response Time : " + elapsedTime);
		return new ResponseEntity<Response>(response, HttpStatus.OK);
		
	}
		
	@KafkaListener(topics=CustomerConstants.TOPIC_NAME,groupId=CustomerConstants.KAFKA_GRP_ID,containerFactory="kafkaListenerContainerFactory")
	public void getJsonCustomerFromTopic(@Payload String customer) 
	{
		ObjectMapper mapper = new ObjectMapper();
		Customer customerObj = null;

		try {
			customerObj = mapper.readValue(customer, Customer.class);
			
            customerFromTopic = customerObj;
			AuditLog auditLog = new AuditLog();
			auditLog.setCustomerNumber(customerObj.getCustomerNumber());
			auditLog.setPayload(customer);
			auditLogRepository.save(auditLog);
			
			String customerNumber = customerObj.getCustomerNumber();
			customerNumber = customerNumber.replaceFirst(".{4}$", "####");
			customerObj.setCustomerNumber(customerNumber);
			
			String birthDate = customerObj.getBirthDate();
			birthDate = birthDate.replaceFirst("^.{4}", "####");
			customerObj.setBirthDate(birthDate);
			
			String email = customerObj.getEmail();
			email = email.replaceFirst("^.{4}", "####");
			customerObj.setEmail(email);
			
			
			customerRepository.save(customerObj);
			
		} catch (Exception e) {

			ErrorLog errorLog = new ErrorLog();
			errorLog.setError_type("exception");
			errorLog.setError_description(e.getMessage().toString());
			errorLog.setPayload(customer);
			errorLogRepository.save(errorLog);
			
		} 

	}
}
