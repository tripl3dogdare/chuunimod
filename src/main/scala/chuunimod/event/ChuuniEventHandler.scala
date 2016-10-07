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

class ChuuniEventHandler {
	
	//===== CAPABILITIES =====//
	
	@SubscribeEvent def addPlayerCapabilities(e:AttachCapabilitiesEvent.Entity) {
		if(e.getEntity.isInstanceOf[EntityPlayer])
			e.addCapability(new ResourceLocation(ChuuniMod.MODID, "ManaHandler"), ManaHandler.getHandlerInstance)
	}
	
	@SubscribeEvent def persistPlayerCapabilities(e:PlayerEvent.Clone) {
		if(e.isWasDeath()) {
			val cnew = ManaHandler.instanceFor(e.getEntityPlayer)
			val cold = ManaHandler.instanceFor(e.getOriginal)
			
			cold.copyTo(cnew, false)
		}
	}
	
	//===== PLAYER TICK =====//
	
	@SubscribeEvent def onPlayerTick(e:PlayerTickEvent) {
		if(e.phase != Phase.END) return
		
		updatePlayerMana(e.player)
	}
	
	def updatePlayerMana(player:EntityPlayer) = if(!player.worldObj.isRemote) {
		val mh = ManaHandler.instanceFor(player)
		
		mh.regenMana(mh.manaRegen)
		mh.updateClient(player)
	}
	
}
