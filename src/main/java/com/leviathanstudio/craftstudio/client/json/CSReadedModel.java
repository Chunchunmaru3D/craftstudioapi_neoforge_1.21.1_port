package com.leviathanstudio.craftstudio.client.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

 



public class CSReadedModel
{
    private String                   name;
    private int                      textureWidth  = 64;
    private int                      textureHeight = 64;
    private List<CSReadedModelBlock> parents       = new ArrayList<>();

     







    private final Map<String, List<String>> pathCache = new HashMap<>();

    public CSReadedModelBlock getBlockFromName(String name) {
        for (CSReadedModelBlock block : this.parents) {
            CSReadedModelBlock b = block.getBlockFromName(name);
            if (b != null)
                return b;
        }
        return null;
    }

     






    public List<String> getPathTo(String name) {
        List<String> cached = this.pathCache.get(name);
        if (cached != null)
            return cached;
        for (CSReadedModelBlock block : this.parents) {
            List<String> path = new ArrayList<>();
            if (block.collectPathTo(name, path)) {
                this.pathCache.put(name, path);
                return path;
            }
        }
        return null;
    }

    public boolean isAnimable() {
        return this.whyUnAnimable() == null;
    }

    public String whyUnAnimable() {
        List<String> names = new ArrayList<>();
        for (CSReadedModelBlock block : this.parents) {
            String str = block.whyUnAnimable(names);
            if (str != null)
                return str;
        }
        return null;
    }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public int getTextureWidth() { return this.textureWidth; }
    public void setTextureWidth(int textureWidth) { this.textureWidth = textureWidth; }

    public int getTextureHeight() { return this.textureHeight; }
    public void setTextureHeight(int textureHeight) { this.textureHeight = textureHeight; }

    public List<CSReadedModelBlock> getParents() { return this.parents; }
}
