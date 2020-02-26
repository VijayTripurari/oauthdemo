package com.oauthdemo.oauthdemo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.text.MaskFormatter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories("com.*")
@EntityScan("com.*")
@SpringBootApplication(scanBasePackages="com.*")
public class OauthdemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(OauthdemoApplication.class, args);
		
		long startTime = System.currentTimeMillis();
		long elapsedTime = System.currentTimeMillis() - startTime;
		
		Date date1 = new Date(startTime);
		
		DateFormat  df = new SimpleDateFormat("dd-MM-yyyy");
		String formattedDate = df.format(date1);
		
		System.out.println(" Todays date is :  "+ formattedDate);
		
		 MaskFormatter mf;
		
			String customerNumber = "C000000689";
			String s = "C000000689";
					s = s.replaceFirst(".{4}$", "####");
			String e = "abcd@gmail.com";
			e = e.replaceFirst("^.{4}", "####");
			
			    System.out.println(" customerNumber : " + s);
			    System.out.println(" customeremail : " + e);
			    
		
		   
		
	}

}
