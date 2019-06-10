package com.github.iunius118.tolaserblade.client.model;

import com.github.iunius118.tolaserblade.ToLaserBladeConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.client.model.BasicState;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.OBJBakedModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.Models;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import java.util.*;

@SuppressWarnings("deprecation") // for ItemCameraTransforms
public class LaserBladeModel implements IBakedModel {

    public IBakedModel bakedOBJModel;
    public IBakedModel bakedJSONModel;

    public ItemStack itemStack;
    public World world;
    public LivingEntity entity;

    public TransformType cameraTransformType = TransformType.NONE;

    public BlockState state;
    public Direction side;
    public Random rand;

    public Map<String, List<BakedQuad>> mapQuads = new HashMap<String, List<BakedQuad>>();
    public String[] partNames = {"Hilt", "Hilt_bright", "Blade_core", "Blade_halo_1", "Blade_halo_2"};

    public LaserBladeModel(IBakedModel bakedOBJModelIn, IBakedModel bakedJSONModelIn) {
        this(bakedOBJModelIn, bakedJSONModelIn, false);
    }

    public LaserBladeModel(IBakedModel bakedOBJModelIn, IBakedModel bakedJSONModelIn, boolean isInitialized) {
        bakedOBJModel = bakedOBJModelIn;
        bakedJSONModel = bakedJSONModelIn;

        if (!isInitialized) {
            // Separate Quads to each parts by OBJ Group.
            for (String partName : partNames) {
                mapQuads.put(partName, getPartQuads(bakedOBJModelIn, ImmutableList.of(partName)));
            }
        }
    }

    public List<BakedQuad> getPartQuads(IBakedModel bakedModelIn, final List<String> visibleGroups) {
        List<BakedQuad> quads = Collections.emptyList();

        if (bakedModelIn instanceof OBJBakedModel) {
            try {
                OBJModel obj = ((OBJBakedModel) bakedModelIn).getModel();

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

                quads = bakedModel.getQuads(null, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return quads;
    }

    public void handleItemState(ItemStack itemStackIn, World worldIn, LivingEntity entityLivingBaseIn) {
        itemStack = itemStackIn;
        world = worldIn;
        entity = entityLivingBaseIn;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState blockStateIn, Direction direction, Random randIn) {
        if (side == null) {
            state = blockStateIn;
            side = direction;
            rand = randIn;

            return bakedOBJModel.getQuads(null, null, randIn);
        }

        return Collections.emptyList();
    }

    public List<BakedQuad> getQuadsByName(String name) {
        if (mapQuads.containsKey(name)) {
            return mapQuads.get(name);
        }

        return Collections.emptyList();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return bakedJSONModel.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return bakedJSONModel.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return new ItemOverrideList() {

            @Override
            public IBakedModel getModelWithOverrides(IBakedModel originalModel, ItemStack stack, World world, LivingEntity entity) {
                // Copy LaserBladeModel object and handle ItemStack.
                if (originalModel instanceof LaserBladeModel) {
                    LaserBladeModel model = (LaserBladeModel) originalModel;
                    model.handleItemState(stack, world, entity);
                    return model;
                }

                return originalModel;
            }

        };
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType transformTypeIn) {
        Matrix4f matrix;

        // Get transformation matrix from JSON item model.
        matrix = bakedJSONModel.handlePerspective(transformTypeIn).getValue();

        cameraTransformType = transformTypeIn;

        if (ToLaserBladeConfig.CLIENT.isEnabledLaserBlade3DModel.get()) {
            return Pair.of(this, matrix);
        } else {
            return Pair.of(this.bakedJSONModel, matrix);
        }
    }

}
