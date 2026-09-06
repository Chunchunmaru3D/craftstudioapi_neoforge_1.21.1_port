package com.leviathanstudio.craftstudio.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import com.leviathanstudio.craftstudio.client.json.CSReadedModel;
import com.leviathanstudio.craftstudio.client.json.CSReadedModelBlock;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

 





















































































public class CSModelBaker
{
    public static final String BOX_SUFFIX = "_box";
     





    public static final String YAW_SUFFIX = "_yaw";
    public static final String PITCH_SUFFIX = "_pitch";
    public static final String ROLL_SUFFIX = "_roll";
     
    public static final String CHILDREN_SUFFIX = "_children";

    public static LayerDefinition bake(CSReadedModel model, int textureWidth, int textureHeight) {
        return bakeAndListRootNames(model, textureWidth, textureHeight).layer();
    }

     








    public static float[] convertRotationOrder(float xDeg, float yDeg, float zDeg) {
        double x = Math.toRadians(xDeg);
        double y = Math.toRadians(yDeg);
        double z = Math.toRadians(zDeg);
        double sinX = Math.sin(x), cosX = Math.cos(x);
        double sinY = Math.sin(y), cosY = Math.cos(y);
        double sinZ = Math.sin(z), cosZ = Math.cos(z);

        double sinB = Math.max(-1.0, Math.min(1.0, sinY * cosX));
        double b = Math.asin(sinB);
        double a = Math.atan2(sinX, cosX * cosY);
        double c = Math.atan2(sinZ * cosY - sinX * sinY * cosZ, sinX * sinY * sinZ + cosY * cosZ);

        return new float[] { (float) a, (float) b, (float) c };
    }

     









    public static BakedResult bakeAndListRootNames(CSReadedModel model, int textureWidth, int textureHeight) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        Map<String, Integer> siblingCounts = new HashMap<>();
        List<String> rootNames = new ArrayList<>();
        for (CSReadedModelBlock parent : model.getParents())
            rootNames.add(addNode(root, parent, siblingCounts));
        LayerDefinition layer = LayerDefinition.create(mesh, textureWidth, textureHeight);
        return new BakedResult(layer, rootNames);
    }

    public record BakedResult(LayerDefinition layer, List<String> rootNames) {}

     
    private static String addNode(PartDefinition parent, CSReadedModelBlock node, Map<String, Integer> siblingCounts) {
        String name = disambiguate(node.getName(), siblingCounts);

        Vector3f rp = node.getRotationPoint();
        Vector3f rot = node.getRotation();
        Vector3f off = node.getOffset();

         
         
         
         
         
         
         
        PartDefinition outer = parent.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.offset(rp.x, rp.y, rp.z));
        PartDefinition yaw = outer.addOrReplaceChild(name + YAW_SUFFIX, CubeListBuilder.create(),
                PartPose.rotation(0.0F, (float) Math.toRadians(rot.y), 0.0F));
        PartDefinition pitch = yaw.addOrReplaceChild(name + PITCH_SUFFIX, CubeListBuilder.create(),
                PartPose.rotation((float) Math.toRadians(rot.x), 0.0F, 0.0F));
        PartDefinition roll = pitch.addOrReplaceChild(name + ROLL_SUFFIX, CubeListBuilder.create(),
                PartPose.rotation(0.0F, 0.0F, (float) Math.toRadians(rot.z)));

        CubeListBuilder cubeBuilder = CubeListBuilder.create().texOffs(node.getTexOffset()[0], node.getTexOffset()[1]);
        addBoxGeometry(cubeBuilder, node);
        PartDefinition inner = roll.addOrReplaceChild(name + BOX_SUFFIX, cubeBuilder, PartPose.offset(off.x, off.y, off.z));

         
         
        PartDefinition childAnchor = roll.addOrReplaceChild(name + CHILDREN_SUFFIX, CubeListBuilder.create(),
                PartPose.offset(off.x, off.y, off.z));

         
         
         
         
         
         
         
         
         
         
         
        Map<String, Integer> childSiblingCounts = new HashMap<>();
        for (CSReadedModelBlock child : node.getChilds())
            addNode(childAnchor, child, childSiblingCounts);

        return name;
    }

     
    private static String disambiguate(String name, Map<String, Integer> siblingCounts) {
        Integer count = siblingCounts.get(name);
        if (count == null) {
            siblingCounts.put(name, 1);
            return name;
        }
        count++;
        siblingCounts.put(name, count);
        return name + "_" + count;
    }

    private static void addBoxGeometry(CubeListBuilder cubeBuilder, CSReadedModelBlock node) {
        float[][] vertex = node.getVertex();
        if (vertex != null) {
            float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
            for (float[] v : vertex) {
                minX = Math.min(minX, v[0]);
                maxX = Math.max(maxX, v[0]);
                minY = Math.min(minY, v[1]);
                maxY = Math.max(maxY, v[1]);
                minZ = Math.min(minZ, v[2]);
                maxZ = Math.max(maxZ, v[2]);
            }
             
             
             
            float sx = stretch(maxX - minX, Math.abs(node.getSize().x));
            float sy = stretch(maxY - minY, Math.abs(node.getSize().y));
            float sz = stretch(maxZ - minZ, Math.abs(node.getSize().z));
            cubeBuilder.addBox(minX / sx, minY / sy, minZ / sz,
                    (maxX - minX) / sx, (maxY - minY) / sy, (maxZ - minZ) / sz);
        } else {
            float dx = Math.abs(node.getSize().x);
            float dy = Math.abs(node.getSize().y);
            float dz = Math.abs(node.getSize().z);
            cubeBuilder.addBox(-dx / 2F, -dy / 2F, -dz / 2F, dx, dy, dz);
        }
    }

    private static float stretch(float actual, float declared) {
        return declared == 0.0F ? 1.0F : actual / declared;
    }

     
    public static void applyStaticStretch(ModelPart root, CSReadedModel model) {
        for (CSReadedModelBlock node : model.getParents())
            applyStaticStretch(root, node);
    }

    private static void applyStaticStretch(ModelPart parent, CSReadedModelBlock node) {
        ModelPart outer = parent.getChild(node.getName());
        ModelPart roll = outer.getChild(node.getName() + YAW_SUFFIX)
                .getChild(node.getName() + PITCH_SUFFIX)
                .getChild(node.getName() + ROLL_SUFFIX);
        ModelPart box = roll.getChild(node.getName() + BOX_SUFFIX);
        float[][] vertex = node.getVertex();
        if (vertex != null) {
            float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
            for (float[] v : vertex) {
                minX = Math.min(minX, v[0]); maxX = Math.max(maxX, v[0]);
                minY = Math.min(minY, v[1]); maxY = Math.max(maxY, v[1]);
                minZ = Math.min(minZ, v[2]); maxZ = Math.max(maxZ, v[2]);
            }
            box.xScale = stretch(maxX - minX, Math.abs(node.getSize().x));
            box.yScale = stretch(maxY - minY, Math.abs(node.getSize().y));
            box.zScale = stretch(maxZ - minZ, Math.abs(node.getSize().z));
        }

        ModelPart childAnchor = roll.getChild(node.getName() + CHILDREN_SUFFIX);
        for (CSReadedModelBlock child : node.getChilds())
            applyStaticStretch(childAnchor, child);
    }
}
