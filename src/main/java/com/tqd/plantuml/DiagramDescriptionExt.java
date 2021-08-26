package com.tqd.plantuml;

import net.sourceforge.plantuml.core.DiagramDescription;
import net.sourceforge.plantuml.core.ImageData;

public class DiagramDescriptionExt extends DiagramDescription {

    private ImageData imageData;

    public DiagramDescriptionExt(String description) {
        super(description);
    }

    public ImageData getImageData() {
        return imageData;
    }

    public void setImageData(ImageData imageData) {
        this.imageData = imageData;
    }
}
