package com.tqd.plantuml;

import net.sourceforge.plantuml.PSystemBuilder;
import net.sourceforge.plantuml.api.PSystemFactory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

public class PSystemBuilderTest {

    @Test
    public void test() throws Exception{
        Field field=PSystemBuilder.class.getDeclaredField("factories");
        field.setAccessible(true);
        List<PSystemFactory>  factories=(List<PSystemFactory> )field.get(PSystemBuilder.class);

        Iterator<PSystemFactory> pfs=factories.iterator();
        while(pfs.hasNext()){
            if(pfs.next().getClass().getName().contains("taa")) {
                pfs.remove();
            }
        }
        factories.stream()
                .filter(f->f.getClass().getName().contains("taa"))
                .forEach(f->{
            System.out.println(f.getClass());
        });
    }
}