package com.wuest.prefab.Proxy;

import com.wuest.prefab.Config.ServerModConfiguration;
import com.wuest.prefab.Events.ClientEventHandler;
import com.wuest.prefab.ModRegistry;
import com.wuest.prefab.Prefab;
import com.wuest.prefab.Structures.Events.StructureClientEventHandler;
import com.wuest.prefab.Structures.Gui.*;
import com.wuest.prefab.Structures.Items.ItemWareHouse;
import com.wuest.prefab.Structures.Items.StructureItem;
import com.wuest.prefab.Structures.Render.ShaderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author WuestMan
 */
public class ClientProxy extends CommonProxy {
	public static ClientEventHandler clientEventHandler = new ClientEventHandler();
	public static StructureClientEventHandler structureClientEventHandler = new StructureClientEventHandler();

	/**
	 * The hashmap of mod guis.
	 */
	public static HashMap<StructureItem, GuiStructure> ModGuis = new HashMap();

	public ServerModConfiguration serverConfiguration = null;

	public ClientProxy() {
		super();
	}

	@Override
	public void preInit(FMLCommonSetupEvent event) {
		super.preInit(event);

		this.AddGuis();

		// After all items have been registered and all recipes loaded, register any necessary renderer.
		Prefab.proxy.registerRenderers();
	}

	@Override
	public void init(FMLCommonSetupEvent event) {
		super.init(event);
	}

	@Override
	public void postinit(FMLCommonSetupEvent event) {
		super.postinit(event);
	}

	@Override
	public void RegisterEventHandler() {
		//FMLJavaModLoadingContext.get().getModEventBus().register(ClientProxy.clientEventHandler);
		//FMLJavaModLoadingContext.get().getModEventBus().register(ClientProxy.structureClientEventHandler);
	}

	@Override
	public void registerRenderers() {
		ShaderHelper.Initialize();
	}

	@Override
	public ServerModConfiguration getServerConfiguration() {
		if (this.serverConfiguration == null) {
			// Get the server configuration.
			return CommonProxy.proxyConfiguration.serverConfiguration;
		} else {
			return this.serverConfiguration;
		}
	}

	@Override
	public void openGuiForItem(ItemUseContext itemUseContext) {
		for (Map.Entry<StructureItem, GuiStructure> entry : ClientProxy.ModGuis.entrySet()) {
			if (entry.getKey().getClass() == itemUseContext.getItem().getItem().getClass()) {
				GuiStructure screen = entry.getValue();
                screen.pos = itemUseContext.getPos();

                Minecraft.getInstance().displayGuiScreen(screen);
			}
		}
	}

	/**
	 * Adds all of the Mod Guis to the HasMap.
	 */
	public static void AddGuis() {
		ClientProxy.ModGuis.put(ModRegistry.WareHouse(), new GuiWareHouse());
		ClientProxy.ModGuis.put(ModRegistry.ChickenCoop(), new GuiChickenCoop());
		ClientProxy.ModGuis.put(ModRegistry.ProduceFarm(), new GuiProduceFarm());
		ClientProxy.ModGuis.put(ModRegistry.TreeFarm(), new GuiTreeFarm());
		ClientProxy.ModGuis.put(ModRegistry.FishPond(), new GuiFishPond());
		ClientProxy.ModGuis.put(ModRegistry.StartHouse(), new GuiStartHouseChooser());
		ClientProxy.ModGuis.put(ModRegistry.AdvancedWareHouse(), new GuiAdvancedWareHouse());
		ClientProxy.ModGuis.put(ModRegistry.MonsterMasher(), new GuiMonsterMasher());
		ClientProxy.ModGuis.put(ModRegistry.HorseStable(), new GuiHorseStable());
		ClientProxy.ModGuis.put(ModRegistry.NetherGate(), new GuiNetherGate());
		ClientProxy.ModGuis.put(ModRegistry.BasicStructure(), new GuiBasicStructure());
		ClientProxy.ModGuis.put(ModRegistry.VillagerHouses(), new GuiVillaerHouses());
		ClientProxy.ModGuis.put(ModRegistry.ModerateHouse(), new GuiModerateHouse());
		ClientProxy.ModGuis.put(ModRegistry.Bulldozer(), new GuiBulldozer());
		ClientProxy.ModGuis.put(ModRegistry.InstantBridge(), new GuiInstantBridge());
		ClientProxy.ModGuis.put(ModRegistry.StructurePart(), new GuiStructurePart());
	}
}