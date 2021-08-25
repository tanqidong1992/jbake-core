package com.tqd.plantuml;

import net.sourceforge.plantuml.PSystemBuilder;
import net.sourceforge.plantuml.api.PSystemFactory;
import net.sourceforge.plantuml.core.DiagramType;
import net.sourceforge.plantuml.ditaa.PSystemDitaaFactory;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

public class DitaaReplaceUtils {

    public static void replace(){
        Field field= null;
        try {
            field = PSystemBuilder.class.getDeclaredField("factories");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        field.setAccessible(true);
        List<PSystemFactory> factories= null;
        try {
            factories = (List<PSystemFactory>)field.get(PSystemBuilder.class);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Iterator<PSystemFactory> pfs=factories.iterator();
        while(pfs.hasNext()){
            if(pfs.next().getClass().getName().contains("taa")) {
                pfs.remove();
            }
        }
        factories.add(new PSystemDitaaFactoryExt(DiagramType.DITAA));
        factories.add(new PSystemDitaaFactoryExt(DiagramType.UML));
    }
}
