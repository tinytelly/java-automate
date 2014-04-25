package org.tinytelly.model;


import org.tinytelly.service.PropertiesService;

public class PropertyPair {
    private String left;
    private String right;

    public PropertyPair(String left, String right) {
        this.left = left.trim();
        this.right = right.trim();
    }

    public String getLeft() {
        return left;
    }

    public String getRight() {
        return right;
    }

    public static String construct(String left, String right) {
        return left + PropertiesService.LINK_ON + right;
    }
}
