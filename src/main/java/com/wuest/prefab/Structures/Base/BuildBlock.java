package com.wuest.prefab.Structures.Base;

import com.google.gson.annotations.Expose;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.wuest.prefab.Prefab;
import com.wuest.prefab.Structures.Config.StructureConfiguration;
import net.minecraft.block.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.IProperty;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

/**
 * This class defines a single block and where it will be in the structure.
 *
 * @author WuestMan
 */
public class BuildBlock {
    public BlockPos blockPos;
    @Expose
    private String blockDomain;
    @Expose
    private String blockName;
    @Expose
    private PositionOffset startingPosition;
    @Expose
    private ArrayList<BuildProperty> properties;
    @Expose
    private BuildBlock subBlock;
    @Expose
    private boolean hasFacing;
    @Expose
    private BlockState state;
    @Expose
    private String blockStateData;

    public BuildBlock() {
        this.Initialize();
    }

    public static BuildBlock SetBlockState(StructureConfiguration configuration, World world, BlockPos originalPos, Direction assumedNorth, BuildBlock block, Block foundBlock,
                                           BlockState blockState, Structure structure) {
        try {
            if (!block.blockStateData.equals("")) {
                return BuildBlock.SetBlockStateFromTagData(configuration, world, originalPos, assumedNorth, block, foundBlock, blockState, structure);
            }

            Direction vineFacing = BuildBlock.getVineFacing(configuration, foundBlock, block, structure.getClearSpace().getShape().getDirection());
            Direction.Axis logFacing = BuildBlock.getBoneFacing(configuration, foundBlock, block, structure.getClearSpace().getShape().getDirection());
            Direction.Axis boneFacing = BuildBlock.getBoneFacing(configuration, foundBlock, block, structure.getClearSpace().getShape().getDirection());
            Direction leverOrientation = BuildBlock.getLeverOrientation(configuration, foundBlock, block, structure.getClearSpace().getShape().getDirection());
            Map<Direction, Boolean> fourWayFacings = BuildBlock.getFourWayBlockFacings(configuration, foundBlock, block, structure.getClearSpace().getShape().getDirection());

            // If this block has custom processing for block state just continue onto the next block. The sub-class is
            // expected to place the block.
            if (block.getProperties().size() > 0) {
                Collection<IProperty<?>> properties = blockState.getProperties();

                // Go through each property of this block and set it.
                // The state will be updated as the properties are
                // applied.
                for (IProperty<?> property : properties) {
                    BuildProperty buildProperty = block.getProperty(property.getName());

                    // Make sure that this property exists in our file. The only way it wouldn't be there would be if a
                    // mod adds properties to vanilla blocks.
                    if (buildProperty != null) {
                        try {
                            Optional<?> propertyValue = property.parseValue(buildProperty.getValue());

                            if (!propertyValue.isPresent()
                                    || propertyValue.getClass().getName().equals("com.google.common.base.Absent")) {
                                Prefab.LOGGER.warn(
                                        "Property value for property name [" + property.getName() + "] for block [" + block.getBlockName() + "] is considered Absent, figure out why.");
                                continue;
                            }

                            Comparable<?> comparable = property.getValueClass().cast(propertyValue.get());

                            if (comparable == null) {
                                continue;
                            }

                            comparable = BuildBlock.setComparable(comparable, foundBlock, property, configuration, block, propertyValue, vineFacing, logFacing,
                                    boneFacing, leverOrientation, structure, fourWayFacings);

                            if (comparable == null) {
                                continue;
                            }

                            try {
                                if (blockState.get(property) != comparable) {
                                    blockState = BuildBlock.setProperty(blockState, property, comparable);
                                }
                            } catch (Exception ex) {
                                System.out.println("Error setting properly value for property name [" + property.getName() + "] property value [" + buildProperty.getValue()
                                        + "] for block [" + block.getBlockName() + "] The default value will be used.");
                            }
                        } catch (Exception ex) {
                            System.out.println("Error getting properly value for property name [" + property.getName() + "] property value [" + buildProperty.getValue()
                                    + "] for block [" + block.getBlockName() + "]");
                            throw ex;
                        }
                    } else {
                        // System.out.println("Property: [" + property.getName() + "] does not exist for Block: [" +
                        // block.getBlockName() + "] this is usually due to mods adding properties to vanilla blocks.");
                    }
                }
            }

            block.setBlockState(blockState);
            return block;
        } catch (Exception ex) {
            System.out.println("Error setting block state for block [" + block.getBlockName() + "] for structure configuration class [" + configuration.getClass().getName() + "]");
            throw ex;
        }
    }

    public static Direction getHorizontalFacing(Direction currentFacing, Direction configurationFacing, Direction structureDirection) {
        if (currentFacing != null && currentFacing != Direction.UP && currentFacing != Direction.DOWN) {
            if (configurationFacing.getOpposite() == structureDirection.rotateY()) {
                currentFacing = currentFacing.rotateY();
            } else if (configurationFacing.getOpposite() == structureDirection.getOpposite()) {
                currentFacing = currentFacing.getOpposite();
            } else if (configurationFacing.getOpposite() == structureDirection.rotateYCCW()) {
                currentFacing = currentFacing.rotateYCCW();
            }
        }

        return currentFacing;
    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent", "OptionalUsedAsFieldOrParameterType"})
    private static Comparable setComparable(Comparable<?> comparable, Block foundBlock, IProperty<?> property, StructureConfiguration configuration, BuildBlock block,
                                            Optional<?> propertyValue, Direction vineFacing, Direction.Axis logFacing, Axis boneFacing, Direction leverOrientation,
                                            Structure structure,
                                            Map<Direction, Boolean> fourWayFacings) {
        if (property.getName().equals("facing") && !(foundBlock instanceof HorizontalFaceBlock)) {
            // Facing properties should be relative to the configuration facing.
            Direction facing = Direction.byName(propertyValue.get().toString());

            // Cannot rotate verticals.
            facing = BuildBlock.getHorizontalFacing(facing, configuration.houseFacing, structure.getClearSpace().getShape().getDirection());

            comparable = facing;

            block.setHasFacing(true);
        } else if (property.getName().equals("facing") && foundBlock instanceof HorizontalFaceBlock) {
            comparable = leverOrientation;
            block.setHasFacing(true);
        } else if (property.getName().equals("rotation")) {
            // 0 = South
            // 4 = West
            // 8 = North
            // 12 = East
            int rotation = (Integer) propertyValue.get();
            Direction facing = rotation == 0 ? Direction.SOUTH : rotation == 4 ? Direction.WEST : rotation == 8 ? Direction.NORTH : Direction.EAST;

            if (configuration.houseFacing.getOpposite() == structure.getClearSpace().getShape().getDirection().rotateY()) {
                facing = facing.rotateY();
            } else if (configuration.houseFacing.getOpposite() == structure.getClearSpace().getShape().getDirection().getOpposite()) {
                facing = facing.getOpposite();
            } else if (configuration.houseFacing.getOpposite() == structure.getClearSpace().getShape().getDirection().rotateYCCW()) {
                facing = facing.rotateYCCW();
            }

            rotation = facing == Direction.SOUTH ? 0 : facing == Direction.WEST ? 4 : facing == Direction.NORTH ? 8 : 12;
            comparable = rotation;
            block.setHasFacing(true);
        } else if (foundBlock instanceof VineBlock) {
            // Vines have a special state. There is 1 property for each "facing".
            if (property.getName().equals(vineFacing.getName2())) {
                comparable = true;
                block.setHasFacing(true);
            } else {
                comparable = false;
            }
        } else if (foundBlock instanceof FourWayBlock && !property.getName().equals("waterlogged")) {
            for (Map.Entry<Direction, Boolean> entry : fourWayFacings.entrySet()) {
                if (property.getName().equals(entry.getKey().getName2())) {
                    comparable = entry.getValue();
                }
            }
        } else if (foundBlock instanceof WallBlock) {
            if (!property.getName().equals("variant")) {
                if (property.getName().equals(vineFacing.getName2())
                        || property.getName().equals(vineFacing.getOpposite().getName2())) {
                    comparable = true;
                    block.setHasFacing(true);
                } else {
                    comparable = false;
                }
            }
        } else if (foundBlock instanceof LogBlock) {
            // logs have a special state. There is a property called axis and it only has 3 directions.
            if (property.getName().equals("axis")) {
                comparable = logFacing;
            }
        } else if (foundBlock instanceof RotatedPillarBlock) {
            // bones have a special state. There is a property called axis and it only has 3 directions.
            if (property.getName().equals("axis")) {
                comparable = boneFacing;
            }
        }

        return comparable;
    }

    private static Direction getVineFacing(StructureConfiguration configuration, Block foundBlock, BuildBlock block, Direction assumedNorth) {
        Direction vineFacing = Direction.UP;

        // Vines have a special property for it's "facing"
        if (foundBlock instanceof VineBlock
                || foundBlock instanceof WallBlock) {
            if (block.getProperty("east").getValue().equals("true")) {
                vineFacing = Direction.EAST;
            } else if (block.getProperty("west").getValue().equals("true")) {
                vineFacing = Direction.WEST;
            } else if (block.getProperty("south").getValue().equals("true")) {
                vineFacing = Direction.SOUTH;
            } else if (block.getProperty("north").getValue().equals("true")) {
                vineFacing = Direction.NORTH;
            }

            if (vineFacing != Direction.UP) {
                if (configuration.houseFacing.rotateY() == assumedNorth) {
                    vineFacing = vineFacing.rotateY();
                } else if (configuration.houseFacing.getOpposite() == assumedNorth) {
                } else if (configuration.houseFacing.rotateYCCW() == assumedNorth) {
                    vineFacing = vineFacing.rotateYCCW();
                } else {
                    vineFacing = vineFacing.getOpposite();
                }
            }
        }

        return vineFacing;
    }

    private static Map<Direction, Boolean> getFourWayBlockFacings(StructureConfiguration configuration, Block foundBlock, BuildBlock block, Direction assumedNorth) {
        Map<Direction, Boolean> facings = new HashMap<>();

        if (foundBlock instanceof FourWayBlock) {
            // Valid states can be any two directions at a time, not just opposites but adjacents as well (for corners).
            boolean northValue = Boolean.parseBoolean(block.getProperty("north").getValue());
            boolean eastValue = Boolean.parseBoolean(block.getProperty("east").getValue());
            boolean westValue = Boolean.parseBoolean(block.getProperty("west").getValue());
            boolean southValue = Boolean.parseBoolean(block.getProperty("south").getValue());
            boolean originalNorth = northValue;
            boolean originalEast = eastValue;
            boolean originalWest = westValue;
            boolean originalSouth = southValue;

            if (configuration.houseFacing.rotateY() == assumedNorth) {
                northValue = originalWest;
                eastValue = originalNorth;
                southValue = originalEast;
                westValue = originalSouth;
            } else if (configuration.houseFacing == assumedNorth) {
                northValue = originalSouth;
                eastValue = originalWest;
                southValue = originalNorth;
                westValue = originalEast;
            } else if (configuration.houseFacing.rotateYCCW() == assumedNorth) {
                northValue = originalEast;
                eastValue = originalSouth;
                southValue = originalWest;
                westValue = originalNorth;
            }

            facings.put(Direction.NORTH, northValue);
            facings.put(Direction.EAST, eastValue);
            facings.put(Direction.WEST, westValue);
            facings.put(Direction.SOUTH, southValue);
        }

        return facings;
    }

    private static Axis getBoneFacing(StructureConfiguration configuration, Block foundBlock, BuildBlock block, Direction assumedNorth) {
        Axis boneFacing = Axis.X;

        if (foundBlock instanceof RotatedPillarBlock) {
            if (block.getProperty("axis").getValue().equals("x")) {
                boneFacing = Axis.X;
            } else if (block.getProperty("axis").getValue().equals("y")) {
                boneFacing = Axis.Y;
            } else {
                boneFacing = Axis.Z;
            }

            if (boneFacing != Axis.Y) {
                boneFacing = configuration.houseFacing == assumedNorth || configuration.houseFacing == assumedNorth.getOpposite()
                        ? boneFacing
                        : boneFacing == Axis.X
                        ? Axis.Z
                        : Axis.X;
            }
        }

        return boneFacing;
    }

    private static Direction getLeverOrientation(StructureConfiguration configuration, Block foundBlock, BuildBlock block, Direction assumedNorth) {
        Direction leverOrientation = Direction.NORTH;
        AttachFace attachedTo = AttachFace.FLOOR;

        if (foundBlock instanceof HorizontalFaceBlock) {
            // Levers have a special facing.
            leverOrientation = LeverBlock.HORIZONTAL_FACING.parseValue(block.getProperty("facing").getValue()).get();
            attachedTo = LeverBlock.FACE.parseValue(block.getProperty("face").getValue()).get();

            if (attachedTo == AttachFace.FLOOR
                    || attachedTo == AttachFace.CEILING) {
                if (attachedTo == AttachFace.FLOOR) {
                    leverOrientation = configuration.houseFacing == assumedNorth || configuration.houseFacing == assumedNorth.getOpposite()
                            ? leverOrientation
                            : leverOrientation == Direction.NORTH
                            ? Direction.EAST
                            : Direction.NORTH;
                } else {
                    leverOrientation = configuration.houseFacing == assumedNorth || configuration.houseFacing == assumedNorth.getOpposite()
                            ? leverOrientation
                            : leverOrientation == Direction.NORTH
                            ? Direction.EAST
                            : Direction.NORTH;
                }
            } else {
                Direction facing = leverOrientation;

                if (configuration.houseFacing.rotateY() == assumedNorth) {
                    facing = facing.rotateY();
                } else if (configuration.houseFacing.getOpposite() == assumedNorth) {
                } else if (configuration.houseFacing.rotateYCCW() == assumedNorth) {
                    facing = facing.rotateYCCW();
                } else {
                    facing = facing.getOpposite();
                }

                for (Direction tempOrientation : Direction.values()) {
                    if (tempOrientation == facing) {
                        leverOrientation = tempOrientation;
                        break;
                    }
                }
            }
        }

        return leverOrientation;
    }

    private static BlockState setProperty(BlockState state, IProperty property, Comparable comparable) {
        // This method is required since the properties and comparables have a <?> in them and it doesn't work properly
        // when that is there. There is a compilation error since it's not hard typed.
        return state.with(property, comparable);
    }

    private static BuildBlock SetBlockStateFromTagData(StructureConfiguration configuration, World world, BlockPos originalPos, Direction assumedNorth, BuildBlock block,
                                                       Block foundBlock, BlockState blockState, Structure structure) {
        BlockState tagState = block.getBlockStateFromDataTag();

        if (tagState != null) {
            block.setBlockState(block.getBlockStateFromDataTag());
        } else {
            block.setBlockStateData("");
            return BuildBlock.SetBlockState(configuration, world, originalPos, assumedNorth, block, foundBlock, blockState, structure);
        }

        return block;
    }

    public String getBlockDomain() {
        return this.blockDomain;
    }

    public void setBlockDomain(String value) {
        this.blockDomain = value;
    }

    public String getBlockName() {
        return this.blockName;
    }

    public void setBlockName(String value) {
        this.blockName = value;
    }

    public ResourceLocation getResourceLocation() {
        ResourceLocation location = new ResourceLocation(this.blockDomain, this.blockName);
        return location;
    }

    public PositionOffset getStartingPosition() {
        return this.startingPosition;
    }

    public void setStartingPosition(PositionOffset value) {
        this.startingPosition = value;
    }

    public ArrayList<BuildProperty> getProperties() {
        return this.properties;
    }

    public void setProperties(ArrayList<BuildProperty> value) {
        this.properties = value;
    }

    public BuildProperty getProperty(String name) {
        for (BuildProperty property : this.getProperties()) {
            if (name.equals(property.getName())) {
                return property;
            }
        }

        return null;
    }

    public BuildBlock getSubBlock() {
        return this.subBlock;
    }

    public void setSubBlock(BuildBlock value) {
        this.subBlock = value;
    }

    public boolean getHasFacing() {
        return this.hasFacing;
    }

    public void setHasFacing(boolean value) {
        this.hasFacing = value;
    }

    public BlockState getBlockState() {
        return this.state;
    }

    public void setBlockState(BlockState value) {
        this.state = value;
    }

    public String getBlockStateData() {
        return this.blockStateData;
    }

    public void setBlockStateData(String value) {
        this.blockStateData = value;
    }

    public void setBlockStateData(CompoundNBT tagCompound) {
        this.blockStateData = tagCompound.toString();
    }

    public CompoundNBT getBlockStateDataTag() {
        CompoundNBT tag = null;

        if (!this.blockStateData.equals("")) {
            try {
                tag = JsonToNBT.getTagFromJson(this.blockStateData);
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
        }

        return tag;
    }

    public BlockState getBlockStateFromDataTag() {
        BlockState state = null;

        if (!this.blockStateData.equals("")) {
            CompoundNBT tag = this.getBlockStateDataTag();

            if (tag != null) {
                state = NBTUtil.readBlockState(tag.getCompound("tag"));
            }
        }

        return state;
    }

    public void Initialize() {
        this.blockDomain = "";
        this.blockName = "";
        this.properties = new ArrayList<BuildProperty>();
        this.hasFacing = false;
        this.state = null;
        this.subBlock = null;
        this.startingPosition = new PositionOffset();
        this.blockStateData = "";
    }
}