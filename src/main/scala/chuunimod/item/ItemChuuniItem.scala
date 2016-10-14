package chuunimod.item

import java.util.Random

import scala.collection.JavaConversions.asScalaSet

import net.minecraft.client.resources.I18n
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.SideOnly
import net.minecraftforge.fml.relauncher.Side

trait ItemChuuniItem extends Item {
	protected val defaultTag:NBTTagCompound = new NBTTagCompound
	
	@SideOnly(Side.CLIENT)
	override def addInformation(stack:ItemStack, player:EntityPlayer, tooltip:java.util.List[String], flag:Boolean) {
		if(I18n.hasKey(stack.getItem.getUnlocalizedName+".quote") && stack.hasTagCompound && stack.getTagCompound.getBoolean("useQuote"))
			tooltip.add(I18n.format(stack.getItem.getUnlocalizedName+".quote"))
		else tooltip.add(I18n.format(stack.getItem.getUnlocalizedName+".lore"))
	}
	
	override def onUpdate(stack:ItemStack, world:World, entity:Entity, slot:Int, selected:Boolean) {
		if(!stack.hasTagCompound) {
			stack.setTagCompound(defaultTag.copy)
			stack.getTagCompound.setBoolean("useQuote", new Random().nextInt(100) < 10)
		} else for(key <- defaultTag.getKeySet) {
			if(!stack.getTagCompound.hasKey(key)) stack.getTagCompound.setTag(key, defaultTag.getTag(key).copy)
		}
	}
}
