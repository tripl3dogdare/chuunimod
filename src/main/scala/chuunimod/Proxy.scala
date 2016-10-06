package chuunimod

import net.minecraftforge.client.model.ModelLoader
import net.minecraft.client.renderer.block.model.ModelResourceLocation

class ServerProxy {
	def preInit {
		
	}
}

class ClientProxy extends ServerProxy {
	override def preInit {
		super.preInit
		
		registerItemModels
		registerBlockModels
	}
	
	def registerItemModels {
	}
	
	def registerBlockModels {
	}
	
	def registerDefaultItemModel(item:net.minecraft.item.Item) = ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName.toString))
}
