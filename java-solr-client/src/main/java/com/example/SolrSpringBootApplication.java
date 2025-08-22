package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SolrSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(SolrSpringBootApplication.class, args);
    }

    @Bean
    public CommandLineRunner solrClientRunner(SolrService solrService) {
        return args -> {
            solrService.runSolrOperations();
        };
    }
}
