package com.leviathanstudio.craftstudio.client.json;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.joml.Vector3f;

 







public class CSReadedAnimBlock
{
    private String                       name;
    private Map<Integer, ReadedKeyFrame> keyFrames = new TreeMap<>();

    public void addKFElement(int keyFrame, EnumFrameType type, Vector3f value) {
        ReadedKeyFrame kf = this.keyFrames.computeIfAbsent(keyFrame, k -> new ReadedKeyFrame());
        switch (type) {
            case POSITION:
                kf.position = value;
                break;
            case ROTATION:
                kf.rotation = value;
                break;
            case OFFSET:
                kf.offset = value;
                break;
            case SIZE:
                kf.size = value;
                break;
            case STRETCH:
                kf.stretching = value;
                break;
        }
    }

    public static class ReadedKeyFrame
    {
        public Vector3f position, rotation, offset, size, stretching;
    }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public Map<Integer, ReadedKeyFrame> getKeyFrames() { return this.keyFrames; }
}
