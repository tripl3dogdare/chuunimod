package chuunimod.item

import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import net.minecraft.world.World

trait ItemArmorPairingWeapon extends ItemChuuniItem {
	this.setMaxStackSize(1)
	this.defaultTag.setBoolean("armorEquipped", false)
	
	val armorItem:ItemArmor
	
	def armorEquipped(stack:ItemStack) = stack.getTagCompound.getBoolean("armorEquipped")
	
	override def onUpdate(stack:ItemStack, world:World, entity:Entity, slot:Int, selected:Boolean) {
		super.onUpdate(stack, world, entity, slot, selected)
		if(world.isRemote) return
		
		val player = entity.asInstanceOf[EntityPlayer]
		val armorStack = player.getItemStackFromSlot(armorItem.armorType)
		val armor = if(armorStack != null) armorStack.getItem else null
		
		stack.getTagCompound.setBoolean("armorEquipped", armor == armorItem)
	}
}
