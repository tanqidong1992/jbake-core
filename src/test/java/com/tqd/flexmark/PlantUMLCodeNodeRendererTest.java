package com.tqd.flexmark;

import com.tqd.flexmark.utils.FlexmarkExtensionUtils;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class PlantUMLCodeNodeRendererTest {
    final private static DataHolder OPTIONS = PegdownOptionsAdapter.flexmarkOptions(
            Extensions.ALL
    );
    public static void main(String[] args) throws IOException {
        MutableDataSet options = new MutableDataSet(OPTIONS);

        options.set(Parser.EXTENSIONS, FlexmarkExtensionUtils.getAllExtensionInClasspath());
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        // You can re-use parser and renderer instances
        String s= """
## Ditta
```plantuml
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
```
## Sequence
```plantuml
@startuml
A -> B
@enduml
```
""";
        Node document = parser.parse(s);
        String html = renderer.render(document);  // "<p>This is <em>Sparta</em></p>\n"
        System.out.println(html);
        FileUtils.writeStringToFile(new File("target","PlantUMLCodeNodeRendererTest.html"),html, StandardCharsets.UTF_8);
    }
}