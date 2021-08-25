package com.tqd.plantuml;

import com.credibledoc.plantuml.svggenerator.SvgGeneratorService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DotImageTest {

    @Test
    public void testSvg() throws IOException {
        String s= """
                @startuml
                digraph t {
                    A -> {B C}
                }
                @enduml
                """;
        String svg = SvgGeneratorService.getInstance().generateSvgFromPlantUml(s,false);
        System.out.println(svg);
        FileUtils.write(new File("target","test.svg"),svg, StandardCharsets.UTF_8);
    }
}
