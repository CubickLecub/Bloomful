package com.pugz.bloomful.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

public class WisteriaLeavesBlock extends Block implements net.minecraftforge.common.IShearable {
    public static final IntegerProperty DISTANCE = IntegerProperty.create("distance", 1, 8);
    public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;
    protected static boolean renderTranslucent;

    public WisteriaLeavesBlock(Block.Properties properties) {
        super(properties);
        setDefaultState(stateContainer.getBaseState().with(DISTANCE, 8).with(PERSISTENT, false));
    }

    @Override
    public boolean ticksRandomly(BlockState state) {
        return state.get(DISTANCE) == 8 && !state.get(PERSISTENT);
    }

    @Override
    public void randomTick(BlockState state, World worldIn, BlockPos pos, Random random) {
        if (!state.get(PERSISTENT) && state.get(DISTANCE) == 8) {
            spawnDrops(state, worldIn, pos);
            worldIn.removeBlock(pos, false);
        }
    }

    @Override
    public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
        worldIn.setBlockState(pos, updateDistance(state, worldIn, pos), 3);
    }

    public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return 1;
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        int i = getDistance(facingState) + 1;
        if (i != 1 || stateIn.get(DISTANCE) != i) {
            worldIn.getPendingBlockTicks().scheduleTick(currentPos, this, 1);
        }
        return stateIn;
    }

    private static BlockState updateDistance(BlockState state, IWorld world, BlockPos pos) {
        int i = 8;
        try (BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain()) {
            for(Direction direction : Direction.values()) {
                blockpos$pooledmutableblockpos.setPos(pos).move(direction);
                i = Math.min(i, getDistance(world.getBlockState(blockpos$pooledmutableblockpos)) + 1);
                if (i == 1) {
                    break;
                }
            }
        }
        return state.with(DISTANCE, i);
    }

    private static int getDistance(BlockState neighbor) {
        if (BlockTags.LOGS.contains(neighbor.getBlock())) {
            return 0;
        } else {
            if (neighbor.getBlock() instanceof WisteriaLeavesBlock) return neighbor.get(DISTANCE);
            if (neighbor.getBlock() instanceof LeavesBlock) return neighbor.get(LeavesBlock.DISTANCE);
            else return 8;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (worldIn.isRainingAt(pos.up())) {
            if (rand.nextInt(15) == 1) {
                BlockPos blockpos = pos.down();
                BlockState blockstate = worldIn.getBlockState(blockpos);
                if (!blockstate.isSolid() || !blockstate.func_224755_d(worldIn, blockpos, Direction.UP)) {
                    double d0 = (double)((float)pos.getX() + rand.nextFloat());
                    double d1 = (double)pos.getY() - 0.05D;
                    double d2 = (double)((float)pos.getZ() + rand.nextFloat());
                    worldIn.addParticle(ParticleTypes.DRIPPING_WATER, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    public boolean isSolid(BlockState state) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public static void setRenderTranslucent(boolean fancy) {
        renderTranslucent = fancy;
    }

    public BlockRenderLayer getRenderLayer() {
        //return renderTranslucent ? BlockRenderLayer.CUTOUT_MIPPED : BlockRenderLayer.SOLID;
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    public boolean causesSuffocation(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return false;
    }

    public boolean canEntitySpawn(BlockState state, IBlockReader worldIn, BlockPos pos, EntityType<?> type) {
        return type == EntityType.OCELOT || type == EntityType.PARROT;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE, PERSISTENT);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return updateDistance(getDefaultState().with(PERSISTENT, true), context.getWorld(), context.getPos());
    }
}