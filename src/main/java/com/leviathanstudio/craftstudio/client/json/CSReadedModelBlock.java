package com.leviathanstudio.craftstudio.client.json;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

 

















public class CSReadedModelBlock
{
    private String                   name;
    private Vector3f                 rotationPoint, rotation, size, stretch, offset;
     
    private float                    vertex[][];
    private int[]                    texOffset = new int[2];
    private List<CSReadedModelBlock> childs    = new ArrayList<>();
     





    private boolean                  root;

    public CSReadedModelBlock getBlockFromName(String name) {
        if (this.name.equals(name))
            return this;
        for (CSReadedModelBlock block : this.childs) {
            CSReadedModelBlock b = block.getBlockFromName(name);
            if (b != null)
                return b;
        }
        return null;
    }

     





    boolean collectPathTo(String target, List<String> path) {
        path.add(this.name);
        if (this.name.equals(target))
            return true;
        for (CSReadedModelBlock child : this.childs)
            if (child.collectPathTo(target, path))
                return true;
        path.remove(path.size() - 1);
        return false;
    }

     



    public String whyUnAnimable(List<String> names) {
        if (names.contains(this.name))
            return this.name;
        names.add(this.name);
        for (CSReadedModelBlock block : this.childs) {
            String str = block.whyUnAnimable(names);
            if (str != null)
                return str;
        }
        return null;
    }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public boolean isRoot() { return this.root; }
    public void setRoot(boolean root) { this.root = root; }

    public Vector3f getRotationPoint() { return this.rotationPoint; }
    public void setRotationPoint(Vector3f rotationPoint) { this.rotationPoint = rotationPoint; }

    public Vector3f getRotation() { return this.rotation; }
    public void setRotation(Vector3f rotation) { this.rotation = rotation; }

    public Vector3f getSize() { return this.size; }
    public void setSize(Vector3f size) { this.size = size; }

    public Vector3f getStretch() { return this.stretch; }
    public void setStretch(Vector3f stretch) { this.stretch = stretch; }

    public Vector3f getOffset() { return this.offset; }
    public void setOffset(Vector3f offset) { this.offset = offset; }

    public float[][] getVertex() { return this.vertex; }
    public void setVertex(float[][] vertex) { this.vertex = vertex; }

    public int[] getTexOffset() { return this.texOffset; }

    public List<CSReadedModelBlock> getChilds() { return this.childs; }
}
