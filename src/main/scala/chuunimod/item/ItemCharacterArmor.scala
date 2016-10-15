package chuunimod.item

import com.google.common.collect.HashMultimap

import chuunimod.ChuuniMod
import chuunimod.ClientProxy
import chuunimod.MiscRegistry
import net.minecraft.client.model.ModelBiped
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.EnumHelper
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class ItemCharacterArmor(name:String, slot:EntityEquipmentSlot) extends ItemArmor(ItemCharacterArmor.createArmorMaterial(name), -1, slot) with ItemChuuniItem {
	this.setCreativeTab(MiscRegistry.tabMain)
	
	@SideOnly(Side.CLIENT)
	override def getArmorModel(entity:EntityLivingBase, stack:ItemStack, slot:EntityEquipmentSlot, default:ModelBiped) =
		if(ClientProxy.armorModels.containsKey(this)) ClientProxy.armorModels.get(this) else default

	/** Hides item attributes on the client side so you don't get an orphaned "When on [slot]..." with no stats */
	override def getAttributeModifiers(slot:EntityEquipmentSlot, stack:ItemStack) = FMLCommonHandler.instance.getEffectiveSide match {
		case Side.CLIENT => HashMultimap.create[String, AttributeModifier]
		case Side.SERVER => super.getAttributeModifiers(slot, stack)
	}
}

object ItemCharacterArmor {
	def createArmorMaterial(name:String) = EnumHelper.addArmorMaterial(ChuuniMod.MODID+":"+name, ChuuniMod.MODID+":"+name, 0, Array[Int](0,0,0,0), 0, null, 0f)
}
