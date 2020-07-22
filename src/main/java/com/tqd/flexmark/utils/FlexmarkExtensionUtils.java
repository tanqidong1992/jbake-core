package com.tqd.flexmark.utils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.vladsch.flexmark.util.misc.Extension;

public class FlexmarkExtensionUtils {
    private static final Logger logger=LoggerFactory.getLogger(FlexmarkExtensionUtils.class);
	public static List<String> ignoreExtensionNames=Arrays.asList(
			"com.vladsch.flexmark.youtrack.converter.YouTrackConverterExtension",
			"com.vladsch.flexmark.jira.converter.JiraConverterExtension");
			
	public static List<Extension> getAllExtensionInClasspath(){
		ClassPath cp=null;
		try {
			cp = ClassPath.from(FlexmarkExtensionUtils.class.getClassLoader());
		} catch (IOException e) {
			logger.error("",e);
		}
		if(cp==null) {
			return Collections.emptyList();
		}
		List<Extension> extensions=cp.getAllClasses().stream()
        .filter(c->c.getName().endsWith("Extension"))
        .filter(c->!ignoreExtensionNames.contains(c.getName()))
        .filter(c->isValidExtension(c))
        .map(c->createExtension(c))
        .collect(Collectors.toList());
		Map<String,Extension> map=new HashMap<>();
		extensions.forEach(e->{
			map.put(e.getClass().getPackage().getName(), e);
		});
		return new ArrayList<>(map.values());
	}
	public static Extension createExtension(ClassInfo ci) {
		String className=ci.getName();
		Class<?> clazz=null;
		try {
			clazz = Class.forName(className);
		} catch (Throwable e) {
			logger.error("",e);
		}
		Method m=null;
		try {
			m = clazz.getDeclaredMethod("create");
		} catch (NoSuchMethodException | SecurityException e) {
		}
		Object obj=null;
		try {
			obj=m.invoke(clazz);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("",e);
		}
		return (Extension)obj;
	}
	
	public static boolean isValidExtension(ClassInfo ci) {
		String className=ci.getName();
		Class<?> clazz=null;
		try {
			clazz = Class.forName(className);
		} catch (Throwable e) {
		}
		if(clazz==null) {
			return false;
		}
		Method m=null;
		try {
			m = clazz.getDeclaredMethod("create");
		} catch (NoSuchMethodException | SecurityException e) {
		}
		if(m==null) {
			return false;
		}
		if(( m.getModifiers() & Modifier.PUBLIC )==0) {
			return false;
		}
		Class<?> retType=m.getReturnType();
		if(Extension.class.isAssignableFrom(retType)) {
			return true;
		}else {
			return false;
		}
	}

}
