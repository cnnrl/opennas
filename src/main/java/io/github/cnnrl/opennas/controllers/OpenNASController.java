package io.github.cnnrl.opennas.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenNASController {

  @GetMapping("/")
  public String index() {
    return "200 now fuck off";
  }

}
