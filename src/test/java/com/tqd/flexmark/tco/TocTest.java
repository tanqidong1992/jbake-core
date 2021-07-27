package com.tqd.flexmark.tco;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.json.simple.JSONObject;

import com.google.gson.GsonBuilder;
import com.tqd.flexmark.toc.Toc;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class TocTest {

	public static void main(String[] args) throws IOException {
		MutableDataSet options = new MutableDataSet();
		 options.set(Parser.EXTENSIONS, Arrays.asList(
 	            TocExtension.create() 
 	    ));
      Parser parser = Parser.builder(options).build();
      HtmlRenderer renderer = HtmlRenderer.builder(options).build();
      FileInputStream fin=new FileInputStream(new File("test-data/toc-test.md"));
      Document document=parser.parseReader(new InputStreamReader(fin,Charset.forName("utf-8")));
 
      List<Heading> headings=new ArrayList<>();
      document.getChildIterator().forEachRemaining(n->{
     	  if(n instanceof Heading) {
     		  headings.add((Heading)n);
     	  }
       });
      
      Toc toc=new Toc();
      for(Heading h:headings) {
    	 String s= renderer.render(h);
    	 System.out.println(s);
          toc.addHeading(h); 
      }
      //toc.print();
      String s=new GsonBuilder().setPrettyPrinting().create().toJson(toc);
      System.out.println(s);
      
      
      
	}

}
