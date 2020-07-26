package com.tqd.flexmark;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.credibledoc.plantuml.svggenerator.SvgGeneratorService;
import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.misc.CharPredicate;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import com.vladsch.flexmark.html.renderer.CoreNodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;

public class PlantUMLCodeNodeRenderer implements NodeRenderer {
    final private boolean codeContentBlock;

    public PlantUMLCodeNodeRenderer(DataHolder options) {
        codeContentBlock = Parser.FENCED_CODE_CONTENT_BLOCK.get(options);
    }

    @Override
    public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        Set<NodeRenderingHandler<?>> set = new HashSet<>();
        set.add(new NodeRenderingHandler<>(FencedCodeBlock.class, this::render));
        return set;
    }

    void render(FencedCodeBlock node, NodeRendererContext context, HtmlWriter html) {

        if (node.getInfo().toString().equals("plantuml")) {
            html.line();
            html.tag("div");
            @NotNull
            List<BasedSequence> lines = node.getContentLines(1, node.getLineCount() - 1);
            String plantUml = lines.stream().map(BasedSequence::toString).reduce((s1, s2) -> {
                return s1.toString() + "\r\n" + s2.toString();
            }).get().toString();
            String svg = SvgGeneratorService.getInstance().generateSvgFromPlantUml(plantUml);
            html.append(svg);
            html.tag("/div");

        } else {
            render1(node, context, html);
        }

    }

    void render1(FencedCodeBlock node, NodeRendererContext context, HtmlWriter html) {
        html.line();
        html.srcPosWithTrailingEOL(node.getChars()).withAttr().tag("pre").openPre();
        BasedSequence info = node.getInfo();
        if (info.isNotNull() && !info.isBlank()) {
            BasedSequence language = node.getInfoDelimitedByAny(CharPredicate.SPACE_TAB);
            html.attr("class", context.getHtmlOptions().languageClassPrefix + language.unescape());
        } else {
            String noLanguageClass = context.getHtmlOptions().noLanguageClass.trim();
            if (!noLanguageClass.isEmpty()) {
                html.attr("class", noLanguageClass);
            }
        }
        html.srcPosWithEOL(node.getContentChars()).withAttr(CoreNodeRenderer.CODE_CONTENT).tag("code");
        if (codeContentBlock) {
            context.renderChildren(node);
        } else {
            html.text(node.getContentChars().normalizeEOL());
        }
        html.tag("/code");
        html.tag("/pre").closePre();
        html.lineIf(context.getHtmlOptions().htmlBlockCloseTagEol);
    }

    public static class Factory implements NodeRendererFactory {
        @NotNull
        @Override
        public NodeRenderer apply(@NotNull DataHolder options) {
            return new PlantUMLCodeNodeRenderer(options);
        }
    }
}
