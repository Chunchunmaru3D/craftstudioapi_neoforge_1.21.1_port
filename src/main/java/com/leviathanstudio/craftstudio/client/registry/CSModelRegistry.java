package com.leviathanstudio.craftstudio.client.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.leviathanstudio.craftstudio.CraftStudioApi;
import com.leviathanstudio.craftstudio.client.exception.CSResourceNotRegisteredException;
import com.leviathanstudio.craftstudio.client.json.CSJsonReader;
import com.leviathanstudio.craftstudio.client.json.CSReadedAnim;
import com.leviathanstudio.craftstudio.client.json.CSReadedModel;
import com.leviathanstudio.craftstudio.client.model.CraftStudioModelHelper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

 




















public class CSModelRegistry extends SimplePreparableReloadListener<CSModelRegistry.LoadedData>
{
    public static final CSModelRegistry INSTANCE = new CSModelRegistry();

    private static final String MODELS_PATH      = "craftstudio/models";
    private static final String ANIMATIONS_PATH  = "craftstudio/animations";
    private static final String MODEL_EXT        = ".csjsmodel";
    private static final String ANIM_EXT         = ".csjsmodelanim";

    private volatile Map<ResourceLocation, CSReadedModel> models = Map.of();
    private volatile Map<ResourceLocation, CSReadedAnim>  anims  = Map.of();

    private CSModelRegistry() {}

    public record LoadedData(Map<ResourceLocation, CSReadedModel> models, Map<ResourceLocation, CSReadedAnim> anims) {}

    @Override
    protected LoadedData prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, CSReadedModel> newModels = new HashMap<>();
        Map<ResourceLocation, CSReadedAnim> newAnims = new HashMap<>();

        for (Entry<ResourceLocation, Resource> entry : resourceManager.listResources(MODELS_PATH, loc -> loc.getPath().endsWith(MODEL_EXT)).entrySet()) {
            ResourceLocation fileLoc = entry.getKey();
            ResourceLocation key = toKey(fileLoc, MODELS_PATH, MODEL_EXT);
            try {
                CSReadedModel model = new CSJsonReader(resourceManager, fileLoc).readModel();
                newModels.put(key, model);
            } catch (RuntimeException e) {
                CraftStudioApi.getLogger().error("CSModelRegistry: failed to parse model {}: {}", fileLoc, e.getMessage());
            }
        }

        for (Entry<ResourceLocation, Resource> entry : resourceManager.listResources(ANIMATIONS_PATH, loc -> loc.getPath().endsWith(ANIM_EXT)).entrySet()) {
            ResourceLocation fileLoc = entry.getKey();
            ResourceLocation key = toKey(fileLoc, ANIMATIONS_PATH, ANIM_EXT);
            try {
                CSReadedAnim anim = new CSJsonReader(resourceManager, fileLoc).readAnim();
                newAnims.put(key, anim);
            } catch (RuntimeException e) {
                CraftStudioApi.getLogger().error("CSModelRegistry: failed to parse animation {}: {}", fileLoc, e.getMessage());
            }
        }

        CraftStudioApi.getLogger().info("CSModelRegistry: loaded {} CraftStudio models and {} animations", newModels.size(), newAnims.size());
        return new LoadedData(newModels, newAnims);
    }

    @Override
    protected void apply(LoadedData data, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.models = data.models();
        this.anims = data.anims();
		 
		 
		CraftStudioModelHelper.clearBakedLayers();
    }

     



    public CSReadedModel getModel(ResourceLocation key) {
        CSReadedModel model = this.models.get(key);
        if (model != null)
            return model;

         
         
         
         
         
         
         
         
         
         
        CSReadedModel loaded = loadModelOnDemand(key);
        if (loaded != null)
            return loaded;

        throw new CSResourceNotRegisteredException(key.toString());
    }

     



    public CSReadedAnim getAnim(ResourceLocation key) {
        CSReadedAnim anim = this.anims.get(key);
        if (anim != null)
            return anim;

        CSReadedAnim loaded = loadAnimOnDemand(key);
        if (loaded != null)
            return loaded;

        throw new CSResourceNotRegisteredException(key.toString());
    }

    private synchronized CSReadedModel loadModelOnDemand(ResourceLocation key) {
         
        CSReadedModel cached = this.models.get(key);
        if (cached != null)
            return cached;

        ResourceManager resourceManager = currentResourceManager();
        if (resourceManager == null)
            return null;

        ResourceLocation fileLoc = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), MODELS_PATH + "/" + key.getPath() + MODEL_EXT);
        try {
            CSReadedModel model = new CSJsonReader(resourceManager, fileLoc).readModel();
            Map<ResourceLocation, CSReadedModel> updated = new HashMap<>(this.models);
            updated.put(key, model);
            this.models = updated;
            CraftStudioApi.getLogger().warn("CSModelRegistry: loaded '{}' on demand before this registry's reload listener completed", key);
            return model;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private synchronized CSReadedAnim loadAnimOnDemand(ResourceLocation key) {
        CSReadedAnim cached = this.anims.get(key);
        if (cached != null)
            return cached;

        ResourceManager resourceManager = currentResourceManager();
        if (resourceManager == null)
            return null;

        ResourceLocation fileLoc = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), ANIMATIONS_PATH + "/" + key.getPath() + ANIM_EXT);
        try {
            CSReadedAnim anim = new CSJsonReader(resourceManager, fileLoc).readAnim();
            Map<ResourceLocation, CSReadedAnim> updated = new HashMap<>(this.anims);
            updated.put(key, anim);
            this.anims = updated;
            CraftStudioApi.getLogger().warn("CSModelRegistry: loaded '{}' on demand before this registry's reload listener completed", key);
            return anim;
        } catch (RuntimeException e) {
            return null;
        }
    }

     





    private static ResourceManager currentResourceManager() {
        try {
            return net.minecraft.client.Minecraft.getInstance().getResourceManager();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static ResourceLocation toKey(ResourceLocation fileLoc, String basePath, String ext) {
        String path = fileLoc.getPath();
         
        String relPath = path.substring(basePath.length() + 1, path.length() - ext.length());
        return ResourceLocation.fromNamespaceAndPath(fileLoc.getNamespace(), relPath);
    }
}
