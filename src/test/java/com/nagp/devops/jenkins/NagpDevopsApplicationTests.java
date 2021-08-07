package com.nagp.devops.jenkins;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.nagp.devops.jenkins.controller.*;

@SpringBootTest
class NagpDevopsApplicationTests {

private static final Logger LOGGER = LoggerFactory.getLogger(NagpDevopsApplicationTests.class);
	
	@Autowired
	private DemoController controller;
	
	@Test
	void testGetServerStatus() throws Exception {
		String value = controller.getServerStatus();
		LOGGER.info("Server Status : " + value);
		assertThat(value).contains("Healthy");
		
			
	}
	
	
	@Test
	void testGetServerMessage() throws Exception {
		String value = controller.getServerMessage();
		LOGGER.info("Server said : " + value);
		assertThat(value).contains("Hello");
		
	}
	
	@Test
	void testGetBranch() throws Exception {
		String value = controller.getBranchName();
		LOGGER.info("Server said : " + value);
		assertThat(value).contains("Develop");
		
	}
	


}