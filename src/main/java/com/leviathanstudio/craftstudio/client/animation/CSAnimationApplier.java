package com.leviathanstudio.craftstudio.client.animation;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.joml.Vector3f;

import com.leviathanstudio.craftstudio.CraftStudioApi;
import com.leviathanstudio.craftstudio.client.json.CSReadedAnim;
import com.leviathanstudio.craftstudio.client.json.CSReadedAnimBlock;
import com.leviathanstudio.craftstudio.client.json.CSReadedAnimBlock.ReadedKeyFrame;
import com.leviathanstudio.craftstudio.client.json.CSReadedModel;
import com.leviathanstudio.craftstudio.client.json.CSReadedModelBlock;
import com.leviathanstudio.craftstudio.client.model.CSModelBaker;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.util.Mth;

 


















public class CSAnimationApplier
{
     






    public static float computePhase(CSReadedAnim anim, float rawTime) {
        float duration = anim.getDuration();
        if (duration <= 0F)
            return 0F;
        if (anim.isHoldLastK())
            return Math.min(Math.max(rawTime, 0F), duration);
        float phase = rawTime % duration;
        return phase < 0F ? phase + duration : phase;
    }

     













    public static void apply(ModelPart root, CSReadedAnim anim, CSReadedModel sourceModel, float phase) {
         
         
         
         
        root.getAllParts().forEach(ModelPart::resetPose);
         
         
        CSModelBaker.applyStaticStretch(root, sourceModel);

        for (CSReadedAnimBlock block : anim.getBlocks()) {
            String name = block.getName();

             
             
             
             
            List<String> nodePath = sourceModel.getPathTo(name);
            if (nodePath == null) {
                CraftStudioApi.getLogger().warn("CSAnimationApplier: animation '{}' references missing model node '{}'; skipping it", anim.getName(), name);
                continue;
            }

            ModelPart outer;
            try {
                 
                 
                 
                 
                 
                outer = root;
                for (int i = 0; i < nodePath.size(); i++) {
                    outer = outer.getChild(nodePath.get(i));
                    if (i < nodePath.size() - 1)
                        outer = getInnerPart(outer, nodePath.get(i));
                }
            } catch (NoSuchElementException e) {
                CraftStudioApi.getLogger().warn("CSAnimationApplier: could not find the baked-tree path for node '{}' in animation '{}'; skipping it", name, anim.getName());
                continue;
            }

            Vector3f pos = interpolate(block, phase, KeyFrameField.POSITION);
            if (pos != null) {
                 
                 
                PartPose base = outer.getInitialPose();
                outer.setPos(base.x + pos.x, base.y + pos.y, base.z + pos.z);
            }

            ModelPart yaw;
            ModelPart pitch;
            ModelPart roll;
            try {
                yaw = outer.getChild(name + CSModelBaker.YAW_SUFFIX);
                pitch = yaw.getChild(name + CSModelBaker.PITCH_SUFFIX);
                roll = pitch.getChild(name + CSModelBaker.ROLL_SUFFIX);
            } catch (NoSuchElementException e) {
                CraftStudioApi.getLogger().warn("CSAnimationApplier: could not find the rotation-node chain for '{}' in animation '{}'; skipping it", name, anim.getName());
                continue;
            }

            Vector3f rot = interpolate(block, phase, KeyFrameField.ROTATION);
            if (rot != null) {
                 
                 
                 
                yaw.yRot = yaw.getInitialPose().yRot + (float) Math.toRadians(rot.y);
                pitch.xRot = pitch.getInitialPose().xRot + (float) Math.toRadians(rot.x);
                roll.zRot = roll.getInitialPose().zRot + (float) Math.toRadians(rot.z);
            }

            Vector3f off = interpolate(block, phase, KeyFrameField.OFFSET);
            if (off != null) {
                try {
                    ModelPart inner = roll.getChild(name + CSModelBaker.BOX_SUFFIX);
                     
                    PartPose base = inner.getInitialPose();
                    inner.setPos(base.x + off.x, base.y + off.y, base.z + off.z);
                     
                     
                    ModelPart childAnchor = roll.getChild(name + CSModelBaker.CHILDREN_SUFFIX);
                    PartPose childrenBase = childAnchor.getInitialPose();
                    childAnchor.setPos(childrenBase.x + off.x, childrenBase.y + off.y, childrenBase.z + off.z);
                } catch (NoSuchElementException ignored) {
                     
                }
            }
        }
    }

    private enum KeyFrameField { POSITION, ROTATION, OFFSET }

     




    private static ModelPart getInnerPart(ModelPart outer, String name) {
        return outer.getChild(name + CSModelBaker.YAW_SUFFIX)
                .getChild(name + CSModelBaker.PITCH_SUFFIX)
                .getChild(name + CSModelBaker.ROLL_SUFFIX)
                .getChild(name + CSModelBaker.CHILDREN_SUFFIX);
    }

    private static Vector3f fieldOf(ReadedKeyFrame kf, KeyFrameField field) {
        return switch (field) {
            case POSITION -> kf.position;
            case ROTATION -> kf.rotation;
            case OFFSET -> kf.offset;
        };
    }

     




    private static Vector3f interpolate(CSReadedAnimBlock block, float phase, KeyFrameField field) {
        Map<Integer, ReadedKeyFrame> frames = block.getKeyFrames();
        Integer prevKey = null, nextKey = null;
        Vector3f prevVal = null, nextVal = null;

        for (Map.Entry<Integer, ReadedKeyFrame> entry : frames.entrySet()) {
            Vector3f v = fieldOf(entry.getValue(), field);
            if (v == null)
                continue;
            int k = entry.getKey();
            if (k <= phase && (prevKey == null || k > prevKey)) {
                prevKey = k;
                prevVal = v;
            }
            if (k >= phase && (nextKey == null || k < nextKey)) {
                nextKey = k;
                nextVal = v;
            }
        }

        if (prevVal == null && nextVal == null)
            return null;
        if (prevVal == null)
            return nextVal;
        if (nextVal == null || nextKey.equals(prevKey))
            return prevVal;

        float t = (phase - prevKey) / (float) (nextKey - prevKey);
        return new Vector3f(
                Mth.lerp(t, prevVal.x, nextVal.x),
                Mth.lerp(t, prevVal.y, nextVal.y),
                Mth.lerp(t, prevVal.z, nextVal.z));
    }
}
