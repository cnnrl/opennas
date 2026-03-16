package io.github.cnnrl.opennas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OpenNASApplication {

  public static void main(String[] args) {
    SpringApplication.run(OpenNASApplication.class, args);
  }

}
