package com.tqd.tool.jbake_core;

import checkers.units.quals.A;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.AttributablePart;
import com.vladsch.flexmark.util.html.Attributes;
import com.vladsch.flexmark.util.html.MutableAttribute;
import com.vladsch.flexmark.util.html.MutableAttributes;


public class HtmlWriterTest {

    public static void main(String[] args) {

        HtmlWriter hw=new HtmlWriter(1000,1);

        MutableAttributes attributes=new MutableAttributes();
        attributes.addValue("src","test");
        hw.withAttr(new AttributablePart("src"))
                .setAttributes(attributes).withAttr().tag("img")
                .tag("/img");
        System.out.println(hw.toString());
    }
}
