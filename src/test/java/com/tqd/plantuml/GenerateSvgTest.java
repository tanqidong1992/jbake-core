package com.tqd.plantuml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import com.credibledoc.plantuml.exception.PlantumlRuntimeException;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;

public class GenerateSvgTest {

	public static void main(String[] args) throws IOException {
		 String s=FileUtils.readFileToString(new File("test-data/start-mindmap.puml"),
				 Charset.forName("utf-8"));
        String svg=test(s);
        System.out.println(s);
        System.out.println(svg);
	}
	
	public static String test(String plantUml) {
		try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            SourceStringReader reader = new SourceStringReader(plantUml);
            FileFormatOption fileFormatOption = new FileFormatOption(FileFormat.SVG);
            DiagramDescription diagramDescription = reader.outputImage(os, fileFormatOption);
            // The XML is stored into svg
            String svg = new String(os.toByteArray(), StandardCharsets.UTF_8);
            return svg;
        } catch (Exception e) {
            throw new PlantumlRuntimeException("PlantUML: " + plantUml, e);
        }
	}
	
	

}
