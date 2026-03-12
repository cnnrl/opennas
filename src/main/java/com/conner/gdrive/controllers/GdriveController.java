package com.conner.gdrive.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GdriveController {

  @GetMapping("/")
  public String index() {
    return "200 now fuck off";
  }

}
