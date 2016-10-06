package chuunimod

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.Mod.EventHandler

@Mod(modid = "chuunimod", name = "The Chuunibyou Mod", version = "INDEV", dependencies = "", modLanguage = "scala")
object ChuuniMod {
	final val MODID   = "chuunimod"
	final val NAME    = "The Chuunibyou Mod"
	final val VERSION = "INDEV"
	final val DEPS    = ""
	
	@SidedProxy(clientSide = "chuunimod.ClientProxy", serverSide = "chuunimod.ServerProxy")
	var proxy:ServerProxy = null
	
	@EventHandler
	def preInit(e:FMLPreInitializationEvent) {
		Registry.preInit  
		proxy.preInit
	}

}
