package com.innowise.gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class ActiveServices {

    final DiscoveryClient discoveryClient;

    public String authenticationApiServiceUri() {
            ServiceInstance authApi = discoveryClient.getInstances("auth-api").get(0);
        return authApi.getUri().toString();
    }
}
