package com.tqd.flexmark;

import com.tqd.plantuml.DitaaReplaceUtils;
import org.jetbrains.annotations.NotNull;
 
import com.vladsch.flexmark.html.HtmlRenderer.Builder;
import com.vladsch.flexmark.html.HtmlRenderer.HtmlRendererExtension;
import com.vladsch.flexmark.util.data.MutableDataHolder;

public class PlantUMLExtension implements HtmlRendererExtension{

    static {
        //ditta svg supported
        DitaaReplaceUtils.replace();
    }
	 @Override
     public void rendererOptions(@NotNull MutableDataHolder options) {

     }

     @Override
     public void extend(@NotNull Builder htmlRendererBuilder, @NotNull String rendererType) {
         htmlRendererBuilder.nodeRendererFactory(new PlantUMLCodeNodeRenderer.Factory());
     }

     public static PlantUMLExtension create() {
         return new PlantUMLExtension();
     }
}
