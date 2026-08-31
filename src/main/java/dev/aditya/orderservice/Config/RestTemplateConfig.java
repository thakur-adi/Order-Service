package dev.aditya.orderservice.Config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean(name = "LoadBalancedRestTemplate")
    @LoadBalanced
    public RestTemplate createLoadbalancedRestTemplate(){
        return new RestTemplate();
    }
}
