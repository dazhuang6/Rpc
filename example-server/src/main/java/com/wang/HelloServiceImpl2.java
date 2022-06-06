package com.wang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloServiceImpl2 {
    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl2.class);

    public String hello(Hello hello) {
        logger.info("HelloServiceImpl2收到: {}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        logger.info("HelloServiceImpl2返回: {}.", result);
        return result;
    }
}
