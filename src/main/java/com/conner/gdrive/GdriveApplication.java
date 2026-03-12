package com.conner.gdrive;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GdriveApplication {

  public static void main(String[] args) {
    SpringApplication.run(GdriveApplication.class, args);
  }

  @Bean
  public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
    return args -> {
      System.out.println("Let's start");

      String[] names = ctx.getBeanDefinitionNames();

      Arrays.sort(names);
      for (String name : names) {
        System.out.println(name);
      }
    };
  }

}
