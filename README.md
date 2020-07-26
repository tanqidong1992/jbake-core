# jbake-core
## 描述
修改自jbake-2.6.5的源码,以满足自己一些自定义的需求

## 修改内容项
- 修正了含有中文文件名的markdown文件在生成html文件后文件名会被转义,导致无法链接.
- 修正配置文件不支持中文.
- markdown格式文档支持提取标题生成toc.
- markdown格式支持plantuml绘图.
- 修正markdown解析渲染引擎flexmark对flexmark本身拓展的支持(Beta).

