package chuunimod

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Items

object Registry {
	
	def preInit {
		MiscRegistry.preInit
		ItemRegistry.preInit
		BlockRegistry.preInit
		CraftingRegistry.preInit
	}
	
}

object MiscRegistry {
	var tabMain:CreativeTabs = null
	
	def preInit {
		tabMain = new CreativeTabs("chuunimod.main") { def getTabIconItem = Items.ENDER_EYE }
	}
}

object ItemRegistry { 
	
	def preInit {
	}
}

object BlockRegistry {
	
	def preInit {
	} 
}

object CraftingRegistry { def preInit {} }
