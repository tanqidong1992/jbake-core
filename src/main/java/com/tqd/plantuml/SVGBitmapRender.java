package com.tqd.plantuml;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;

import org.stathissideris.ascii2image.core.RenderingOptions;
import org.stathissideris.ascii2image.core.Shape3DOrderingComparator;
import org.stathissideris.ascii2image.graphics.*;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import java.awt.image.RenderedImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class SVGBitmapRender {

    private static final boolean DEBUG = false;

    private static final String IDREGEX = "^.+_vfill$";

    Stroke normalStroke;
    Stroke dashStroke;


    public static SVGGraphics2D newSVGGraphics2D() {

        // Get a DOMImplementation.
        DOMImplementation domImpl= GenericDOMImplementation.getDOMImplementation();

        // Create an instance of org.w3c.dom.Document.
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);

        // Create an instance of the SVG Generator.
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        // Ask the test to render into the SVG Graphics2D implementation.
        // Finally, stream out SVG to the standard output using
        // UTF-8 encoding.
        //boolean useCSS = true; // we want to use CSS style attributes
        //Writer out = new OutputStreamWriter(System.out, "UTF-8");
        //svgGenerator.stream(out, useCSS);

        return svgGenerator;
    }



    public SVGGraphics2D render(Diagram diagram,RenderingOptions options){

        SVGGraphics2D g2=newSVGGraphics2D();


        Object antialiasSetting = RenderingHints.VALUE_ANTIALIAS_OFF;
        if(options.performAntialias())
            antialiasSetting = RenderingHints.VALUE_ANTIALIAS_ON;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasSetting);

        g2.setColor(Color.white);
        //TODO: find out why the next line does not work
        g2.fillRect(0, 0, diagram.getWidth()+10, diagram.getHeight()+10);
		/*for(int y = 0; y < diagram.getHeight(); y ++)
			g2.drawLine(0, y, diagram.getWidth(), y);*/

        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));

        ArrayList shapes = diagram.getAllDiagramShapes();

        if(DEBUG) System.out.println("Rendering "+shapes.size()+" shapes (groups flattened)");

        Iterator shapesIt;
        if(options.dropShadows()){
            //render shadows
            shapesIt = shapes.iterator();
            while(shapesIt.hasNext()){
                DiagramShape shape = (DiagramShape) shapesIt.next();

                if(shape.getPoints().isEmpty()) continue;

                //GeneralPath path = shape.makeIntoPath();
                GeneralPath path;
                path = shape.makeIntoRenderPath(diagram);

                float offset = diagram.getMinimumOfCellDimension() / 3.333f;

                if(path != null
                        && shape.dropsShadow()
                        && shape.getType() != DiagramShape.TYPE_CUSTOM){
                    GeneralPath shadow = new GeneralPath(path);
                    AffineTransform translate = new AffineTransform();
                    translate.setToTranslation(offset, offset);
                    shadow.transform(translate);
                    g2.setColor(new Color(150,150,150));
                    g2.fill(shadow);

                }
            }
            //return g2;
        }


        //fill and stroke

        float dashInterval = Math.min(diagram.getCellWidth(), diagram.getCellHeight()) / 2;
        //Stroke normalStroke = g2.getStroke();

        float strokeWeight = diagram.getMinimumOfCellDimension() / 10;

        normalStroke =
                new BasicStroke(
                        strokeWeight,
                        //10,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                );

        dashStroke =
                new BasicStroke(
                        strokeWeight,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_ROUND,
                        0,
                        new float[] {dashInterval},
                        0
                );

        //TODO: at this stage we should draw the open shapes first in order to make sure they are at the bottom (this is useful for the {mo} shape)


        //find storage shapes
        ArrayList storageShapes = new ArrayList();
        shapesIt = shapes.iterator();
        while(shapesIt.hasNext()){
            DiagramShape shape = (DiagramShape) shapesIt.next();
            if(shape.getType() == DiagramShape.TYPE_STORAGE) {
                storageShapes.add(shape);
                continue;
            }
        }

        //render storage shapes
        //special case since they are '3d' and should be
        //rendered bottom to top
        //TODO: known bug: if a storage object is within a bigger normal box, it will be overwritten in the main drawing loop
        //(BUT this is not possible since tags are applied to all shapes overlaping shapes)


        Collections.sort(storageShapes, new Shape3DOrderingComparator());

        g2.setStroke(normalStroke);
        shapesIt = storageShapes.iterator();
        while(shapesIt.hasNext()){
            DiagramShape shape = (DiagramShape) shapesIt.next();

            GeneralPath path;
            path = shape.makeIntoRenderPath(diagram);

            if(!shape.isStrokeDashed()) {
                if(shape.getFillColor() != null)
                    g2.setColor(shape.getFillColor());
                else
                    g2.setColor(Color.white);
                g2.fill(path);
            }

            if(shape.isStrokeDashed())
                g2.setStroke(dashStroke);
            else
                g2.setStroke(normalStroke);
            g2.setColor(shape.getStrokeColor());
            g2.draw(path);
        }


        //render the rest of the shapes
        ArrayList pointMarkers = new ArrayList();
        shapesIt = shapes.iterator();
        while(shapesIt.hasNext()){
            DiagramShape shape = (DiagramShape) shapesIt.next();
            if(shape.getType() == DiagramShape.TYPE_POINT_MARKER) {
                pointMarkers.add(shape);
                continue;
            }
            if(shape.getType() == DiagramShape.TYPE_STORAGE) {
                continue;
            }
            if(shape.getType() == DiagramShape.TYPE_CUSTOM){
                renderCustomShape(shape, g2);
                continue;
            }

            if(shape.getPoints().isEmpty()) continue;

            int size = shape.getPoints().size();

            GeneralPath path;
            path = shape.makeIntoRenderPath(diagram);

            //fill
            if(path != null && shape.isClosed() && !shape.isStrokeDashed()){
                if(shape.getFillColor() != null)
                    g2.setColor(shape.getFillColor());
                else
                    g2.setColor(Color.white);
                g2.fill(path);
            }

            //draw
            if(shape.getType() != DiagramShape.TYPE_ARROWHEAD){
                g2.setColor(shape.getStrokeColor());
                if(shape.isStrokeDashed())
                    g2.setStroke(dashStroke);
                else
                    g2.setStroke(normalStroke);
                g2.draw(path);
            }
        }

        //render point markers

        g2.setStroke(normalStroke);
        shapesIt = pointMarkers.iterator();
        while(shapesIt.hasNext()){
            DiagramShape shape = (DiagramShape) shapesIt.next();
            //if(shape.getType() != DiagramShape.TYPE_POINT_MARKER) continue;

            GeneralPath path;
            path = shape.makeIntoRenderPath(diagram);

            g2.setColor(Color.white);
            g2.fill(path);
            g2.setColor(shape.getStrokeColor());
            g2.draw(path);
        }

        //handle text
        //g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //renderTextLayer(diagram.getTextObjects().iterator());

        Iterator textIt = diagram.getTextObjects().iterator();
        while(textIt.hasNext()){
            DiagramText text = (DiagramText) textIt.next();
            g2.setFont(text.getFont());
            if(text.hasOutline()){
                g2.setColor(text.getOutlineColor());
                g2.drawString(text.getText(), text.getXPos() + 1, text.getYPos());
                g2.drawString(text.getText(), text.getXPos() - 1, text.getYPos());
                g2.drawString(text.getText(), text.getXPos(), text.getYPos() + 1);
                g2.drawString(text.getText(), text.getXPos(), text.getYPos() - 1);
            }
            g2.setColor(text.getColor());
            g2.drawString(text.getText(), text.getXPos(), text.getYPos());
        }

        if(options.renderDebugLines() || DEBUG){
            Stroke debugStroke =
                    new BasicStroke(
                            1,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    );
            g2.setStroke(debugStroke);
            g2.setColor(new Color(170, 170, 170));
            g2.setXORMode(Color.white);
            for(int x = 0; x < diagram.getWidth(); x += diagram.getCellWidth())
                g2.drawLine(x, 0, x, diagram.getHeight());
            for(int y = 0; y < diagram.getHeight(); y += diagram.getCellHeight())
                g2.drawLine(0, y, diagram.getWidth(), y);
        }


        g2.dispose();

        return g2;
    }

    private RenderedImage renderTextLayer(ArrayList textObjects, int width, int height){
        SVGBitmapRender.TextCanvas canvas = new SVGBitmapRender.TextCanvas(textObjects);
        Image image = canvas.createImage(width, height);
        Graphics g = image.getGraphics();
        canvas.paint(g);
        return (RenderedImage) image;
    }

    private class TextCanvas extends Canvas {
        ArrayList textObjects;

        public TextCanvas(ArrayList textObjects){
            this.textObjects = textObjects;
        }

        public void paint(Graphics g){
            Graphics g2 = (Graphics2D) g;
            Iterator textIt = textObjects.iterator();
            while(textIt.hasNext()){
                DiagramText text = (DiagramText) textIt.next();
                g2.setFont(text.getFont());
                if(text.hasOutline()){
                    g2.setColor(text.getOutlineColor());
                    g2.drawString(text.getText(), text.getXPos() + 1, text.getYPos());
                    g2.drawString(text.getText(), text.getXPos() - 1, text.getYPos());
                    g2.drawString(text.getText(), text.getXPos(), text.getYPos() + 1);
                    g2.drawString(text.getText(), text.getXPos(), text.getYPos() - 1);
                }
                g2.setColor(text.getColor());
                g2.drawString(text.getText(), text.getXPos(), text.getYPos());
            }
        }
    }

    private void renderCustomShape(DiagramShape shape, Graphics2D g2){
        CustomShapeDefinition definition = shape.getDefinition();

        Rectangle bounds = shape.getBounds();

        if(definition.hasBorder()){
            g2.setColor(shape.getStrokeColor());
            if(shape.isStrokeDashed())
                g2.setStroke(dashStroke);
            else
                g2.setStroke(normalStroke);
            g2.drawLine(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y);
            g2.drawLine(bounds.x + bounds.width, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height);
            g2.drawLine(bounds.x, bounds.y + bounds.height, bounds.x + bounds.width, bounds.y + bounds.height);
            g2.drawLine(bounds.x, bounds.y, bounds.x, bounds.y + bounds.height);

//			g2.drawRect(bounds.x, bounds.y, bounds.width, bounds.height); //looks different!
        }

        //TODO: custom shape distintion relies on filename extension. Make this more intelligent
        if(definition.getFilename().endsWith(".png")){
            renderCustomPNGShape(shape, g2);
        } else if(definition.getFilename().endsWith(".svg")){
            // renderCustomSVGShape(shape, g2);
            throw new UnsupportedOperationException();
        }
    }

//	private void renderCustomSVGShape(DiagramShape shape, Graphics2D g2){
//		CustomShapeDefinition definition = shape.getDefinition();
//		Rectangle bounds = shape.getBounds();
//		Image graphic;
//		try {
//			if(shape.getFillColor() == null) {
//				graphic = ImageHandler.instance().renderSVG(
//						definition.getFilename(), bounds.width, bounds.height, definition.stretches());
//			} else {
//				graphic = ImageHandler.instance().renderSVG(
//						definition.getFilename(), bounds.width, bounds.height, definition.stretches(), IDREGEX, shape.getFillColor());
//			}
//			g2.drawImage(graphic, bounds.x, bounds.y, null);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

    private void renderCustomPNGShape(DiagramShape shape, Graphics2D g2){
        CustomShapeDefinition definition = shape.getDefinition();
        Rectangle bounds = shape.getBounds();
        Image graphic = ImageHandler.instance().loadImage(definition.getFilename());

        int xPos, yPos, width, height;

        if(definition.stretches()){ //occupy all available space
            xPos = bounds.x; yPos = bounds.y;
            width = bounds.width; height = bounds.height;
        } else { //decide how to fit
            int newHeight = bounds.width * graphic.getHeight(null) / graphic.getWidth(null);
            if(newHeight < bounds.height){ //expand to fit width
                height = newHeight;
                width = bounds.width;
                xPos = bounds.x;
                yPos = bounds.y + bounds.height / 2 - graphic.getHeight(null) / 2;
            } else { //expand to fit height
                width = graphic.getWidth(null) * bounds.height / graphic.getHeight(null);
                height = bounds.height;
                xPos = bounds.x + bounds.width / 2 - graphic.getWidth(null) / 2;
                yPos = bounds.y;
            }
        }

        g2.drawImage(graphic, xPos, yPos, width, height, null);
    }

    public static boolean isColorDark(Color color){
        int brightness = Math.max(color.getRed(), color.getGreen());
        brightness = Math.max(color.getBlue(), brightness);
        if(brightness < 200) {
            if(DEBUG) System.out.println("Color "+color+" is dark");
            return true;
        }
        if(DEBUG) System.out.println("Color "+color+" is not dark");
        return false;
    }
}
