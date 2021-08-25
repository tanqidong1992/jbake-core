package com.tqd.plantuml;

import com.credibledoc.plantuml.svggenerator.SvgGeneratorService;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DitaaImageTest {

    @Test
    public void testSvg() throws IOException {
        String plantUml= """
@startuml
ditaa
+--------+   +-------+    +-------+
|        | --+ ditaa +--> |       |
|  Text  |   +-------+    |diagram|
|Document|   |!magic!|    |       |
|     {d}|   |       |    |       |
+---+----+   +-------+    +-------+
    :                         ^
    |       Lots of work      |
    +-------------------------+
@enduml
                """;
        SourceStringReader reader = new SourceStringReader(plantUml);


        FileFormatOption fileFormatOption = new FileFormatOption(FileFormat.SVG);
        ByteArrayOutputStream os=new ByteArrayOutputStream();
        DiagramDescription diagramDescription = reader.outputImage(os, fileFormatOption);

        FileUtils.writeByteArrayToFile(new File("target","ditaa.png"), os.toByteArray());
        DitaaReplaceUtils.replace();
        String svg = SvgGeneratorService.getInstance().generateSvgFromPlantUml(plantUml,false);
        System.out.println(svg);
        FileUtils.write(new File("target","ditaa.svg"),svg, StandardCharsets.UTF_8);

    }
}
