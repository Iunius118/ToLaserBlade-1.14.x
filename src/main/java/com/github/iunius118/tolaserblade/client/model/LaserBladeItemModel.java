package com.github.iunius118.tolaserblade.client.model;

import com.github.iunius118.tolaserblade.ToLaserBlade;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.BasicState;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.Models;
import net.minecraftforge.common.model.TRSRTransformation;

import java.util.*;

public class LaserBladeItemModel {
    public static Map<Part, List<BakedQuad>> parts = Maps.newEnumMap(Part.class);

    public static void loadLaserBladeOBJModel(ModelLoader loader) {
        // Load model
        parts.clear();
        ResourceLocation modelLocation = new ResourceLocation(ToLaserBlade.MOD_ID, "item/laser_blade.obj");
        IUnbakedModel model = null;

        // Use legacy OBJLoader and OBJModel
        OBJLoader.INSTANCE.addDomain(ToLaserBlade.MOD_ID);
        model = ModelLoaderRegistry.getModelOrMissing(modelLocation);

        for (Part part : Part.values()) {
            parts.put(part, getQuadsByGroups(model, ImmutableList.of(part.getName())));
        }
    }

    private static List<BakedQuad> getQuadsByGroups(IUnbakedModel modelIn, final List<String> visibleGroups) {
        List<BakedQuad> quads = null;

        if (modelIn instanceof OBJModel) {
            try {
                OBJModel obj = (OBJModel)modelIn;

                // ModelState for handling visibility of each group.
                IModelState modelState = part -> {
                    if (part.isPresent()) {
                        UnmodifiableIterator<String> parts = Models
                                .getParts(part.get());

                        if (parts.hasNext()) {
                            String name = parts.next();

                            if (!parts.hasNext() && visibleGroups.contains(name)) {
                                // Return Absent for NOT invisible group.
                                return Optional.empty();
                            } else {
                                // Return Present for invisible group.
                                return Optional.of(TRSRTransformation.identity());
                            }
                        }
                    }

                    return Optional.empty();
                };

                // Bake model of visible groups.
                IBakedModel bakedModel = obj.bake(null, ModelLoader.defaultTextureGetter(), new BasicState(modelState, false), DefaultVertexFormats.ITEM);

                if (bakedModel != null) {
                    quads = bakedModel.getQuads(null, null, new Random(), EmptyModelData.INSTANCE);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (quads != null) {
            return quads;
        } else {
            return Collections.emptyList();
        }
    }

    public enum Part {
        // Names for legacy OBJModel
        HILT("hilt"),
        HILT_0_2("hilt_0_2"),
        HILT_NO_TINT("hilt_no_tint"),
        HILT_BRIGHT("hilt_bright"),
        BLADE_OUT_2("blade_out_2"),
        BLADE_MID_2("blade_mid_2"),
        BLADE_IN("blade_in_0_2"),
        BLADE_MID_0("blade_mid_0"),
        BLADE_OUT_0("blade_out_0"),
        BLADE_OUT_1("blade_out_1"),
        BLADE_IN_1("blade_in_1");

        private String name;

        Part(String nameIn) {
            name = nameIn;
        }

        public String getName() {
            return name;
        }

        public static Part find(String nameIn) {
            for(Part value : values()) {
                if(value.getName().equals(nameIn)) {
                    return value;
                }
            }

            return null;
        }
    }
}
