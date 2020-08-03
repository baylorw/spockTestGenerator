package com.baylorw.spockTestGenerator.service;

import com.baylorw.spockTestGenerator.model.ClassDescriptor;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.AnnotationExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class ClassDescriptorFactory
{
    private static Logger logger = LoggerFactory.getLogger(ClassDescriptorFactory.class);

    public static ClassDescriptor toClassDescriptor(String javaClassCode)
    {
        CompilationUnit parsedCode = null;
        try {
            parsedCode = StaticJavaParser.parse(javaClassCode);
        } catch (Exception e) {
            logger.error("Error parsing Java class text. errror={}", e.getMessage(), e);
        }
        ClassDescriptor classDescriptor = new ClassDescriptor();

        //--- Class definition.
        List<ClassOrInterfaceDeclaration> classDefinitions = parsedCode.findAll(ClassOrInterfaceDeclaration.class);
        if (1 != classDefinitions.size())
        {
            logger.error("No class definition found in the supplied text.");
            return null;
        }
        classDescriptor.setName(classDefinitions.get(0).getName().toString());
        Optional<PackageDeclaration> packageWrapper = parsedCode.getPackageDeclaration();
        if (packageWrapper.isPresent())
        {
            classDescriptor.setPackageName(packageWrapper.get().getName().toString());
        }

        //--- Properties (constants, variables, etc.).
        List<FieldDeclaration> properties = parsedCode.findAll(FieldDeclaration.class);
        for (FieldDeclaration property : properties)
        {
            classDescriptor.getFieldDeclarations().add(property);
            for (VariableDeclarator variable : property.getVariables())
            {
                classDescriptor.getProperties().add(variable);
                if (isAConstant(property))
                {
                    classDescriptor.getConstants().add(variable);
                }
                else if (isAutowired(property))
                {
                    classDescriptor.getAutowiredProperties().add(variable);
                }
                else
                {
                    classDescriptor.getNonAutowiredProperties().add(variable);
                }
            }
        }

        //--- Constructors.
        List<ConstructorDeclaration> constructors = parsedCode.findAll(ConstructorDeclaration.class);
        for (ConstructorDeclaration constructor : constructors)
        {
            classDescriptor.getConstructors().add(constructor);
        }

        //--- Methods.
        List<MethodDeclaration> methods = parsedCode.findAll(MethodDeclaration.class);
        for (MethodDeclaration method : methods)
        {
            classDescriptor.getMethods().add(method);
        }

        return classDescriptor;
    }

    private static boolean hasAnnotation(FieldDeclaration property, String annotationName) {
        for (AnnotationExpr annotation : property.getAnnotations())
        {
            String currentAnnotationName = annotation.getNameAsString();
            if (annotationName.equals(currentAnnotationName))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean hasModifier(FieldDeclaration property, String modifierName) {
        for (Modifier modifier : property.getModifiers())
        {
            //--- For some reasons the modifiers have trailing spaces on them. :(
            String currentModifierName = modifier.toString().trim();
            if (modifierName.equals(currentModifierName))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean isAutowired(FieldDeclaration property) {
        return hasAnnotation(property, "Autowired");
    }

    private static boolean isAConstant(FieldDeclaration property) {
        return hasModifier(property, "final");
    }
}
