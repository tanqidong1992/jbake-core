package com.tqd.plantuml;

import net.sourceforge.plantuml.*;
import net.sourceforge.plantuml.api.ImageDataSimple;
import net.sourceforge.plantuml.core.DiagramDescription;
import net.sourceforge.plantuml.core.ImageData;
import net.sourceforge.plantuml.core.UmlSource;
import net.sourceforge.plantuml.ditaa.PSystemDitaa;
import net.sourceforge.plantuml.security.ImageIO;
import net.sourceforge.plantuml.svek.GraphvizCrash;
import org.apache.batik.svggen.SVGGraphics2D;
import org.stathissideris.ascii2image.core.RenderingOptions;
import org.stathissideris.ascii2image.graphics.Diagram;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DitaaSVGPSystem extends AbstractPSystem {

    // private ProcessingOptions processingOptions;
    private Object processingOptions;
    private final boolean dropShadows;
    private final String data;
    private final float scale;
    private final boolean performSeparationOfCommonEdges;

    public DitaaSVGPSystem(UmlSource source, String data, boolean performSeparationOfCommonEdges, boolean dropShadows, float scale) {
        super(source);
        this.data = data;
        this.dropShadows = dropShadows;
        this.performSeparationOfCommonEdges = performSeparationOfCommonEdges;
        try {
            this.processingOptions = Class.forName("org.stathissideris.ascii2image.core.ProcessingOptions")
                    .newInstance();
            // this.processingOptions.setPerformSeparationOfCommonEdges(performSeparationOfCommonEdges);
            this.processingOptions.getClass().getMethod("setPerformSeparationOfCommonEdges", boolean.class)
                    .invoke(this.processingOptions, performSeparationOfCommonEdges);
        } catch (Exception e) {
            e.printStackTrace();
            this.processingOptions = null;
        }
        this.scale = scale;
    }

    DitaaSVGPSystem add(String line) {
        return new DitaaSVGPSystem(getSource(), data + line + BackSlash.NEWLINE, performSeparationOfCommonEdges, dropShadows, scale);
    }

    public DiagramDescription getDescription() {
        return new DiagramDescription("(Ditaa)");
    }

    @Override
    final protected ImageData exportDiagramNow(OutputStream os, int num, FileFormatOption fileFormat)
            throws IOException {
        if (fileFormat.getFileFormat() == FileFormat.ATXT) {
            os.write(getSource().getPlainString().getBytes());
            return ImageDataSimple.ok();
        }

        // ditaa can only export png so file format is mostly ignored
        try {
            // ditaa0_9.jar
            // final ConversionOptions options = new ConversionOptions();
            final Object options = Class.forName("org.stathissideris.ascii2image.core.ConversionOptions").newInstance();

            // final RenderingOptions renderingOptions = options.renderingOptions;
            final Field f_renderingOptions = options.getClass().getField("renderingOptions");
            final Object renderingOptions = f_renderingOptions.get(options);

            // renderingOptions.setScale(scale);
            final Method setScale = renderingOptions.getClass().getMethod("setScale", float.class);
            setScale.invoke(renderingOptions, scale);

            // options.setDropShadows(dropShadows);
            final Method setDropShadows = options.getClass().getMethod("setDropShadows", boolean.class);
            setDropShadows.invoke(options, dropShadows);

            // final TextGrid grid = new TextGrid();
            final Object grid = Class.forName("org.stathissideris.ascii2image.text.TextGrid").newInstance();

            // grid.initialiseWithText(data, null);
            final Method initialiseWithText = grid.getClass().getMethod("initialiseWithText", String.class,
                    Class.forName("org.stathissideris.ascii2image.core.ProcessingOptions"));
            initialiseWithText.invoke(grid, data, null);

            // final Diagram diagram = new Diagram(grid, options, processingOptions);
            final Class<?> clDiagram = Class.forName("org.stathissideris.ascii2image.graphics.Diagram");
            clDiagram.getConstructor(grid.getClass(), options.getClass(), processingOptions.getClass())
                    .newInstance(grid, options, processingOptions);
            final Object diagram = clDiagram
                    .getConstructor(grid.getClass(), options.getClass(), processingOptions.getClass())
                    .newInstance(grid, options, processingOptions);

            // final BitmapRenderer bitmapRenderer = new BitmapRenderer();
            final Object bitmapRenderer = Class.forName("org.stathissideris.ascii2image.graphics.BitmapRenderer")
                    .newInstance();

            // final BufferedImage image = (BufferedImage)
            // bitmapRenderer.renderToImage(diagram, renderingOptions);

            if(FileFormat.PNG.equals(fileFormat.getFileFormat())){
                final Method renderToImage = bitmapRenderer.getClass().getMethod("renderToImage", diagram.getClass(),
                        renderingOptions.getClass());
                final BufferedImage image = (BufferedImage) renderToImage.invoke(bitmapRenderer, diagram, renderingOptions);
                ImageIO.write(image, "png", os);
                final int width = image.getWidth();
                final int height = image.getHeight();
                return new ImageDataSimple(width, height);
            }else if(FileFormat.SVG.equals(fileFormat.getFileFormat())){
                SVGBitmapRender  svgBitmapRender=new SVGBitmapRender();
                SVGGraphics2D g2=svgBitmapRender.render((Diagram)diagram,(RenderingOptions)renderingOptions);
                Writer out = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                g2.stream(out,false);
                final int width = ((Diagram)diagram).getWidth();
                final int height = ((Diagram)diagram).getHeight();
                return new ImageDataSimple(width, height);
            }else{
                final int width = ((Diagram)diagram).getWidth();
                final int height = ((Diagram)diagram).getHeight();
                return new ImageDataSimple(width, height);
            }


        } catch (Throwable e) {
            final List<String> strings = new ArrayList<>();
            strings.add("DITAA has crashed");
            strings.add(" ");
            GraphvizCrash.youShouldSendThisDiagram(strings);
            strings.add(" ");
            UmlDiagram.exportDiagramError(os, e, new FileFormatOption(FileFormat.PNG), seed(), null, null, strings);
            return ImageDataSimple.error();
        }

    }
}
