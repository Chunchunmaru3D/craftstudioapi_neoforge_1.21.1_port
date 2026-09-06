package com.leviathanstudio.craftstudio.client.json;

import java.util.ArrayList;
import java.util.List;

 



public class CSReadedAnim
{
    private String                  name;
    private int                     duration;
    private boolean                 holdLastK;
    private List<CSReadedAnimBlock> blocks = new ArrayList<>();

    public CSReadedAnimBlock getBlockFromName(String name) {
        for (CSReadedAnimBlock block : this.blocks)
            if (block.getName().equals(name))
                return block;
        return null;
    }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public int getDuration() { return this.duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public boolean isHoldLastK() { return this.holdLastK; }
    public void setHoldLastK(boolean holdLastK) { this.holdLastK = holdLastK; }

    public List<CSReadedAnimBlock> getBlocks() { return this.blocks; }
}
