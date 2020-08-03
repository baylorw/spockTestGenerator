package com.baylorw.spockTestGenerator.controller;

import com.baylorw.spockTestGenerator.service.TestGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
public class CodeToolsController
{
	private static Logger logger = LoggerFactory.getLogger(CodeToolsController.class);

	@Autowired
	TestGenerator testGenerator;


	@PostMapping("/tests/create")
	public ResponseEntity onGenerateTestsRequest(@RequestBody String codeToTest)
	{
		String generatedCode = testGenerator.createTests(codeToTest);

		return new ResponseEntity(generatedCode, HttpStatus.OK);
	}
}
