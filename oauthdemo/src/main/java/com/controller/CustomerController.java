package com.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.constants.CustomerConstants;
import com.model.Customer;
import com.publisher.service.CustomerService;
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

	@PostMapping("/publishcustomer")
	public ResponseEntity<Response> postCustomer(@RequestBody final Customer customer,
			@RequestHeader("Authorization") String authorization, BindingResult bindingResult) {
		LOGGER.info("Customer Controller started......");
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
		LOGGER.info("Customer Controller end......");
		return new ResponseEntity<Response>(responseEntity.getBody(), HttpStatus.OK);
		
	}

	@PostMapping("/customer/details")
	public ResponseEntity<Response> publishToTopic(@RequestBody final Customer customer) {

		return customerService.publishToTopic(customer);

	}
}
