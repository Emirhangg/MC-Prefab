package com.wuest.prefab.Structures.Predefined;

import com.wuest.prefab.Structures.Base.BuildBlock;
import com.wuest.prefab.Structures.Base.BuildClear;
import com.wuest.prefab.Structures.Base.Structure;
import com.wuest.prefab.Structures.Config.InstantBridgeConfiguration;
import com.wuest.prefab.Structures.Config.StructureConfiguration;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;

/**
 * @author WuestMan
 */
public class StructureInstantBridge extends Structure {
    /*
     * Initializes a new instance of the StructureInstantBridge class.
     */
    public StructureInstantBridge() {
        super();
    }

    /**
     * Creates an instance of the structure after reading from a resource location and converting it from JSON.
     *
     * @return A new instance of this class.
     */
    public static StructureInstantBridge CreateInstance() {
        StructureInstantBridge structure = new StructureInstantBridge();
        return structure;
    }

    /**
     * This is the main building method for this structure.
     *
     * @param configuration The configuration the user updated.
     * @param world         The current world.
     * @param originalPos   The block the user clicked on.
     * @param assumedNorth  This should always be "NORTH" when the file is based on a scan.
     * @param player        The player requesting the structure.
     * @return True if the build can occur, otherwise false.
     */
    @Override
    public boolean BuildStructure(StructureConfiguration configuration, ServerWorld world, BlockPos originalPos, Direction assumedNorth, PlayerEntity player) {
        InstantBridgeConfiguration specificConfig = (InstantBridgeConfiguration) configuration;
        this.setupClearSpace(specificConfig);

        this.setupStructure(specificConfig, originalPos);

        return super.BuildStructure(specificConfig, world, originalPos, assumedNorth, player);
    }

    public void setupStructure(InstantBridgeConfiguration configuration, BlockPos originalPos) {
        ArrayList<BuildBlock> buildingBlocks = new ArrayList<BuildBlock>();
        BlockState materialState = configuration.bridgeMaterial.getBlockType();
        Direction facing = Direction.SOUTH;

        BlockState torchState = Blocks.TORCH.getDefaultState();
        BlockState glassState = Blocks.GLASS_PANE.getDefaultState();

        int interiorHeightOffSet = configuration.interiorHeight - 3;

        for (int i = 1; i <= configuration.bridgeLength; i++) {
            BlockPos currentPos = originalPos.offset(facing, i);

            // Place the floor
            buildingBlocks.add(Structure.createBuildBlockFromBlockState(materialState, materialState.getBlock(), currentPos.offset(facing.rotateYCCW(), 2), originalPos));
            buildingBlocks.add(Structure.createBuildBlockFromBlockState(materialState, materialState.getBlock(), currentPos.offset(facing.rotateYCCW()), originalPos));
            buildingBlocks.add(Structure.createBuildBlockFromBlockState(materialState, materialState.getBlock(), currentPos, originalPos));
            buildingBlocks.add(Structure.createBuildBlockFromBlockState(materialState, materialState.getBlock(), currentPos.offset(facing.rotateY()), originalPos));
            buildingBlocks.add(Structure.createBuildBlockFromBlockState(materialState, materialState.getBlock(), currentPos.offset(facing.rotateY(), 2), originalPos));

            // Build the walls.
            buildingBlocks.add(Structure.createBuildBlockFromBlockState(materialState, materialState.getBlock(), currentPos.offset(facing.rotateYCCW(), 2).up(), originalPos));
            buildingBlocks.add(Structure.createBuildBlockFromBlockState(materialState, materialState.getBlock(), currentPos.offset(facing.rotateY(), 2).up(), originalPos));

            if (configuration.includeRoof) {
                // Build the roof.
                buildingBlocks.add(Structure.createBuildBlockFromBlockState(materialState, materialState.getBlock(),
                        currentPos.offset(facing.rotateYCCW(), 2).up(3 + interiorHeightOffSet), originalPos));
                buildingBlocks.add(Structure.createBuildBlockFromBlockState(materialState, materialState.getBlock(),
                        currentPos.offset(facing.rotateYCCW()).up(4 + interiorHeightOffSet), originalPos));
                buildingBlocks.add(Structure.createBuildBlockFromBlockState(materialState, materialState.getBlock(), currentPos.up(4 + interiorHeightOffSet), originalPos));
                buildingBlocks.add(Structure.createBuildBlockFromBlockState(materialState, materialState.getBlock(),
                        currentPos.offset(facing.rotateY()).up(4 + interiorHeightOffSet), originalPos));
                buildingBlocks.add(Structure.createBuildBlockFromBlockState(materialState, materialState.getBlock(),
                        currentPos.offset(facing.rotateY(), 2).up(3 + interiorHeightOffSet), originalPos));
            }

            for (int j = 0; j <= interiorHeightOffSet; j++) {
                if ((i == 1 || i % 6 == 0) && j == 0) {
                    // Place torches.
                    buildingBlocks.add(Structure.createBuildBlockFromBlockState(torchState, torchState.getBlock(), currentPos.offset(facing.rotateYCCW(), 2).up(2), originalPos));
                    buildingBlocks.add(Structure.createBuildBlockFromBlockState(torchState, torchState.getBlock(), currentPos.offset(facing.rotateY(), 2).up(2), originalPos));
                } else if (configuration.includeRoof) {
                    // Place Glass panes
                    buildingBlocks
                            .add(Structure.createBuildBlockFromBlockState(glassState, glassState.getBlock(), currentPos.offset(facing.rotateYCCW(), 2).up(2 + j), originalPos));
                    buildingBlocks.add(Structure.createBuildBlockFromBlockState(glassState, glassState.getBlock(), currentPos.offset(facing.rotateY(), 2).up(2 + j), originalPos));
                }
            }
        }

        this.setBlocks(buildingBlocks);
    }

    private void setupClearSpace(InstantBridgeConfiguration configuration) {
        int clearHeight = 3;

        if (configuration.includeRoof) {
            clearHeight = (configuration.interiorHeight - clearHeight) + clearHeight + 2;
        }

        BuildClear clear = new BuildClear();
        clear.getStartingPosition().setSouthOffset(1);
        clear.getStartingPosition().setEastOffset(2);
        clear.getShape().setDirection(Direction.SOUTH);
        clear.getShape().setHeight(clearHeight - 1);
        clear.getShape().setWidth(5);
        clear.getShape().setLength(configuration.bridgeLength);

        this.setClearSpace(clear);
    }
}