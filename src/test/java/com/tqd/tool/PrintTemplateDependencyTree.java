package com.tqd.tool;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrintTemplateDependencyTree {
    static String rootPath;
    public static void main(String[] args) {
        File root=new File("jbake-ui/templates");
        rootPath=FilenameUtils.normalize(root.getAbsolutePath());
        System.out.println(rootPath);
        List<TemplateInfo> s=Stream.of(root.listFiles())
                .filter(f->f.getName().endsWith(".ftl"))
                .map(PrintTemplateDependencyTree::readTemplate)
                        .collect(Collectors.toList());
        System.out.println("----Template Dependency Tree----");
        s.stream().sorted(Comparator.comparing(t->t.file.getName())).forEach(c->printTree(c,0));
        System.out.println("--------------------------------");
        Collection<File> allFiles=FileUtils.listFiles(root,new String[]{"ftl"},true);
        System.out.println("----Useless Template Files----");
        allFiles.stream()
                .filter(f->!files.contains(f.getAbsolutePath()))
                .forEach(System.err::println);
        System.out.println("------------------------------");
    }
    static Set<String> files=new HashSet<>();
     static void printTree(TemplateInfo t,int level){
        String path=FilenameUtils.normalize(t.file.getAbsolutePath());
         files.add(path);
        StringBuilder prefix=new StringBuilder();
        for(int i=0;i<level;i++){
            prefix.append(" ");
        }
        String relativePath=path.replaceAll(rootPath,"");
        System.out.println(prefix.toString()+""+relativePath);
        if(t.dependencies!=null){
            t.dependencies.stream().sorted(Comparator.comparing(c->c.file.getName())).forEach(c->printTree(c,level+4));
        }
    }

    public static TemplateInfo readTemplate(File file){
        TemplateInfo templateInfo=new TemplateInfo();
        templateInfo.file=file;
        String s= null;
        try {
            s = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> includes=new ArrayList<>();
        Matcher matcher=pattern.matcher(s);
        while (matcher.find()){
            //System.out.println(matcher.start()+","+ matcher.end());
            //System.out.println();
            includes.add(s.substring(matcher.start(),matcher.end()));
        }
        if(includes.isEmpty()){
            if(pattern1.matcher(s).find()){
                System.err.println(file.getAbsolutePath());
            }
        }
        File root=file.getParentFile();
        List<TemplateInfo> dependencies=includes.stream()
                .map(include->{
                    String flename=include.split("\"")[1];
                    File df=new File(root,flename);
                    return readTemplate(df);
                }).collect(Collectors.toList());
        templateInfo.content=s;
        templateInfo.dependencies=dependencies;
        return templateInfo;

    }
    static Pattern pattern=Pattern.compile("<#include \".*\\.ftl\">");
    static Pattern pattern1=Pattern.compile("<#include '.*\\.ftl'>");
    static class TemplateInfo{
        File file;
        String content;
        List<TemplateInfo> dependencies;
    }
}



