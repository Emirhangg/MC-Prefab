package com.wuest.prefab.Items.Structures;

import com.wuest.prefab.ModRegistry;
import com.wuest.prefab.Prefab;
import com.wuest.prefab.Config.ModularHouseConfiguration;
import com.wuest.prefab.StructureGen.CustomStructures.StructureModularHouse;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 
 * @author WuestMan
 *
 */
public class ItemModularHouse extends StructureItem
{
	public ItemModularHouse(String name)
	{
		super();

		this.guiId = ModRegistry.GuiModularHouse;
		ModRegistry.setItemName(this, name);
	}
	
	public static void BuildHouse(EntityPlayer player, World world, ModularHouseConfiguration configuration)
	{
		// This is always on the server.
		if (configuration != null)
		{
			BlockPos hitBlockPos = configuration.pos;
			BlockPos playerPosition = player.getPosition();

			IBlockState hitBlockState = world.getBlockState(hitBlockPos);

			if (hitBlockState != null)
			{
				Block hitBlock = hitBlockState.getBlock();
 
				if (hitBlock != null)
				{
					StructureModularHouse structure = StructureModularHouse.CreateInstance(StructureModularHouse.ASSETLOCATION, StructureModularHouse.class);
					
					if (structure.BuildStructure(configuration, world, hitBlockPos, EnumFacing.NORTH, player))
					{
						player.inventory.clearMatchingItems(ModRegistry.ModularHouse(), -1, 1, null);
						player.inventoryContainer.detectAndSendChanges();
					}
				}
			}
		}
	}
}
