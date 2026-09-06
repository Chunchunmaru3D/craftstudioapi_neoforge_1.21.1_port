package com.leviathanstudio.craftstudio.client.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;

import org.joml.Vector3f;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.leviathanstudio.craftstudio.client.exception.CSMalformedJsonException;
import com.leviathanstudio.craftstudio.client.exception.CSResourceNotFoundException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

 





























public class CSJsonReader
{
     
    private static final int[] VERTEX_ORDER_CONVERT = new int[] { 3, 2, 1, 0, 6, 7, 4, 5 };

    private final JsonObject root;
    private final String     ress;

    public CSJsonReader(ResourceManager resourceManager, ResourceLocation resourceIn) {
        this.ress = resourceIn.toString();
         
         
         
        try {
            Resource resource = resourceManager.getResourceOrThrow(resourceIn);
            try (InputStream stream = resource.open();
                 InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonElement parsed = JsonParser.parseReader(reader);
                this.root = parsed.getAsJsonObject();
            }
        } catch (IOException | RuntimeException e) {
            throw new CSResourceNotFoundException(this.ress);
        }
    }

     


    public CSReadedModel readModel() {
        CSReadedModel model = new CSReadedModel();

        JsonElement jsEl = this.root.get("title");
        if (jsEl == null)
            throw new CSMalformedJsonException("title", "String", this.ress);
        model.setName(strNormalize(jsEl.getAsString()));

        JsonArray tree = this.root.getAsJsonArray("tree");
        if (tree == null)
            throw new CSMalformedJsonException("tree", "Array", this.ress);

        for (JsonElement element : tree)
            if (element.isJsonObject()) {
                CSReadedModelBlock parent = new CSReadedModelBlock();
                model.getParents().add(parent);
                try {
                    readModelBlock(element.getAsJsonObject(), parent, true);
                } catch (CSMalformedJsonException e) {
                    throw e;
                } catch (RuntimeException e) {
                    throw new CSMalformedJsonException(parent.getName() != null ? parent.getName() : "a parent block without name", this.ress);
                }
            }
        return model;
    }

    private static void readModelBlock(JsonObject jsonBlock, CSReadedModelBlock block, boolean isRoot) {
        block.setName(strNormalize(jsonBlock.get("name").getAsString()));
        block.setRoot(isRoot);

        JsonArray array = jsonBlock.getAsJsonArray("size");
        float sizeX = array.get(0).getAsFloat();
        float sizeY = array.get(1).getAsFloat();
        float sizeZ = array.get(2).getAsFloat();

        array = jsonBlock.getAsJsonArray("position");
        float posX = array.get(0).getAsFloat();
        float posY = array.get(1).getAsFloat();
        float posZ = array.get(2).getAsFloat();

        array = jsonBlock.getAsJsonArray("rotation");
        float rotationX = array.get(0).getAsFloat();
        float rotationY = array.get(1).getAsFloat();
        float rotationZ = array.get(2).getAsFloat();

        array = jsonBlock.getAsJsonArray("offsetFromPivot");
        float pivotOffsetX = array.get(0).getAsFloat();
        float pivotOffsetY = array.get(1).getAsFloat();
        float pivotOffsetZ = array.get(2).getAsFloat();

         
         
        JsonArray vertexArray = jsonBlock.getAsJsonArray("vertexCoords");
        if (vertexArray != null) {
            float[][] vertex = new float[8][3];
            for (int i = 0; i < 8; i++) {
                JsonArray v = vertexArray.get(VERTEX_ORDER_CONVERT[i]).getAsJsonArray();
                vertex[i][0] = v.get(0).getAsFloat();
                vertex[i][1] = -v.get(1).getAsFloat();
                vertex[i][2] = -v.get(2).getAsFloat();
            }
            block.setVertex(vertex);
        }
        block.setStretch(new Vector3f(1, 1, 1));

        if (isRoot)
            block.setRotationPoint(new Vector3f(posX, -posY + 24, -posZ));
        else
            block.setRotationPoint(new Vector3f(posX, -posY, -posZ));
        block.setRotation(new Vector3f(rotationX, -rotationY, -rotationZ));
        block.setOffset(new Vector3f(pivotOffsetX, -pivotOffsetY, -pivotOffsetZ));
        block.setSize(new Vector3f(sizeX, -sizeY, -sizeZ));

        array = jsonBlock.getAsJsonArray("texOffset");
        block.getTexOffset()[0] = array.get(0).getAsInt();
        block.getTexOffset()[1] = array.get(1).getAsInt();

        JsonArray children = jsonBlock.getAsJsonArray("children");
        if (children != null)
            for (JsonElement element : children) {
                CSReadedModelBlock child = new CSReadedModelBlock();
                block.getChilds().add(child);
                readModelBlock(element.getAsJsonObject(), child, false);
            }
    }

     


    public CSReadedAnim readAnim() {
        CSReadedAnim anim = new CSReadedAnim();

        JsonElement jsEl = this.root.get("title");
        if (jsEl == null)
            throw new CSMalformedJsonException("title", "String", this.ress);
        anim.setName(strNormalize(jsEl.getAsString()));

        jsEl = this.root.get("duration");
        if (jsEl == null)
            throw new CSMalformedJsonException("duration", "Integer", this.ress);
        anim.setDuration(jsEl.getAsInt());

        jsEl = this.root.get("holdLastKeyframe");
        if (jsEl == null)
            throw new CSMalformedJsonException("holdLastKeyframe", "Boolean", this.ress);
        anim.setHoldLastK(jsEl.getAsBoolean());

        jsEl = this.root.get("nodeAnimations");
        if (jsEl == null)
            throw new CSMalformedJsonException("nodeAnimations", "Object", this.ress);

        for (Entry<String, JsonElement> entry : jsEl.getAsJsonObject().entrySet()) {
            CSReadedAnimBlock block = new CSReadedAnimBlock();
            anim.getBlocks().add(block);
            try {
                readAnimBlock(entry, block);
            } catch (RuntimeException e) {
                throw new CSMalformedJsonException(block.getName() != null ? block.getName() : "a block without name", this.ress);
            }
        }
        return anim;
    }

    private static void readAnimBlock(Entry<String, JsonElement> entry, CSReadedAnimBlock block) {
        block.setName(strNormalize(entry.getKey()));
        JsonObject objBlock = entry.getValue().getAsJsonObject();

        addKFElementIfPresent(objBlock, "position", block, EnumFrameType.POSITION);
        addKFElementIfPresent(objBlock, "offsetFromPivot", block, EnumFrameType.OFFSET);
        addKFElementIfPresent(objBlock, "size", block, EnumFrameType.SIZE);
        addKFElementIfPresent(objBlock, "rotation", block, EnumFrameType.ROTATION);
        addKFElementIfPresent(objBlock, "stretch", block, EnumFrameType.STRETCH);
    }

    private static void addKFElementIfPresent(JsonObject objBlock, String field, CSReadedAnimBlock block, EnumFrameType type) {
        JsonElement objField = objBlock.get(field);
        if (objField == null || !objField.isJsonObject())
            return;
        for (Entry<String, JsonElement> entry : objField.getAsJsonObject().entrySet()) {
            int keyFrame = Integer.parseInt(entry.getKey());
            JsonArray array = entry.getValue().getAsJsonArray();
            Vector3f value;
            switch (type) {
                case STRETCH:
                case SIZE:
                    value = new Vector3f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
                    break;
                default:
                    value = new Vector3f(array.get(0).getAsFloat(), -array.get(1).getAsFloat(), -array.get(2).getAsFloat());
            }
            block.addKFElement(keyFrame, type, value);
        }
    }

     
    private static String strNormalize(String str) {
        return str.replaceAll("[^\\dA-Za-z ]", "_").replaceAll("\\s+", "_").replaceAll("[^\\p{ASCII}]", "_");
    }
}
