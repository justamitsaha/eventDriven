package com.saha.amit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.saha.amit")
public class SpringMessagingApplication {

    private static final Log log = LogFactory.getLog(SpringMessagingApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringMessagingApplication.class, args);
        log.info("Swagger URL http://localhost:8080/swagger-ui/index.html#/");
    }
}