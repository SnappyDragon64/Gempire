package com.gempire.client.entity.render.layers;

import com.gempire.Gempire;
import com.gempire.client.entity.model.ModelGem;
import com.gempire.entities.bases.EntityGem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public class OutfitLayer<E extends EntityGem, M extends ModelGem<E>> extends GempireLayer<EntityGem, ModelGem<EntityGem>> {
    private RenderLayerParent<EntityGem, ModelGem<EntityGem>> gemRenderer;

    public OutfitLayer(RenderLayerParent<EntityGem, ModelGem<EntityGem>> entityRendererIn) {
        super(entityRendererIn);
        this.gemRenderer = entityRendererIn;
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, EntityGem gem, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        float[] outfitColors = Sheep.getColorArray(DyeColor.byId(gem.getOutfitColor()));
        VertexConsumer builder = null;
        if (gem.getRebelled()) {
            if (gem.hasOutfitPlacementVariant()) {
                for (int i : gem.outfitPlacementVariants()) {
                    if (i == gem.getGemPlacement()) {
                        builder = bufferIn.getBuffer(RenderType.entityTranslucent(new ResourceLocation(gem.getModID() + ":textures/entity/" + this.getName(gem).toLowerCase() + "/outfits/outfit_" + gem.getGemPlacement() + "_0.png")));
                        break;
                    } else {
                        builder = bufferIn.getBuffer(RenderType.entityTranslucent(new ResourceLocation(gem.getModID() + ":textures/entity/" + this.getName(gem).toLowerCase() + "/outfits/outfit_" + gem.getRebelOutfitVariant() + ".png")));
                    }
                }
            } else {
                builder = bufferIn.getBuffer(RenderType.entityTranslucent(new ResourceLocation(gem.getModID() + ":textures/entity/" + this.getName(gem).toLowerCase() + "/outfits/outfit_" + gem.getRebelOutfitVariant() + ".png")));
            }
        }
        if (!gem.getRebelled()) {
            if (gem.hasOutfitPlacementVariant()) {
                for (int i : gem.outfitPlacementVariants()) {
                    if (i == gem.getGemPlacement()) {
                        builder = bufferIn.getBuffer(RenderType.entityTranslucent(new ResourceLocation(gem.getModID() + ":textures/entity/" + this.getName(gem).toLowerCase() + "/outfits/outfit_" + gem.getGemPlacement() + "_0.png")));
                        break;
                    } else {
                        builder = bufferIn.getBuffer(RenderType.entityTranslucent(new ResourceLocation(gem.getModID() + ":textures/entity/" + this.getName(gem).toLowerCase() + "/outfits/outfit_" + gem.getOutfitVariant() + ".png")));
                    }
                }
            } else {
                builder = bufferIn.getBuffer(RenderType.entityTranslucent(new ResourceLocation(gem.getModID() + ":textures/entity/" + this.getName(gem).toLowerCase() + "/outfits/outfit_" + gem.getOutfitVariant() + ".png")));
            }
        }
        this.getParentModel().setupAnim(gem, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.getParentModel().renderToBuffer(matrixStackIn, builder, packedLightIn, OverlayTexture.NO_OVERLAY, outfitColors[0], outfitColors[1], outfitColors[2], 1.0F);
        /*if(gem instanceof EntityStarterGem){
            ModelStarterGem model = ((ModelStarterGem)this.getEntityModel());
            model.
        }*/
    }
}
