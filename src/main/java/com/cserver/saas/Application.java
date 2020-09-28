package com.cserver.saas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    private final static Logger LOGGER = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args){
		SpringApplication.run(Application.class, args);
        LOGGER.info("支付项目启动");
	}
}