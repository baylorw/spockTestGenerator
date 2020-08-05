package com.baylorw.spockTestGenerator.service;

import com.baylorw.spockTestGenerator.model.ClassDescriptor;
import com.baylorw.spockTestGenerator.util.StringUtil;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TestGenerator
{
	private static Logger logger = LoggerFactory.getLogger(TestGenerator.class);


	private Configuration templateEngine;

	@Value("${test-generator.template-directory}")
	private String testGeneratorTemplateDirectory;

	@Value("${test-generator.test-template}")
	private String templateName;


	@PostConstruct
	public void initialization()
	{
		initializeTemplateEngine();
	}

	public String createTests(String codeToTest)
	{
		Map<String, Object> model = createCodeModel(codeToTest);
		String generatedCode = fillInTemplate(model);
		return generatedCode;
	}

	private Map<String, Object> createCodeModel(String codeToTest)
	{
		Map<String, Object> model = new HashMap<>();

		ClassDescriptor codeDescription = ClassDescriptorFactory.toClassDescriptor(codeToTest);

		model.put("packageName", codeDescription.getPackageName());
		model.put("className", codeDescription.getName());
		model.put("classNameCamel", StringUtil.pascalToCamelCase(codeDescription.getName()));

		List<Object> autowires = new ArrayList<>();
		model.put("autowires", autowires);
		for (VariableDeclarator autowiredField : codeDescription.getAutowiredProperties())
		{
			Map<String, Object> autowiredProperties = new HashMap<>();
			autowires.add(autowiredProperties);
			autowiredProperties.put("type", autowiredField.getType().toString());
			autowiredProperties.put("name", autowiredField.getName().toString());
		}

		List<Object> methods = new ArrayList<>();
		model.put("methods", methods);
		for (MethodDeclaration method : codeDescription.getMethods())
		{
			Map<String, Object> methodModel = new HashMap<>();
			methods.add(methodModel);
			if (method.getJavadocComment().isPresent())
			{
				methodModel.put("comment", method.getJavadocComment().toString());
			} else
			{
				methodModel.put("comment", null);
			}

			methodModel.put("signature", method.getDeclarationAsString());
			methodModel.put("name", method.getName().toString());
			methodModel.put("returnType", method.getTypeAsString());

			List<Object> parameters = new ArrayList<>();
			methodModel.put("parameters", parameters);
			for (Parameter parameter : method.getParameters())
			{
				Map<String, Object> parameterModel = new HashMap<>();
				parameters.add(parameterModel);
				parameterModel.put("type", parameter.getType());
				parameterModel.put("name", parameter.getName());
			}
		}

		return model;
	}

	private String fillInTemplate(Map<String, Object> model)
	{
		try
		{
			Template template = templateEngine.getTemplate(templateName);
			StringWriter stringWriter = new StringWriter();
			template.process(model, stringWriter);
			String generatedCode = stringWriter.toString();
			return generatedCode;
		} catch (Exception e)
		{
			logger.error("Error filling in template. error={}", e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Initialize the template engine.
	 * <p>
	 * This must run *AFTER* this object has been created. Don't call this from the constructor,
	 * the autowires won't be ready.
	 */
	private void initializeTemplateEngine()
	{
		try
		{
			templateEngine = new Configuration(Configuration.VERSION_2_3_30);
			File templatesDirectory = new File(testGeneratorTemplateDirectory);
			templateEngine.setDirectoryForTemplateLoading(templatesDirectory);
		} catch (IOException e)
		{
			String templateDirectoryFQN = System.getProperty("user.dir") + "/" + testGeneratorTemplateDirectory;
			logger.error("Problem initialing the templating engine. templateDirectory={}, error={}",
					templateDirectoryFQN, e.getMessage(), e);
		}
	}
}
