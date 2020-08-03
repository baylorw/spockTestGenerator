package com.baylorw.spockTestGenerator.model;

import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.MethodCallExpr;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ClassDescriptor
{
	private String name;
	private ClassOrInterfaceDeclaration classDeclaration;
	private String packageName;
	private PackageDeclaration packageDeclaration;

	//--- The raw JavaParser field declarations. Includes lines that have multiple properties defined on them.
	private List<FieldDeclaration> fieldDeclarations = new ArrayList<>();

	private List<VariableDeclarator> properties = new ArrayList<>();
	private List<VariableDeclarator> autowiredProperties = new ArrayList<>();
	private List<VariableDeclarator> constants = new ArrayList<>();
	private List<VariableDeclarator> nonAutowiredProperties = new ArrayList<>();

	private List<ConstructorDeclaration> constructors = new ArrayList<>();
	private List<MethodDeclaration> methods = new ArrayList<>();


	public String getClassName()
	{
		//--- class.name makes more sense than class.className but some people will look for className.
		return getName();
	}

	public List<String> getMethodNames()
	{
		List<String> methodNames = new ArrayList<>();
		for (MethodDeclaration method : getMethods())
		{
			methodNames.add(method.getNameAsString());
		}
		return methodNames;
	}

	public String getScopedMethodName(MethodCallExpr method)
	{
		String methodName = method.getNameAsString() + "()";
		if (method.getScope().isPresent())
		{
			methodName = method.getScope().get() + "." + methodName;
		}
		return methodName;
	}
}
