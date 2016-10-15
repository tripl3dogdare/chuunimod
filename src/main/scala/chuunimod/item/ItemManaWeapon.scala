package chuunimod.item

import chuunimod.capabilities.ManaHandler
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.resources.I18n
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumHand
import net.minecraft.world.World
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

trait ItemManaWeapon extends ItemChuuniItem {
	this.setMaxStackSize(1)
	this.defaultTag.setBoolean("active", false)
	this.defaultTag.setBoolean("selected", false)
	
	val baseAttackDmg:Float
	val baseAttackSpd:Double
	val baseManaCost:Float
	val baseCooldown:Int
	
	def onActiveAttack(stack:ItemStack, target:EntityLivingBase, attacker:EntityPlayer) {}
	def getAttackDmg(stack:ItemStack) = baseAttackDmg
	def getAttackSpd(stack:ItemStack) = baseAttackSpd
	def getManaCost(stack:ItemStack) = baseManaCost
	def getCooldown(stack:ItemStack) = baseCooldown
	
	@SideOnly(Side.CLIENT)
	override def addInformation(stack:ItemStack, player:EntityPlayer, tooltip:java.util.List[String], flag:Boolean) {
		super.addInformation(stack, player, tooltip, flag)
		tooltip.add("")
		
		if(GuiScreen.isShiftKeyDown()) {
			tooltip.add(I18n.format("item.chuunimod.manaweapon.atkdmg", baseAttackDmg.toString))
			tooltip.add(I18n.format("item.chuunimod.manaweapon.atkspd", baseAttackSpd.toString))
			tooltip.add(I18n.format("item.chuunimod.manaweapon.manacost", (baseManaCost*20).toString))
			tooltip.add(I18n.format("item.chuunimod.manaweapon.cooldown", (baseCooldown.toFloat/20).toString))
		} else tooltip.add(I18n.format("item.chuunimod.manaweapon.hidden"))
	}
	
	override def hitEntity(stack:ItemStack, target:EntityLivingBase, attacker:EntityLivingBase):Boolean = {
		if(!attacker.worldObj.isRemote && attacker.isInstanceOf[EntityPlayer] && stack.getTagCompound.getBoolean("active"))
			onActiveAttack(stack, target, attacker.asInstanceOf[EntityPlayer])
		false
	}
	
	override def onItemRightClick(stack:ItemStack, world:World, player:EntityPlayer, hand:EnumHand) = {
		if(hand == EnumHand.MAIN_HAND) {
			if(!stack.getTagCompound.getBoolean("active"))
				stack.getTagCompound.setBoolean("active", true)
			else if(player.isSneaking) {
				stack.getTagCompound.setBoolean("active", false)
				player.getCooldownTracker.setCooldown(this, getCooldown(stack))
			}
			
			new ActionResult(EnumActionResult.SUCCESS, stack)
		} else new ActionResult(EnumActionResult.PASS, stack)
	}
	
	override def onUpdate(stack:ItemStack, world:World, entity:Entity, slot:Int, selected:Boolean) {
		super.onUpdate(stack, world, entity, slot, selected)
		val player = entity.asInstanceOf[EntityPlayer]
		val mh = ManaHandler.instanceFor(player)
		
		if(stack.getTagCompound.getBoolean("active") && !mh.consumeMana(getManaCost(stack))) {
			stack.getTagCompound.setBoolean("active", false)
			player.getCooldownTracker.setCooldown(this, getCooldown(stack))
		}
		
		stack.getTagCompound.setBoolean("selected", selected)
	}
	
	override def getAttributeModifiers(slot:EntityEquipmentSlot, stack:ItemStack) = FMLCommonHandler.instance.getEffectiveSide match {
		case Side.CLIENT => super.getAttributeModifiers(slot, stack)
		case Side.SERVER => {
			val map = super.getAttributeModifiers(slot, stack)
			
			if(slot == EntityEquipmentSlot.MAINHAND && stack.hasTagCompound && stack.getTagCompound.getBoolean("active")) {
				map.put(SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName, 
					new AttributeModifier(ItemAttributeModifiers.ATTACK_DAMAGE_MODIFIER, "Weapon modifier", getAttackDmg(stack), 0))
				map.put(SharedMonsterAttributes.ATTACK_SPEED.getAttributeUnlocalizedName, 
					new AttributeModifier(ItemAttributeModifiers.ATTACK_SPEED_MODIFIER, "Weapon modifier", -(4-getAttackSpd(stack)), 0))
			}
			
			map
		}
	}
	
	override def getHighlightTip(stack:ItemStack, displayName:String) = 
		if(stack.getTagCompound.getBoolean("active") && !stack.getTagCompound.getBoolean("selected")) "" else displayName
	
	override def shouldCauseReequipAnimation(oldStack:ItemStack, newStack:ItemStack, slotChanged:Boolean) =
		if(!slotChanged && oldStack.hasTagCompound && newStack.hasTagCompound &&
			oldStack.getItem.isInstanceOf[ItemManaWeapon] && newStack.getItem.isInstanceOf[ItemManaWeapon])
				oldStack.getTagCompound.getBoolean("active") != newStack.getTagCompound.getBoolean("active")
		else super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged)
}
