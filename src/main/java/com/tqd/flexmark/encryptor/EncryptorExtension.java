package com.tqd.flexmark.encryptor;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.tqd.flexmark.PlantUMLCodeNodeRenderer;
import com.tqd.flexmark.encryptor.internal.EncryptorDelimiterProcessor;
import com.tqd.flexmark.encryptor.internal.ToBeEncrypteNode;
import com.tqd.flexmark.encryptor.internal.ToBeEncrypteNodeRender;
import com.vladsch.flexmark.ast.Emphasis;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.internal.StrikethroughDelimiterProcessor;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlRendererOptions;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.HtmlRenderer.Builder;
import com.vladsch.flexmark.html.renderer.CoreNodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.core.ParagraphParser;
import com.vladsch.flexmark.util.data.MutableDataHolder;

import de.neuland.jade4j.parser.node.CodeNode;

public class EncryptorExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension{

	
    public static EncryptorExtension create() {
        return new EncryptorExtension();
    }
     
	@Override
	public void rendererOptions(@NotNull MutableDataHolder options) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void extend(@NotNull Builder htmlRendererBuilder, @NotNull String rendererType) {
		 htmlRendererBuilder.nodeRendererFactory(new ToBeEncrypteNodeRender.Factory());
		
	}

	@Override
	public void parserOptions(MutableDataHolder options) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void extend(com.vladsch.flexmark.parser.Parser.Builder parserBuilder) {
		 parserBuilder.customDelimiterProcessor(new EncryptorDelimiterProcessor());
		
	}

}
