package chuunimod

import chuunimod.capabilities.LevelHandler
import chuunimod.capabilities.ManaHandler
import chuunimod.capabilities.MessageUpdateClientLevel
import chuunimod.capabilities.MessageUpdateClientMana
import chuunimod.event.ChuuniEventHandler
import chuunimod.gui.GuiChuuniOverlay
import net.minecraft.client.model.ModelBiped
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.item.Item
import net.minecraft.item.ItemArmor
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.CapabilityManager
import java.util.HashMap
import net.minecraft.client.renderer.ItemMeshDefinition
import net.minecraft.client.renderer.block.model.ModelBakery
import net.minecraft.util.ResourceLocation
import net.minecraft.item.ItemStack

class ServerProxy {
	def preInit {
		CapabilityManager.INSTANCE.register(classOf[ManaHandler], ManaHandler.getStorageInstance, ManaHandler.getHandlerFactory)
		CapabilityManager.INSTANCE.register(classOf[LevelHandler], LevelHandler.getStorageInstance, LevelHandler.getHandlerFactory)
		MinecraftForge.EVENT_BUS.register(new ChuuniEventHandler)
	}
}

class ClientProxy extends ServerProxy {
	override def preInit {
		super.preInit
		
		registerItemModels
		registerBlockModels
		registerArmorModels
		registerNetworkPackets
		
		MinecraftForge.EVENT_BUS.register(new GuiChuuniOverlay)
	}
	
	def registerItemModels {
		registerDefaultItemModel(ItemRegistry.itemRikkaArmor)
		
		val applyManaWeaponModel = variantItemModelFactory(List("_normal", "_active"), new ItemMeshDefinition() {
			override def getModelLocation(stack:ItemStack) = new ModelResourceLocation(
				stack.getItem.getRegistryName.toString+(if(stack.hasTagCompound && stack.getTagCompound.getBoolean("active")) "_active" else "_normal"), "inventory")
		}) _
		
		applyManaWeaponModel(ItemRegistry.itemRikkaWeapon)
	}
	
	def registerBlockModels {}
	def registerArmorModels {}
	
	def registerNetworkPackets {
		MessageUpdateClientMana.register(ChuuniMod.network, 0)
		MessageUpdateClientLevel.register(ChuuniMod.network, 1)
	}
	
	def registerDefaultItemModel(item:Item) = ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName.toString))
	
	def registerVariantItemModel(item:Item, variants:List[String], mesh:ItemMeshDefinition) {
		ModelBakery.registerItemVariants(item, variants map { v => new ResourceLocation(item.getRegistryName+v) }:_*)
		ModelLoader.setCustomMeshDefinition(item, mesh)
	}
	def variantItemModelFactory(variants:List[String], mesh:ItemMeshDefinition)(item:Item) { registerVariantItemModel(item, variants, mesh) }
}

object ClientProxy {
	val armorModels = new HashMap[ItemArmor, ModelBiped]
}
