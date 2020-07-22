package com.tqd.flexmark.extension;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.tqd.flexmark.utils.FlexmarkExtensionUtils;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.misc.Extension;

public class ListAllExtension {

	public static void main(String[] args) throws IOException {
		ClassPath cp=ClassPath.from(ListAllExtension.class.getClassLoader());
        cp.getAllClasses().stream()
        .filter(c->c.getName().endsWith("Extension"))
        .filter(c->!FlexmarkExtensionUtils.ignoreExtensionNames.contains(c.getName()))
        .filter(c->FlexmarkExtensionUtils.isValidExtension(c))
        .forEach(c->{
        	System.out.println(c);
        });
	}
	
}
