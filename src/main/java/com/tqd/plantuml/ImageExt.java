package com.tqd.plantuml;

public class ImageExt<T> {

    private T data;
    private int width;
    private int height;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
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

    public ImageExt(T data, int width, int height) {
        this.data = data;
        this.width = width;
        this.height = height;
    }
}
