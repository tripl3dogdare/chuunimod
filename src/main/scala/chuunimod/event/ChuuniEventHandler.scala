package chuunimod.event

import chuunimod.ChuuniMod
import chuunimod.capabilities.ManaHandler
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent
import chuunimod.capabilities.LevelHandler

class ChuuniEventHandler {
	
	//===== CAPABILITIES =====//
	
	@SubscribeEvent def addPlayerCapabilities(e:AttachCapabilitiesEvent.Entity) {
		if(e.getEntity.isInstanceOf[EntityPlayer]) {
			e.addCapability(new ResourceLocation(ChuuniMod.MODID, "ManaHandler"), ManaHandler.getHandlerInstance)
			e.addCapability(new ResourceLocation(ChuuniMod.MODID, "LevelHandler"), LevelHandler.getHandlerInstance)
		}
	}
	
	@SubscribeEvent def persistPlayerCapabilities(e:PlayerEvent.Clone) {
		if(e.isWasDeath()) {
			ManaHandler.instanceFor(e.getOriginal).copyTo(ManaHandler.instanceFor(e.getEntityPlayer), false)
			LevelHandler.instanceFor(e.getOriginal).copyTo(LevelHandler.instanceFor(e.getEntityPlayer))
		}
	}
	
	//===== PLAYER TICK =====//
	
	@SubscribeEvent def onPlayerTick(e:PlayerTickEvent) {
		if(e.phase != Phase.END || e.player.isDead) return
		
		updatePlayerMana(e.player)
	}
	
	def updatePlayerMana(player:EntityPlayer) = if(!player.worldObj.isRemote) {
		val mh = ManaHandler.instanceFor(player)
		
		mh.regenMana(mh.manaRegen)
		mh.updateClient(player)
	}
	
}
