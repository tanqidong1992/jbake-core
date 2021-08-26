package com.tqd.plantuml;

public class SVGExt {

    private String svg;
    private int width;
    private int height;

    public String getSvg() {
        return svg;
    }

    public void setSvg(String svg) {
        this.svg = svg;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public SVGExt(String svg, int width, int height) {
        this.svg = svg;
        this.width = width;
        this.height = height;
    }
}
