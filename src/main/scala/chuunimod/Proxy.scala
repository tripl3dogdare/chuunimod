package chuunimod

import java.util.HashMap

import chuunimod.capabilities.LevelHandler
import chuunimod.capabilities.ManaHandler
import chuunimod.event.ChuuniEventHandler
import chuunimod.gui.GuiChuuniOverlay
import chuunimod.util.MiscUtils.func2callable
import net.minecraft.client.model.ModelBiped
import net.minecraft.client.renderer.ItemMeshDefinition
import net.minecraft.client.renderer.block.model.ModelBakery
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.item.Item
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.CapabilityManager
import chuunimod.model.ModelYuutaArmor
import chuunimod.model.ModelDekoArmor

class ServerProxy {
	def preInit {
		CapabilityManager.INSTANCE.register(classOf[ManaHandler], new ManaHandler.Storage, ManaHandler.handlerFactory)
		CapabilityManager.INSTANCE.register(classOf[LevelHandler], new LevelHandler.Storage, LevelHandler.handlerFactory)
		MinecraftForge.EVENT_BUS.register(new ChuuniEventHandler)
	}
}

class ClientProxy extends ServerProxy {
	import ClientProxy._
	
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
		registerDefaultItemModel(ItemRegistry.itemYuutaArmor)
		registerDefaultItemModel(ItemRegistry.itemToukaArmor)
		registerDefaultItemModel(ItemRegistry.itemDekoArmor)
		
		val applyManaWeaponModel = variantItemModelFactory(List("_normal", "_active"), new ItemMeshDefinition() {
			override def getModelLocation(stack:ItemStack) = new ModelResourceLocation(
				stack.getItem.getRegistryName.toString+(if(stack.hasTagCompound && stack.getTagCompound.getBoolean("active")) "_active" else "_normal"), "inventory")
		}) _
		
		applyManaWeaponModel(ItemRegistry.itemRikkaWeapon)
		applyManaWeaponModel(ItemRegistry.itemYuutaWeapon)
		applyManaWeaponModel(ItemRegistry.itemToukaWeapon)
		applyManaWeaponModel(ItemRegistry.itemDekoWeapon)
	}
	
	def registerBlockModels {}
	
	def registerArmorModels {
		armorModels.put(ItemRegistry.itemYuutaArmor, new ModelYuutaArmor)
		armorModels.put(ItemRegistry.itemDekoArmor, new ModelDekoArmor)
	}
	
	def registerNetworkPackets {
		ManaHandler.registerClientUpdatePacket(ChuuniMod.network, 0)
		LevelHandler.registerClientUpdatePacket(ChuuniMod.network, 1)
	}
	
	def registerDefaultItemModel(item:Item) = ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName.toString))
	
	def registerVariantItemModel(item:Item, variants:List[String], mesh:ItemMeshDefinition) {
		ModelBakery.registerItemVariants(item, variants map { v => new ResourceLocation(item.getRegistryName+v) }:_*)
		ModelLoader.setCustomMeshDefinition(item, mesh)
	}
	def variantItemModelFactory(variants:List[String], mesh:ItemMeshDefinition)(item:Item) = registerVariantItemModel(item, variants, mesh)
}

object ClientProxy {
	val armorModels = new HashMap[ItemArmor, ModelBiped]
}
