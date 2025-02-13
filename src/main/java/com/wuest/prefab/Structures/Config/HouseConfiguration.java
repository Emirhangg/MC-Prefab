package com.wuest.prefab.Structures.Config;

import com.wuest.prefab.Config.EntityPlayerConfiguration;
import com.wuest.prefab.Config.ModConfiguration.CeilingFloorBlockType;
import com.wuest.prefab.Config.ModConfiguration.WallBlockType;
import com.wuest.prefab.Gui.GuiLangKeys;
import com.wuest.prefab.ModRegistry;
import com.wuest.prefab.Prefab;
import com.wuest.prefab.Proxy.Messages.PlayerEntityTagMessage;
import com.wuest.prefab.Structures.Base.BuildingMethods;
import com.wuest.prefab.Structures.Predefined.StructureAlternateStart;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BedPart;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;

import java.util.ArrayList;

/**
 * This class is used to determine the configuration for a particular house.
 *
 * @author WuestMan
 */
public class HouseConfiguration extends StructureConfiguration {
	private static String addTorchesTag = "addTorches";
	private static String addBedTag = "addBed";
	private static String addCraftingTableTag = "addCraftingTable";
	private static String addFurnaceTag = "addFurnace";
	private static String addChestTag = "addChest";
	private static String addChestContentsTag = "addChestContents";
	private static String addMineShaftTag = "addMineShaft";
	private static String hitXTag = "hitX";
	private static String hitYTag = "hitY";
	private static String hitZTag = "hitZ";
	private static String houseFacingTag = "houseFacing";
	private static String houseStyleTag = "houseStyle";
	private static String glassColorTag = "glassColor";

	public boolean addTorches;
	public boolean addBed;
	public boolean addCraftingTable;
	public boolean addFurnace;
	public boolean addChest;
	public boolean addChestContents;
	public boolean addMineShaft;
	public HouseStyle houseStyle;
	public DyeColor glassColor;

	/**
	 * Initializes a new instance of the {@link HouseConfiguration} class.
	 */
	public HouseConfiguration() {
		super();
	}

	/**
	 * Gets the name used in a text slider.
	 *
	 * @param name  The name of the option to get.
	 * @param value The integer value to associate with the name.
	 * @return A string representing the value to show in the text slider.
	 */
	public static String GetIntegerOptionStringValue(String name, int value) {
		if (name.equals(GuiLangKeys.STARTER_HOUSE_CEILING_TYPE)
				|| name.equals(GuiLangKeys.STARTER_HOUSE_FLOOR_STONE)) {
			return " - " + CeilingFloorBlockType.ValueOf(value).getName();
		} else if (name.equals(GuiLangKeys.STARTER_HOUSE_WALL_TYPE)) {
			return " - " + WallBlockType.ValueOf(value).getName();
		}

		return "";
	}

	@Override
	public void Initialize() {
		super.Initialize();
		this.houseStyle = HouseStyle.BASIC;
		this.glassColor = DyeColor.LIGHT_GRAY;
		this.addTorches = true;
		this.addBed = true;
		this.addCraftingTable = true;
		this.addFurnace = true;
		this.addChest = true;
		this.addChestContents = true;
		this.addMineShaft = true;
	}

	@Override
	public CompoundNBT WriteToCompoundNBT() {
		CompoundNBT tag = new CompoundNBT();

		// This tag should only be written for options which will NOT be overwritten by server options.
		// Server configuration settings will be used for all other options.
		// This is so the server admin can force a player to not use something.
		tag.putBoolean(HouseConfiguration.addTorchesTag, this.addTorches);
		tag.putBoolean(HouseConfiguration.addBedTag, this.addBed);
		tag.putBoolean(HouseConfiguration.addCraftingTableTag, this.addCraftingTable);
		tag.putBoolean(HouseConfiguration.addFurnaceTag, this.addFurnace);
		tag.putBoolean(HouseConfiguration.addChestTag, this.addChest);
		tag.putBoolean(HouseConfiguration.addChestContentsTag, this.addChestContents);
		tag.putBoolean(HouseConfiguration.addMineShaftTag, this.addMineShaft);
		tag.putInt(HouseConfiguration.hitXTag, this.pos.getX());
		tag.putInt(HouseConfiguration.hitYTag, this.pos.getY());
		tag.putInt(HouseConfiguration.hitZTag, this.pos.getZ());
		tag.putString(HouseConfiguration.houseFacingTag, this.houseFacing.getName());
		tag.putInt(HouseConfiguration.houseStyleTag, this.houseStyle.value);
		tag.putString(HouseConfiguration.glassColorTag, this.glassColor.getName().toUpperCase());

		return tag;
	}

	/**
	 * Custom method to read the CompoundNBT message.
	 *
	 * @param tag The message to create the configuration from.
	 * @return An new configuration object with the values derived from the CompoundNBT.
	 */
	@Override
	public HouseConfiguration ReadFromCompoundNBT(CompoundNBT tag) {
		HouseConfiguration config = null;

		if (tag != null) {
			config = new HouseConfiguration();

			if (tag.contains(HouseConfiguration.addTorchesTag)) {
				config.addTorches = tag.getBoolean(HouseConfiguration.addTorchesTag);
			}

			if (tag.contains(HouseConfiguration.addBedTag)) {
				config.addBed = tag.getBoolean(HouseConfiguration.addBedTag);
			}

			if (tag.contains(HouseConfiguration.addCraftingTableTag)) {
				config.addCraftingTable = tag.getBoolean(HouseConfiguration.addCraftingTableTag);
			}

			if (tag.contains(HouseConfiguration.addFurnaceTag)) {
				config.addFurnace = tag.getBoolean(HouseConfiguration.addFurnaceTag);
			}

			if (tag.contains(HouseConfiguration.addChestTag)) {
				config.addChest = tag.getBoolean(HouseConfiguration.addChestTag);
			}

			if (tag.contains(HouseConfiguration.addChestContentsTag)) {
				config.addChestContents = tag.getBoolean(HouseConfiguration.addChestContentsTag);
			}

			if (tag.contains(HouseConfiguration.addMineShaftTag)) {
				config.addMineShaft = tag.getBoolean(HouseConfiguration.addMineShaftTag);
			}

			if (tag.contains(HouseConfiguration.hitXTag)) {
				config.pos = new BlockPos(tag.getInt(HouseConfiguration.hitXTag), tag.getInt(HouseConfiguration.hitYTag), tag.getInt(HouseConfiguration.hitZTag));
			}

			if (tag.contains(HouseConfiguration.houseFacingTag)) {
				config.houseFacing = Direction.byName(tag.getString(HouseConfiguration.houseFacingTag));
			}

			if (tag.contains(HouseConfiguration.houseStyleTag)) {
				config.houseStyle = HouseStyle.ValueOf(tag.getInt(HouseConfiguration.houseStyleTag));
			}

			if (tag.contains(HouseConfiguration.glassColorTag)) {
				config.glassColor = DyeColor.valueOf(tag.getString(HouseConfiguration.glassColorTag));
			}
		}

		return config;
	}

	/**
	 * This is used to actually build the structure as it creates the structure instance and calls build structure.
	 *
	 * @param player      The player which requested the build.
	 * @param world       The world instance where the build will occur.
	 * @param hitBlockPos This hit block position.
	 */
	@Override
	protected void ConfigurationSpecificBuildStructure(PlayerEntity player, ServerWorld world, BlockPos hitBlockPos) {
		boolean houseBuilt = true;

		// Build the alternate starter house instead.
		StructureAlternateStart structure = StructureAlternateStart.CreateInstance(this.houseStyle.getStructureLocation(), StructureAlternateStart.class);
		houseBuilt = structure.BuildStructure(this, world, hitBlockPos, Direction.NORTH, player);

		// The house was successfully built, remove the item from the inventory.
		if (houseBuilt) {
			EntityPlayerConfiguration playerConfig = EntityPlayerConfiguration.loadFromEntityData(player);
			playerConfig.builtStarterHouse = true;
			playerConfig.saveToPlayer(player);

			this.RemoveStructureItemFromPlayer(player, ModRegistry.StartHouse());

			// Make sure to send a message to the client to sync up the server player information and the client player
			// information.
			Prefab.network.sendTo(new PlayerEntityTagMessage(playerConfig.getModIsPlayerNewTag(player)), ((ServerPlayerEntity) player).connection.netManager,
					NetworkDirection.PLAY_TO_CLIENT);
		}
	}

	/**
	 * This enum is used to contain the different type of starting houses available to the player.
	 *
	 * @author WuestMan
	 */
	public enum HouseStyle {
		BASIC(
				0,
				GuiLangKeys.STARTER_HOUSE_BASIC_DISPLAY,
				new ResourceLocation("prefab", "textures/gui/basic_house.png"),
				GuiLangKeys.STARTER_HOUSE_BASIC_NOTES,
				163,
				146,
				"assets/prefab/structures/basic_house.zip"),
		RANCH(1, GuiLangKeys.STARTER_HOUSE_RANCH_DISPLAY, new ResourceLocation("prefab", "textures/gui/ranch_house.png"), GuiLangKeys.STARTER_HOUSE_RANCH_NOTES, 152, 89,
				"assets/prefab/structures/ranch_house.zip"),
		LOFT(2, GuiLangKeys.STARTER_HOUSE_LOFT_DISPLAY, new ResourceLocation("prefab", "textures/gui/loft_house.png"), GuiLangKeys.STARTER_HOUSE_LOFT_NOTES, 152, 87,
				"assets/prefab/structures/loft_house.zip"),
		HOBBIT(3, GuiLangKeys.STARTER_HOUSE_HOBBIT_DISPLAY, new ResourceLocation("prefab", "textures/gui/hobbit_house.png"), GuiLangKeys.STARTER_HOUSE_HOBBIT_NOTES, 151, 133,
				"assets/prefab/structures/hobbit_house.zip"),
		DESERT(4, GuiLangKeys.STARTER_HOUSE_DESERT_DISPLAY, new ResourceLocation("prefab", "textures/gui/desert_house.png"), GuiLangKeys.STARTER_HOUSE_DESERT_NOTES, 152, 131,
				"assets/prefab/structures/desert_house.zip"),
		SNOWY(5, GuiLangKeys.STARTER_HOUSE_SNOWY_DISPLAY, new ResourceLocation("prefab", "textures/gui/snowy_house.png"), GuiLangKeys.STARTER_HOUSE_SNOWY_NOTES, 150, 125,
				"assets/prefab/structures/snowy_house.zip"),
		DESERT2(6,
				GuiLangKeys.STARTER_HOUSE_DESERT_DISPLAY2,
				new ResourceLocation("prefab", "textures/gui/desert_house2.png"),
				GuiLangKeys.STARTER_HOUSE_DESERT_NOTES2,
				145,
				153,
				"assets/prefab/structures/desert_house2.zip"),
		SUBAQUATIC(7,
					GuiLangKeys.STARTER_HOUSE_SUBAQUATIC_DISPLAY,
					new ResourceLocation("prefab", "textures/gui/subaquatic_house.png"),
					GuiLangKeys.STARTER_HOUSE_SUBAQUATIC_NOTES,
					144,
					162,
					"assets/prefab/structures/subaquatic_house.zip");

		private final int value;
		private final String displayName;
		private final ResourceLocation housePicture;
		private final String houseNotes;
		private final int imageWidth;
		private final int imageHeight;
		private final String structureLocation;

		HouseStyle(int newValue, String displayName, ResourceLocation housePicture, String houseNotes, int imageWidth, int imageHeight, String structureLocation) {
			this.value = newValue;
			this.displayName = displayName;
			this.housePicture = housePicture;
			this.houseNotes = houseNotes;
			this.imageWidth = imageWidth;
			this.imageHeight = imageHeight;
			this.structureLocation = structureLocation;
		}

		/**
		 * Returns a house style based off of an integer value.
		 *
		 * @param value The integer value representing the house style.
		 * @return The house style found or HouseStyle.Basic if none found.
		 */
		public static HouseStyle ValueOf(int value) {
			HouseStyle returnValue = HouseStyle.BASIC;

			for (HouseStyle current : HouseStyle.values())
			{
				if (current.value == value) {
					returnValue = current;
					break;
				}
			}

			return returnValue;
		}

		/**
		 * Gets a unique identifier for this style.
		 *
		 * @return An integer representing the ID of this style.
		 */
		public int getValue() {
			return value;
		}

		/**
		 * Gets the display name for this style.
		 *
		 * @return A string representing the name of this style.
		 */
		public String getDisplayName() {
			return GuiLangKeys.translateString(this.displayName);
		}

		/**
		 * Gets the notes for this house style.
		 *
		 * @return A string representing the translated notes for this style.
		 */
		public String getHouseNotes() {
			return GuiLangKeys.translateString(this.houseNotes);
		}

		/**
		 * Gets the picture used in the GUI for this style.
		 *
		 * @return A resource location representing the image to use for this style.
		 */
		public ResourceLocation getHousePicture() {
			return this.housePicture;
		}

		/**
		 * Gets the width of the image to use with this style.
		 *
		 * @return An integer representing the image width.
		 */
		public int getImageWidth() {
			return this.imageWidth;
		}

		/**
		 * Gets the height of the image to use with this style.
		 *
		 * @return An integer representing the image height.
		 */
		public int getImageHeight() {
			return this.imageHeight;
		}

		/**
		 * Gets a string for the resource location of this style.
		 *
		 * @return A string representing the location of the structure asset in the mod.
		 */
		public String getStructureLocation() {
			return this.structureLocation;
		}
	}
}
