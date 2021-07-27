package com.tqd.tool.jbake_core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import com.google.gson.GsonBuilder;
import com.tqd.flexmark.extension.ListAllExtension;
import com.tqd.flexmark.toc.Toc;
import com.tqd.flexmark.utils.FlexmarkExtensionUtils;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.toc.SimTocExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class OrderNoTest {
	final private static DataHolder OPTIONS = PegdownOptionsAdapter.flexmarkOptions(
            Extensions.ALL
    );
	
	public static void main(String[] args) throws IOException {
		
		MutableDataSet options = new MutableDataSet(OPTIONS);
		options.set(Parser.EXTENSIONS, FlexmarkExtensionUtils.getAllExtensionInClasspath());
		//SubscriptDelimiterProcessor
		//StrikethroughSubscriptDelimiterProcessor
		 //OPTIONS.get(Parser.EXTENSIONS).add(TocExtension.create());
		 //Parser.EXTENSIONS.get(options).addAll();
		 HtmlRenderer.RENDER_HEADER_ID.set((@NotNull MutableDataHolder) options, true);
		 String ss=new GsonBuilder().setPrettyPrinting().create().toJson(options);
		 System.out.println(ss);
		 Parser parser = Parser.builder(options).build();
     
     HtmlRenderer renderer = HtmlRenderer.builder(options).build();
     FileInputStream fin=new FileInputStream("test-data/start-mindmap.md");
     Document document=parser
    		 .parseReader(new InputStreamReader(fin, Charset.forName("utf-8")));
     String s1= renderer.render(document);
     System.out.println(s1);
     
     FileUtils.write(new File("test-data/no-test.html"), s1, Charset.forName("utf-8"),false);
 

	}

}
