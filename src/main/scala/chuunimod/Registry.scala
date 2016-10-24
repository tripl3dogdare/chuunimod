package chuunimod

import chuunimod.item.ItemArmorPairingManaWeapon
import chuunimod.item.ItemCharacterArmor
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.registry.GameRegistry

object Registry {
	def preInit {
		MiscRegistry.preInit
		ItemRegistry.preInit
		BlockRegistry.preInit
		CraftingRegistry.preInit
	}
}

object MiscRegistry {
	val tabMain:CreativeTabs = new CreativeTabs("chuunimod.main") { def getTabIconItem = ItemRegistry.itemRikkaArmor }
	
	def preInit {}
}

object ItemRegistry {
	val itemRikkaArmor = register(new ItemCharacterArmor("tweseal", EntityEquipmentSlot.HEAD), "tweseal")
	
	val itemRikkaWeapon = register(new ItemArmorPairingManaWeapon(10.0f, 1d, 1, 200, itemRikkaArmor) {
		override def getArmoredAttackDmg(stack:ItemStack) = baseAttackDmg*2
	}, "sspm2")
	
	def preInit {}
	
	def register[T <: Item](item:T, name:String=null):T = {
		if(name != null) item.setUnlocalizedName(ChuuniMod.MODID+"."+name).setRegistryName(name)
		GameRegistry.register(item)
	}
}

object BlockRegistry { def preInit {} }
object CraftingRegistry { def preInit {} }
