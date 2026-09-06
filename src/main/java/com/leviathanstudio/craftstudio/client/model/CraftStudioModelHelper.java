package com.leviathanstudio.craftstudio.client.model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.leviathanstudio.craftstudio.CraftStudioApi;
import com.leviathanstudio.craftstudio.client.animation.CSAnimationApplier;
import com.leviathanstudio.craftstudio.client.json.CSReadedAnim;
import com.leviathanstudio.craftstudio.client.json.CSReadedModel;
import com.leviathanstudio.craftstudio.client.registry.CSModelRegistry;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;

 

























public final class CraftStudioModelHelper
{
	 





	private static final Map<CacheKey, CachedLayer> BAKED_LAYERS = new ConcurrentHashMap<>();

	private record CacheKey(ResourceLocation modelKey, int textureWidth, int textureHeight) {}
	private record CachedLayer(LayerDefinition layer, List<String> rootNames) {}

	private CraftStudioModelHelper() {}

     










	public static LayerDefinition bakeLayer(ResourceLocation modelKey, int textureWidth, int textureHeight) {
		 
		 
		 
		return bakeModel(modelKey, textureWidth, textureHeight).layer();
    }

	 




	public static LayerDefinition bakeCachedLayer(ResourceLocation modelKey, int textureWidth, int textureHeight) {
		return getCachedLayer(modelKey, textureWidth, textureHeight).layer();
	}

	 



	public static void applyStaticStretch(ModelPart root, ResourceLocation modelKey) {
		try {
			CSModelBaker.applyStaticStretch(root, CSModelRegistry.INSTANCE.getModel(modelKey));
		} catch (RuntimeException e) {
			CraftStudioApi.getLogger().error("CraftStudioModelHelper: cannot apply static stretch for '{}': {}", modelKey, e.getMessage());
		}
	}

	 
	public static void clearBakedLayers() {
		BAKED_LAYERS.clear();
	}

	private static CachedLayer getCachedLayer(ResourceLocation modelKey, int textureWidth, int textureHeight) {
		CacheKey key = new CacheKey(modelKey, textureWidth, textureHeight);
		return BAKED_LAYERS.computeIfAbsent(key, ignored -> bakeModel(modelKey, textureWidth, textureHeight));
	}

	private static CachedLayer bakeModel(ResourceLocation modelKey, int textureWidth, int textureHeight) {
		try {
			CSReadedModel model = CSModelRegistry.INSTANCE.getModel(modelKey);
			CSModelBaker.BakedResult baked = CSModelBaker.bakeAndListRootNames(model, textureWidth, textureHeight);
			return new CachedLayer(baked.layer(), List.copyOf(baked.rootNames()));
		} catch (RuntimeException e) {
			CraftStudioApi.getLogger().error("CraftStudioModelHelper: failed to bake model '{}'; rendering an empty fallback: {}", modelKey, e.getMessage());
			return new CachedLayer(LayerDefinition.create(new net.minecraft.client.model.geom.builders.MeshDefinition(), textureWidth, textureHeight), List.of());
		}
	}

     







    public static void renderAllRoots(ModelPart bakedRoot, ResourceLocation modelKey, int textureWidth, int textureHeight,
            com.mojang.blaze3d.vertex.PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer buffer, int packedLight, int packedOverlay) {
		for (String rootName : getCachedLayer(modelKey, textureWidth, textureHeight).rootNames())
            bakedRoot.getChild(rootName).render(poseStack, buffer, packedLight, packedOverlay);
    }

     






    public static void playAnimation(ModelPart root, ResourceLocation modelKey, ResourceLocation animKey, float rawTime) {
        try {
            CSReadedModel model = CSModelRegistry.INSTANCE.getModel(modelKey);
            CSReadedAnim anim = CSModelRegistry.INSTANCE.getAnim(animKey);
            float phase = CSAnimationApplier.computePhase(anim, rawTime);
            CSAnimationApplier.apply(root, anim, model, phase);
        } catch (RuntimeException e) {
            CraftStudioApi.getLogger().error("CraftStudioModelHelper: failed to play animation '{}' on model '{}': {}", animKey, modelKey, e.getMessage());
        }
    }
}
