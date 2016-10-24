package chuunimod

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.Mod.Instance
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.network.NetworkRegistry

@Mod(modid = ChuuniMod.MODID, name = ChuuniMod.NAME, version = ChuuniMod.VERSION, dependencies = ChuuniMod.DEPS, modLanguage = "scala")
object ChuuniMod {
	final val MODID   = "chuunimod"
	final val NAME    = "The Chuunibyou Mod"
	final val VERSION = "INDEV"
	final val DEPS    = ""
	
	@Instance(MODID) var instance = null
	@SidedProxy(clientSide = "chuunimod.ClientProxy", serverSide = "chuunimod.ServerProxy") var proxy:ServerProxy = null
	val network = NetworkRegistry.INSTANCE.newSimpleChannel(ChuuniMod.MODID)
	
	@EventHandler def preInit(e:FMLPreInitializationEvent) {
		Registry.preInit  
		proxy.preInit
	}

}
