package com.innowise.gateway;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class Test {


    @Autowired
    DiscoveryClient discoveryClient;


    @RequestMapping(value = "/test")
    void someShit() {
        List<ServiceInstance> instances = discoveryClient.getInstances("file-api");
        System.out.println();
    }
}
