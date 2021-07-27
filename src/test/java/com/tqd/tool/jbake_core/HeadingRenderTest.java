package com.tqd.tool.jbake_core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.tqd.flexmark.toc.Toc;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.toc.SimTocExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class HeadingRenderTest {

	public static void main(String[] args) {
		
		MutableDataSet options = new MutableDataSet();
		 options.set(Parser.EXTENSIONS, Arrays.asList(
				 TocExtension.create(),SimTocExtension.create(),AnchorLinkExtension.create()
	    ));
     Parser parser = Parser.builder(options).build();
     HtmlRenderer renderer = HtmlRenderer.builder(options).build();
     
     Document document=parser.parse("[TOC]\n"
     		+ "# h111\n"
     		+ "## h2\n"
     		+ "### h3\n"
     		+ "");

     List<Heading> headings=new ArrayList<>();
     document.getChildIterator().forEachRemaining(n->{
    	  if(n instanceof Heading) {
    		  headings.add((Heading)n);
    	  }
      });
     
     Toc toc=new Toc();
     for(Heading h:headings) {
   	
         toc.addHeading(h); 
     }
     String s1= renderer.render(document);
   	 System.out.println(s1);
     //toc.print();
     String s=new GsonBuilder().setPrettyPrinting().create().toJson(toc);
     //System.out.println(s);

	}

}
