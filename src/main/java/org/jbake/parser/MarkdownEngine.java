package org.jbake.parser;

import com.tqd.flexmark.toc.Toc;
import com.tqd.flexmark.utils.FlexmarkExtensionUtils;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.PegdownExtensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders documents in the Markdown format.
 *
 * @author Cédric Champeau
 */
public class MarkdownEngine extends MarkupEngine {

    private static final Logger logger = LoggerFactory.getLogger(MarkdownEngine.class);

    @Override
    public void processBody(final ParserContext context) {
        List<String> mdExts = context.getConfig().getMarkdownExtensions();

        int extensions = PegdownExtensions.NONE;

        for (String ext : mdExts) {
            if (ext.startsWith("-")) {
                ext = ext.substring(1);
                extensions = removeExtension(extensions, extensionFor(ext));
            } else {
                if (ext.startsWith("+")) {
                    ext = ext.substring(1);
                }
                extensions = addExtension(extensions, extensionFor(ext));
            }
        }

        //DataHolder options = PegdownOptionsAdapter.flexmarkOptions(extensions);

        DataHolder options1 = PegdownOptionsAdapter.flexmarkOptions(extensions);
        MutableDataSet options=new MutableDataSet(options1);
        Parser.EXTENSIONS.get(options).addAll(FlexmarkExtensionUtils.getAllExtensionInClasspath());
        HtmlRenderer.RENDER_HEADER_ID.set( options, true);

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        Document document = parser.parse(context.getBody());
        context.setBody(renderer.render(document));
        parseToc(document,context);
    }

    private void parseToc(Document document,final ParserContext context) {

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
        context.setToc(toc);

    }

    private int extensionFor(String name) {
        int extension = PegdownExtensions.NONE;

        try {
            Field extField = PegdownExtensions.class.getDeclaredField(name);
            extension = extField.getInt(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.debug("Undeclared extension field '{}', fallback to NONE", name);
        }
        return extension;
    }

    private int addExtension(int previousExtensions, int additionalExtension) {
        return previousExtensions | additionalExtension;
    }

    private int removeExtension(int previousExtensions, int unwantedExtension) {
        return previousExtensions & (~unwantedExtension);
    }

}
