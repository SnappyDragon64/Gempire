package com.gempire.blocks;

import com.gempire.entities.bases.EntityGem;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DestabWallBlock extends HorizontalDirectionalBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public EntityGem gem;
    public VoxelShape shapeEW = Block.box(0, 0, 7, 16, 16, 9);
    public VoxelShape shapeNS = Block.box(7, 0, 0, 9, 16, 16);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    protected static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter((p_52346_) -> {
        return p_52346_.getKey().getAxis().isHorizontal();
    }).collect(Util.toMap());

    public DestabWallBlock(Properties p_54120_) {
        super(p_54120_);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, Boolean.FALSE).setValue(FACING, Direction.NORTH));
    }

    public boolean skipRendering(BlockState p_54207_, BlockState p_54208_, Direction p_54209_) {
        if (p_54208_.is(this)) {
            if (!p_54209_.getAxis().isHorizontal()) {
                return true;
            }

            if (p_54207_.getValue(PROPERTY_BY_DIRECTION.get(p_54209_)) && p_54208_.getValue(PROPERTY_BY_DIRECTION.get(p_54209_.getOpposite()))) {
                return true;
            }
        }

        return super.skipRendering(p_54207_, p_54208_, p_54209_);
    }

    private void updateNeighbours(BlockState state, Level level, BlockPos pos) {
        level.updateNeighborsAt(pos, this);
        level.updateNeighborsAt(pos.below(), this);
        level.updateNeighborsAt(pos.above(), this);
        level.updateNeighborsAt(pos.north(), this);
        level.updateNeighborsAt(pos.east(), this);
        level.updateNeighborsAt(pos.south(), this);
        level.updateNeighborsAt(pos.west(), this);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_51101_) {
        p_51101_.add(POWERED, FACING);
    }

    public boolean isSignalSource(BlockState p_51114_) {
        return true;
    }

    public int getSignal(BlockState p_51078_, BlockGetter p_51079_, BlockPos p_51080_, Direction p_51081_) {
        return p_51078_.getValue(POWERED) ? 15 : 0;
    }

    public int getDirectSignal(BlockState p_51109_, BlockGetter p_51110_, BlockPos p_51111_, Direction p_51112_) {
        return p_51109_.getValue(POWERED) ? 15 : 0;
    }

    public BlockState getStateForPlacement(BlockPlaceContext p_51377_) {
        if(p_51377_.getClickedFace().equals(Direction.UP)) {
            return this.defaultBlockState().setValue(FACING, p_51377_.getHorizontalDirection().getOpposite());
        }
        else
        {
            return null;
        }
    }

    public void onRemove(BlockState p_51095_, Level p_51096_, BlockPos p_51097_, BlockState p_51098_, boolean p_51099_) {
        if (!p_51099_ && !p_51095_.is(p_51098_.getBlock())) {
            if (p_51095_.getValue(POWERED)) {
                this.updateNeighbours(p_51095_, p_51096_, p_51097_);
            }

            super.onRemove(p_51095_, p_51096_, p_51097_, p_51098_, p_51099_);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof EntityGem) {
            gem = (EntityGem) entity;
        }
    }

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext context) {
        if (FACING.getValue("facing").equals(Optional.of(Direction.SOUTH)) || FACING.getValue("facing").equals(Optional.of(Direction.NORTH))) {
            return shapeNS;
        } else {
            return shapeEW;
        }
    }

    @Override
    public void tick(BlockState p_222945_, ServerLevel p_222946_, BlockPos pos, RandomSource p_222948_) {
        if (gem != null) {
            BlockPos gemPos = new BlockPos(gem.getX(), gem.getY(), gem.getZ());
            if (gemPos.equals(pos)) {
                gem.hurt(DamageSource.MAGIC, 2);
            }
        }
    }
}
