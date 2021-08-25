package com.tqd.plantuml;

import net.sourceforge.plantuml.command.PSystemBasicFactory;
import net.sourceforge.plantuml.core.DiagramType;
import net.sourceforge.plantuml.core.UmlSource;
import net.sourceforge.plantuml.ditaa.PSystemDitaa;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PSystemDitaaFactoryExt extends PSystemBasicFactory<DitaaSVGPSystem> {

    // private StringBuilder data;
    // // -E,--no-separation
    // private boolean performSeparationOfCommonEdges;
    //
    // // -S,--no-shadows
    // private boolean dropShadows;

    public PSystemDitaaFactoryExt(DiagramType diagramType) {
        super(diagramType);
    }

    @Override
    public DitaaSVGPSystem initDiagram(UmlSource source, String startLine) {
        boolean performSeparationOfCommonEdges = true;
        if (startLine != null && (startLine.contains("-E") || startLine.contains("--no-separation"))) {
            performSeparationOfCommonEdges = false;
        }
        boolean dropShadows = true;
        if (startLine != null && (startLine.contains("-S") || startLine.contains("--no-shadows"))) {
            dropShadows = false;
        }
        final float scale = extractScale(startLine);
        if (getDiagramType() == DiagramType.UML) {
            return null;
        } else if (getDiagramType() == DiagramType.DITAA) {
            return new DitaaSVGPSystem(source, "", performSeparationOfCommonEdges, dropShadows, scale);
        } else {
            throw new IllegalStateException(getDiagramType().name());
        }
    }

    @Override
    public DitaaSVGPSystem executeLine(UmlSource source, DitaaSVGPSystem system, String line) {
        if (system == null && (line.equals("ditaa") || line.startsWith("ditaa("))) {
            boolean performSeparationOfCommonEdges = true;
            if (line.contains("-E") || line.contains("--no-separation")) {
                performSeparationOfCommonEdges = false;
            }
            boolean dropShadows = true;
            if (line.contains("-S") || line.contains("--no-shadows")) {
                dropShadows = false;
            }
            final float scale = extractScale(line);
            return new DitaaSVGPSystem(source, "", performSeparationOfCommonEdges, dropShadows, scale);
        }
        if (system == null) {
            return null;
        }
        return system.add(line);
    }

    private float extractScale(String line) {
        if (line == null) {
            return 1;
        }
        final Pattern p = Pattern.compile("scale=([\\d.]+)");
        final Matcher m = p.matcher(line);
        if (m.find()) {
            final String number = m.group(1);
            return Float.parseFloat(number);
        }
        return 1;
    }
}