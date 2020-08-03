package com.baylorw.spockTestGenerator.controller;

import com.baylorw.spockTestGenerator.util.TimeUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;

@RestController
public class DefaultController
{
	@GetMapping("/")
	String onBaseUrl()
	{
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		String swaggerUrl = request.getRequestURL().toString() + "swagger-ui.html";
		return "<p>baylor's sandbox, where new ideas are tested.</p>" +
				"<p>current time: " + TimeUtil.format(ZonedDateTime.now(), "h:mm a z") +
				" on " + TimeUtil.format(ZonedDateTime.now(), "E M-dd-yyyy") + "</p>" +
				"<p>Swagger: <a href='" + swaggerUrl + "'>" + swaggerUrl + "</a></p>";
	}
}
