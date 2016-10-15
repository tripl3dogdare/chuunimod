package chuunimod.item

import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import net.minecraft.client.gui.GuiScreen
import chuunimod.ItemRegistry
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.relauncher.SideOnly
import net.minecraft.client.resources.I18n
import net.minecraftforge.fml.relauncher.Side
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraft.item.Item
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.Entity
import net.minecraft.world.World

abstract class ItemArmorPairingManaWeapon(
	val baseAttackDmg:Float,
	val baseAttackSpd:Double,
	val baseManaCost:Float,
	val baseCooldown:Int,
	val armorItem:ItemArmor
) extends ItemManaWeapon with ItemArmorPairingWeapon {
	@SideOnly(Side.CLIENT)
	override def addInformation(stack:ItemStack, player:EntityPlayer, tooltip:java.util.List[String], flag:Boolean) {
		super.addInformation(stack, player, tooltip, flag)
		
		if(GuiScreen.isShiftKeyDown()) {
			tooltip.add("")
			tooltip.add(I18n.format("item.chuunimod.manaweapon.armor", I18n.format(armorItem.getUnlocalizedName+".name")))
			
			if(super.getAttackDmg(stack) != getArmoredAttackDmg(stack))
				tooltip.add(I18n.format("item.chuunimod.manaweapon.atkdmg", getArmoredAttackDmg(stack).toString))
			if(super.getAttackSpd(stack) != getArmoredAttackSpd(stack))
				tooltip.add(I18n.format("item.chuunimod.manaweapon.atkspd", getArmoredAttackSpd(stack).toString))
			if(super.getManaCost(stack) != getArmoredManaCost(stack))
				tooltip.add(I18n.format("item.chuunimod.manaweapon.manacost", (getArmoredManaCost(stack)*20).toString))
			if(super.getCooldown(stack) != getArmoredCooldown(stack))
				tooltip.add(I18n.format("item.chuunimod.manaweapon.cooldown", (getArmoredCooldown(stack).toFloat/20).toString))
		}
	}
	
	final override def onActiveAttack(stack:ItemStack, target:EntityLivingBase, attacker:EntityPlayer) { onActiveAttack(stack, target, attacker, armorEquipped(stack)) }
	def onActiveAttack(stack:ItemStack, target:EntityLivingBase, attacker:EntityPlayer, armorEquipped:Boolean) {}
	
	final override def getAttackDmg(stack:ItemStack) = if(armorEquipped(stack)) getArmoredAttackDmg(stack) else super.getAttackDmg(stack)
	final override def getAttackSpd(stack:ItemStack) = if(armorEquipped(stack)) getArmoredAttackSpd(stack) else super.getAttackSpd(stack)
	final override def getManaCost(stack:ItemStack) = if(armorEquipped(stack)) getArmoredManaCost(stack) else super.getManaCost(stack)
	final override def getCooldown(stack:ItemStack) = if(armorEquipped(stack)) getArmoredCooldown(stack) else super.getCooldown(stack)
	
	def getArmoredAttackDmg(stack:ItemStack) = super.getAttackDmg(stack)
	def getArmoredAttackSpd(stack:ItemStack) = super.getAttackSpd(stack)
	def getArmoredManaCost(stack:ItemStack) = super.getManaCost(stack)
	def getArmoredCooldown(stack:ItemStack) = super.getCooldown(stack)
}
