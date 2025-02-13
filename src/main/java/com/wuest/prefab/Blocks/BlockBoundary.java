package com.wuest.prefab.Blocks;

import com.wuest.prefab.Events.ModEventHandler;
import com.wuest.prefab.Gui.GuiLangKeys;
import com.wuest.prefab.ModRegistry;
import com.wuest.prefab.Prefab;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author WuestMan
 */
public class BlockBoundary extends Block {
    /**
     * The powered meta data property.
     */
    public static final BooleanProperty Powered = BooleanProperty.create("powered");

    public final ItemGroup itemGroup;

    /**
     * Initializes a new instance of the BlockBoundary class.
     *
     * @param name The name of the block to register.
     */
    public BlockBoundary(String name) {
        super(Block.Properties.create(Prefab.SeeThroughImmovable)
                .sound(SoundType.STONE)
                .hardnessAndResistance(0.6F));

        this.itemGroup = ItemGroup.BUILDING_BLOCKS;
        this.setDefaultState(this.stateContainer.getBaseState().with(Powered, false));

        ModRegistry.setBlockName(this, name);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockBoundary.Powered);
    }

    /**
     * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
     * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
     * @deprecated call via {@link IBlockState#getRenderType()} whenever possible. Implementing/overriding is fine.
     */
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        boolean powered = state.get(Powered);
        return powered ? BlockRenderType.MODEL : BlockRenderType.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        boolean powered = state.get(Powered);

        return powered ? VoxelShapes.fullCube() : VoxelShapes.empty();
    }

    /**
     * Called when a player removes a block. This is responsible for actually destroying the block, and the block is
     * intact at time of call. This is called regardless of whether the player can harvest the block or not.
     * <p>
     * Return true if the block is actually destroyed.
     * <p>
     * Note: When used in multi-player, this is called on both client and server sides!
     *
     * @param state       The current state.
     * @param world       The current world
     * @param player      The player damaging the block, may be null
     * @param pos         Block position in world
     * @param willHarvest True if Block.harvestBlock will be called after this, if the return in true. Can be useful to
     *                    delay the destruction of tile entities till after harvestBlock
     * @param fluid       The current fluid state at current position
     * @return True if the block is actually destroyed.
     */
    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, IFluidState fluid) {
        boolean returnValue = super.removedByPlayer(state, world, pos, player, willHarvest, fluid);

        ModEventHandler.RedstoneAffectedBlockPositions.remove(pos);

        boolean poweredSide = world.isBlockPowered(pos);

        if (poweredSide) {
            this.setNeighborGlassBlocksPoweredStatus(world, pos, false, 0, new ArrayList<>(), false);
        }

        return returnValue;
    }

    /**
     * Queries if this block should render in a given layer. ISmartBlockModel can use
     * {@link MinecraftForgeClient#getRenderLayer()} to alter their model based on layer.
     */
    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        boolean powered = state.get(Powered);

        return (layer == BlockRenderLayer.TRANSLUCENT && !powered) || (layer == BlockRenderLayer.SOLID && powered);
    }

    /**
     * Gets the {@link BlockState} to place
     *
     * @param context The {@link BlockItemUseContext}.
     * @return The state to be placed in the world
     */
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        /*
         * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
         * BlockState
         */
        boolean poweredSide = context.getWorld().isBlockPowered(context.getPos());

        if (poweredSide) {
            this.setNeighborGlassBlocksPoweredStatus(context.getWorld(), context.getPos(), true, 0, new ArrayList<>(), false);
        }

        return this.getDefaultState().with(Powered, poweredSide);
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos p_189540_5_, boolean p_220069_6_) {
        if (!worldIn.isRemote) {
            // Only worry about powering blocks.
            if (blockIn.getDefaultState().canProvidePower()) {
                boolean poweredSide = worldIn.isBlockPowered(pos);

                this.setNeighborGlassBlocksPoweredStatus(worldIn, pos, poweredSide, 0, new ArrayList<>(), true);
            }
        }
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        super.addInformation(stack, worldIn, tooltip, advanced);

        boolean advancedKeyDown = Screen.hasShiftDown();

        if (!advancedKeyDown) {
            tooltip.add(new StringTextComponent(GuiLangKeys.translateString(GuiLangKeys.SHIFT_TOOLTIP)));
        } else {
            tooltip.add(new StringTextComponent(GuiLangKeys.translateString(GuiLangKeys.BOUNDARY_TOOLTIP)));
        }
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    @Override
    public boolean isSolid(BlockState state) {
        return false;
    }

    @Override
    public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos) {
        boolean powered = state.get(Powered);

        if (powered && state.isOpaqueCube(worldIn, pos)) {
            return worldIn.getMaxLightLevel();
        } else {
            return state.propagatesSkylightDown(worldIn, pos) ? 0 : 1;
        }
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        boolean powered = state.get(Powered);

        return !powered || (!isOpaque(state.getShape(reader, pos)) && state.getFluidState().isEmpty());
    }

    /**
     * Get's the collision shape.
     *
     * @param state   The block state.
     * @param worldIn The world object.
     * @param pos     The block Position.
     * @param context The selection context.
     * @return Returns a shape.
     */
    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.fullCube();
    }

    @Deprecated
    @Override
    public VoxelShape getRaytraceShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        if (!state.get(Powered)) {
            return VoxelShapes.empty();
        } else {
            return VoxelShapes.fullCube();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
        return !state.get(Powered);
    }

    /**
     * Sets the neighbor powered status
     *
     * @param world            The world where the block resides.
     * @param pos              The position of the block.
     * @param isPowered        Determines if the block is powered.
     * @param cascadeCount     How many times this has been cascaded.
     * @param cascadedBlockPos All of the block positions which have been cascaded too.
     * @param setCurrentBlock  Determines if the current block should be set.
     */
    protected void setNeighborGlassBlocksPoweredStatus(World world, BlockPos pos, boolean isPowered, int cascadeCount, ArrayList<BlockPos> cascadedBlockPos,
                                                       boolean setCurrentBlock) {
        cascadeCount++;

        if (cascadeCount > 100) {
            return;
        }

        if (setCurrentBlock) {
            BlockState state = world.getBlockState(pos);
            world.setBlockState(pos, state.with(Powered, isPowered));
        }

        cascadedBlockPos.add(pos);

        for (Direction facing : Direction.values()) {
            Block neighborBlock = world.getBlockState(pos.offset(facing)).getBlock();

            if (neighborBlock instanceof BlockBoundary) {
                // If the block is already in the correct state, there is no need to cascade to it's neighbors.
                if (cascadedBlockPos.contains(pos.offset(facing))) {
                    continue;
                }

                // running this method for the neighbor block will cascade out to it's other neighbors until there are
                // no more Phasic blocks around.
                ((BlockBoundary) neighborBlock).setNeighborGlassBlocksPoweredStatus(world, pos.offset(facing), isPowered, cascadeCount, cascadedBlockPos, true);
            }
        }
    }
}
