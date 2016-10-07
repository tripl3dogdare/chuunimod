package chuunimod

import chuunimod.capabilities.ManaHandler
import chuunimod.capabilities.MessageUpdateClientMana
import chuunimod.event.ChuuniEventHandler
import chuunimod.gui.GuiManaOverlay
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.item.Item
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.network.NetworkRegistry

class ServerProxy {
	def preInit {
		CapabilityManager.INSTANCE.register(classOf[ManaHandler], ManaHandler.getStorageInstance, ManaHandler.getHandlerFactory)
		MinecraftForge.EVENT_BUS.register(new ChuuniEventHandler)
	}
}

class ClientProxy extends ServerProxy {
	override def preInit {
		super.preInit
		
		registerItemModels
		registerBlockModels
		registerNetworkPackets
		
		MinecraftForge.EVENT_BUS.register(new GuiManaOverlay)
	}
	
	def registerItemModels {
	}
	
	def registerBlockModels {
	}
	
	def registerNetworkPackets {
		MessageUpdateClientMana.register(ChuuniMod.network, 0)
	}
	
	def registerDefaultItemModel(item:Item) = ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName.toString))
}
