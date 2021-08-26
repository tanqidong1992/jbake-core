package com.tqd.plantuml;

import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.Log;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.core.DiagramDescription;
import net.sourceforge.plantuml.core.ImageData;

import java.io.IOException;
import java.io.OutputStream;

public class SourceStringReaderExt extends SourceStringReader {

    public SourceStringReaderExt(String source) {
        super(source);
    }

    public DiagramDescriptionExt outputImageExt(OutputStream os, int numImage, FileFormatOption fileFormatOption)
            throws IOException {
        if (getBlocks().size() == 0) {
            noStartumlFound(os, fileFormatOption);
            return null;
        }
        for (BlockUml b : getBlocks()) {
            final Diagram system = b.getDiagram();
            final int nbInSystem = system.getNbImages();
            if (numImage < nbInSystem) {
                // final CMapData cmap = new CMapData();
                final ImageData imageData = system.exportDiagram(os, numImage, fileFormatOption);
                // if (imageData.containsCMapData()) {
                // return system.getDescription().getDescription() + BackSlash.BS_N +
                // imageData.getCMapData("plantuml");
                // }
                DiagramDescriptionExt desc=new DiagramDescriptionExt(system.getDescription().getDescription());
                desc.setImageData(imageData);
                return desc;
            }
            numImage -= nbInSystem;
        }
        Log.error("numImage is too big = " + numImage);
        return null;

    }
}
