package com.nagp.devops.jenkins.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

	@GetMapping("/")
	public String getServerStatus() {
		return "{status : Healthy}";
	}
	
	@GetMapping("/hello")
	public String getServerMessage() {
		return "Hello From Master Branch";
	}
	
	@GetMapping("/branch")
	public String getBranchName() {
		return "Master";
	}
}
