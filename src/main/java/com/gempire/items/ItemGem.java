package com.gempire.items;

import com.gempire.entities.bases.EntityGem;
import com.gempire.entities.gems.EntityZircon;
import com.gempire.entities.gems.starter.EntityPebble;
import com.gempire.events.GemFormEvent;
import com.gempire.init.AddonHandler;
import com.gempire.init.ModEnchants;
import com.gempire.init.ModEntities;
import com.gempire.init.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class ItemGem extends Item {
    public String ID = "";

    private static final String TAG_CRACKED = "Cracked";
    int coundownMax = 600;
    int countdown = 600;

    Random rand = new Random();
    public boolean doEffect = false;
    public EntityGem assigned_gem;
    public EntityGem gemToAssign;
    public boolean livingEntityHit = false;
    public boolean isAssigned = false;

    public ItemGem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || stack.getItem() == ModItems.NACRE_GEM.get();
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        System.out.println("entity interact");
        if (entity instanceof EntityGem) {
            if (((EntityGem) entity).assignedGem == null) {
                if (((EntityGem) entity).isOwner(player)) {
                    if (player.isCrouching()) {
                        livingEntityHit = true;
                        System.out.println("gem interact");
                        gemToAssign = null;
                        isAssigned = true;
                    } else {
                        livingEntityHit = true;
                        System.out.println("gem interact");
                        gemToAssign = (EntityGem) entity;
                        isAssigned = false;
                    }
                }
            }
        }
        return super.interactLivingEntity(stack, player, entity, hand);
    }


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level worldIn, @NotNull Player playerIn, @NotNull InteractionHand handIn) {
        boolean spawned = false;
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (!worldIn.isClientSide) {
            System.out.println("check tags");
            if (!getCracked(stack)) {
                System.out.println("through");
                if (!livingEntityHit) {
                    ItemStack itemstack = playerIn.getItemInHand(handIn);
                    BlockHitResult raytraceresult = getPlayerPOVHitResult(worldIn, playerIn, ClipContext.Fluid.NONE);
                    if (raytraceresult.getType() == HitResult.Type.MISS) {
                        return super.use(worldIn, playerIn, handIn);
                    } else {
                        if (raytraceresult.getType() == HitResult.Type.BLOCK) {
                            BlockPos blockpos = raytraceresult.getBlockPos();
                            Direction direction = raytraceresult.getDirection();
                            if (!worldIn.mayInteract(playerIn, blockpos) || !playerIn.mayUseItemAt(blockpos.relative(direction), direction, itemstack)) {
                                return super.use(worldIn, playerIn, handIn);
                            }

                            if (worldIn.mayInteract(playerIn, blockpos) && playerIn.mayUseItemAt(blockpos, raytraceresult.getDirection(), itemstack)) {
                                spawned = this.formGem(worldIn, playerIn, blockpos, itemstack, null);
                            }
                        }
                        //Problem with the claiming of gems ??
                        if (!playerIn.isCreative() && spawned) {
                            playerIn.getMainHandItem().shrink(1);
                        }
                    }
                } else {
                    if (!gemToAssign.isDeadOrDying()) {
                        if (isAssigned) {
                            playerIn.sendSystemMessage(Component.translatable("This Gem is no longer assigned to " + assigned_gem.getName().getString() + " " + assigned_gem.getFacetAndCut()));
                            livingEntityHit = false;
                            assigned_gem = null;
                        } else {
                            playerIn.sendSystemMessage(Component.translatable("This Gem was assigned to " + gemToAssign.getName().getString() + " " + gemToAssign.getFacetAndCut()));
                            livingEntityHit = false;
                            System.out.println("ToAssign to assigned");
                            assigned_gem = gemToAssign;
                            System.out.println(assigned_gem);
                            System.out.println(gemToAssign);
                        }
                    }
                }
            }
        }
        return super.use(worldIn, playerIn, handIn);
    }
    public boolean checkTags(ItemStack itemStack)
    {
        CompoundTag tag = itemStack.getTag();
        return tag != null;
    }
    //(?i) means case sensitive

    public boolean getCracked(ItemStack stack) {
        System.out.println("checking");
        if (checkTags(stack)) {
            System.out.println("checked");
            System.out.println(stack.getTag().getBoolean("cracked"));
            return stack.getTag().getBoolean("cracked");
        } else {
            System.out.println("check failed");
            return false;
        }
    }
    public boolean formGem(Level world, @Nullable Player player, BlockPos pos, ItemStack stack, @Nullable ItemEntity item) {
        if (!world.isClientSide) {
                        System.out.println("form event");
                        System.out.println(assigned_gem);
                        System.out.println(gemToAssign);
                        RegistryObject<EntityType<EntityPebble>> gemm = ModEntities.PEBBLE;
                        String skinColorVariant = "";
                        EntityGem gem = gemm.get().create(world);
                        String namee = "";
                        boolean dying = false;
                        List<EntityGem> list;
                        if (player == null) {
                            dying = false;
                        } else {
                            list = player.level.getEntitiesOfClass(EntityGem.class, player.getBoundingBox().inflate(4.0D, 4.0D, 4.0D));
                            for (EntityGem gemmy : list) {
                                if (gemmy.isDeadOrDying())
                                    dying = true;
                                System.out.println(gemmy.getUUID());
                            }
                        }
                        if (!dying) {
                            if (Objects.equals(this.ID, "")) {
                                namee = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this)).toString().replaceAll("gempire", "").replaceAll("gem", "").replaceAll(":", "").replaceAll(" ", "");
                            } else {
                                namee = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this)).toString().replaceAll(this.ID, "").replaceAll("gem", "").replaceAll(":", "").replaceAll(" ", "");
                            }

                            //This whole section here checks for variations in color so it can spawn the correct type of gem

                            String[] ainmneacha = namee.split("_");
                            boolean nullFlag = false;
                            int idx = 0;
                            for (int i = 0; i < ainmneacha.length; i++) {
                                if (ainmneacha[i].isEmpty()) {
                                    nullFlag = true;
                                    idx = i;
                                }
                            }
                            if (nullFlag) ainmneacha = ArrayUtils.remove(ainmneacha, idx);
                            namee = ainmneacha[0];
                            if (ainmneacha.length > 1) skinColorVariant = ainmneacha[1];
                            for (String s : ainmneacha) {
                                System.out.println(s);
                            }

                            //End of check and set

                            try {
                                if (Objects.equals(this.ID, "")) {
                                    gemm = (RegistryObject<EntityType<EntityPebble>>) ModEntities.class.getField(namee.toUpperCase()).get(null);
                                } else {
                                    gemm = (RegistryObject<EntityType<EntityPebble>>) AddonHandler.ADDON_ENTITY_REGISTRIES.get(this.ID).getField(namee.toUpperCase()).get(null);
                                }
                                gem = gemm.get().create(world);
                                assert gem != null;
                                gem.setUUID(Mth.createInsecureUUID(world.random));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                if (item != null) {
                                    assert gem != null;
                                    gem.spawnGem = item;
                                }
                                assert stack.getTag() != null;
                                assert gem != null;
                                gem.load(stack.getTag());
                            } catch (Exception e) {
                                if (ainmneacha.length > 1) {
                                    assert gem != null;
                                    gem.setSkinVariantOnInitialSpawn = false;
                                    gem.initalSkinVariant = Integer.parseInt(skinColorVariant);
                                }
                                if (player != null) {
                                    assert gem != null;
                                    gem.finalizeSpawn((ServerLevelAccessor) world, world.getCurrentDifficultyAt(player.blockPosition()), MobSpawnType.TRIGGERED, null, null);
                                    gem.addOwner(player.getUUID());
                                    if (gem.MASTER_OWNER == null) {
                                        gem.addMasterOwner(player.getUUID());
                                    }
                                    gem.FOLLOW_ID = player.getUUID();
                                    gem.setMovementType((byte) 2);
                                } else {
                                    if (item != null) {
                                        gem.spawnGem = item;
                                    }
                                    assert gem != null;
                                    assert item != null;
                                    gem.finalizeSpawn((ServerLevelAccessor) world, world.getCurrentDifficultyAt(item.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
                                }
                            }
                            if (isAssigned) {
                                gem.ASSIGNED_ID = UUID.randomUUID();
                            }
                            if (gem instanceof EntityZircon) {
                                if (!((EntityZircon) gem).getEnchantPageDefined()) {
                                    ((EntityZircon) gem).setEnchantPage(RandomSource.create().nextInt(ModEnchants.VANILLA_ENCHANTMENTS.size()));
                                    ((EntityZircon) gem).setEnchantPageDefined(true);
                                }
                            }
                            System.out.println(assigned_gem);
                            System.out.println(gemToAssign);
                            if (gem.MASTER_OWNER == null) {
                                gem.MASTER_OWNER = player.getUUID();
                            }
                            gem.registerRecipes();
                            gem.setPos(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
                            gem.setHealth(gem.getMaxHealth());
                            gem.clearFire();
                            gem.removeAllEffects();
                            gem.setDeltaMovement(0, 0, 0);
                            gem.fallDistance = 0;
                            gem.setCracked(stack.getOrCreateTag().getBoolean("cracked"));
                            gem.setAssignedGem(assigned_gem);
                            System.out.println(getAssigned_gem());
                            GemFormEvent event = new GemFormEvent(gem, gem.blockPosition());
                            MinecraftForge.EVENT_BUS.post(event);
                            world.addFreshEntity(gem);
                            System.out.println(gem.getGemPlacementE());
                            System.out.println(gem.getOutfitVariant() + " and " + gem.getInsigniaVariant());
                            return true;
                    }


        }
        return false;
    }

    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> p_40553_, TooltipFlag p_40554_) {
        if (Screen.hasShiftDown()) {
            if (checkTags(itemStack)) {
                p_40553_.add(Component.translatable(itemStack.getTag().getString("name")).withStyle(ChatFormatting.GRAY));
            }
            if (itemStack.getOrCreateTag().getBoolean("cracked")) {
                p_40553_.add(Component.translatable("Cracked").withStyle(ChatFormatting.RED));
            }
            if (this.gemToAssign != null) {
                p_40553_.add(Component.translatable( "Assigned to " + assigned_gem.getName().getString() + " " + assigned_gem.getFacetAndCut()));
            }
        } else {
            if (checkTags(itemStack)) {
                p_40553_.add(Component.translatable(itemStack.getTag().getString("name")).withStyle(ChatFormatting.GRAY));
            }
            if (itemStack.getOrCreateTag().getBoolean("cracked")) {
                p_40553_.add(Component.translatable("Cracked").withStyle(ChatFormatting.RED));
            }
            p_40553_.add(Component.translatable("Hold Shift for more info").withStyle(ChatFormatting.GOLD));

        }
    }

    public static void saveData(ItemStack stack, EntityGem gem, boolean cracked) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("name", gem.getName().getString());
        tag.putBoolean("cracked", cracked);
        tag.putBoolean("prime", gem.isPrimary());
        tag.putBoolean("defective", gem.isDefective());
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        this.Countdown(stack, entity);
        return super.onEntityItemUpdate(stack, entity);
    }

    public void clearData(ItemStack stack) {
        stack.setTag(new CompoundTag());
    }

    public void Countdown(ItemStack stack, ItemEntity entity){
        if(this.countdown > 0){
            if(this.countdown < (int)Math.floor(this.coundownMax / 6)){
                this.doEffect = true;
                float f = (this.rand.nextFloat() - 0.5F) * 2.0F;
                float f1 = (this.rand.nextFloat() - 0.5F) * 2.0F;
                float f2 = (this.rand.nextFloat() - 0.5F) * 2.0F;
                entity.level.addParticle(ParticleTypes.EXPLOSION, entity.getX() + (double)f, entity.getY() + 2.0D + (double)f1, entity.getZ() + (double)f2, 0.0D, 0.0D, 0.0D);
            }
            this.countdown--;
        }
        else{
            this.formGem(entity.level, null, entity.blockPosition(), stack, entity);
            this.countdown = this.coundownMax;
        }
    }

    public EntityGem getAssigned_gem() {
        return assigned_gem;
    }
}
