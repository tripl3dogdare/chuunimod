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
	val itemYuutaArmor = register(new ItemCharacterArmor("cloakod", EntityEquipmentSlot.CHEST), "cloakod")
	val itemToukaArmor = register(new ItemCharacterArmor("tpcollar", EntityEquipmentSlot.CHEST), "tpcollar")
	val itemDekoArmor = register(new ItemCharacterArmor("twintails", EntityEquipmentSlot.HEAD), "twintails")
	val itemShichiArmor = register(new ItemCharacterArmor("longscarf", EntityEquipmentSlot.CHEST), "longscarf")
	
	val itemRikkaWeapon = register(new ItemArmorPairingManaWeapon(10.0f, 1d, 1, 200, itemRikkaArmor) {
		override def getArmoredAttackDmg(stack:ItemStack) = baseAttackDmg*2
	}, "sspm2")
	
	val itemYuutaWeapon = register(new ItemArmorPairingManaWeapon(10.0f, 1d, 1, 200, itemYuutaArmor) {
		override def getArmoredCooldown(stack:ItemStack) = baseCooldown/2
	}, "dfblade")
	
	val itemToukaWeapon = register(new ItemArmorPairingManaWeapon(10.0f, 1d, 1, 200, itemToukaArmor) {
		override def getArmoredManaCost(stack:ItemStack) = baseManaCost/2
	}, "ladle")
	
	val itemDekoWeapon = register(new ItemArmorPairingManaWeapon(10.0f, 1d, 1, 200, itemDekoArmor) {
		override def getArmoredAttackSpd(stack:ItemStack) = baseAttackSpd*2
	}, "mjolnir")
	
	val itemShichiWeapon = register(new ItemArmorPairingManaWeapon(10.0f, 1d, 1, 200, itemShichiArmor) {
		override def getArmoredAttackSpd(stack:ItemStack) = baseAttackSpd*2
	}, "sophiawand")
	
	def preInit {}
	
	def register[T <: Item](item:T, name:String=null):T = {
		if(name != null) item.setUnlocalizedName(ChuuniMod.MODID+"."+name).setRegistryName(name)
		GameRegistry.register(item)
	}
}

object BlockRegistry { def preInit {} }
object CraftingRegistry { def preInit {} }
