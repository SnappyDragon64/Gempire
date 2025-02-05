package com.gempire.commands.impl;

import com.gempire.commands.CommandBase;
import com.gempire.entities.bases.EntityGem;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.UUID;

public class CommandGempireLocate /*extends CommandBase*/ {
    public static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.gempire.nounderstand"));
/*
    public CommandGempireLocate(String name, int permissionLevel, boolean enabled) {
        super(name, permissionLevel, enabled);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> setExecution() {
        for (StructureFeature<?> structureFeature : net.minecraftforge.registries.ForgeRegistries.STRUCTURE_FEATURES) {
            String name = structureFeature.getRegistryName().toString().replace("minecraft:", "");
            this.builder = this.builder.then(Commands.literal(name)
                    .executes(source -> execute(source.getSource(), structureFeature)));
        }
        return this.builder;
    }

    public int execute(CommandSourceStack source, StructureFeature<?> structure) throws CommandSyntaxException {
        BlockPos pos = new BlockPos(source.getPosition().x, source.getPosition().y, source.getPosition().z);
        AABB aabb = source.getPlayerOrException().getBoundingBox().inflate(12.0D);
        EntityGem nephrite = null;
        List<EntityGem> gems = source.getLevel().getEntitiesOfClass(EntityGem.class, aabb);
        for(EntityGem gem : gems){
            if(gem.canLocateStructures()){
                if(gem.isOwner(source.getPlayerOrException())){
                    if(!gem.isOnStructureCooldown()){
                        nephrite = gem;
                    }
                }
            }
        }
        if(nephrite != null){
            nephrite.runFindCommand(source.getPlayerOrException(), structure, null, false);
            return Command.SINGLE_SUCCESS;
        }
        else{
            source.getPlayerOrException().sendMessage(new TranslatableComponent("commands.gempire.nounderstand"), UUID.randomUUID());
            throw CommandGempireLocate.FAILED_EXCEPTION.create();
        }
    }
    */
}
