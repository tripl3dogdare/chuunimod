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
import net.minecraftforge.event.entity.living.LivingDeathEvent
import java.util.Random
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.boss.EntityWither
import java.util.concurrent.ThreadLocalRandom
import net.minecraft.entity.monster.EntityGhast
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.common.MinecraftForge

class ChuuniEventHandler {
	
	//===== CAPABILITIES =====//
	
	@SubscribeEvent def addPlayerCapabilities(e:AttachCapabilitiesEvent.Entity) {
		if(e.getEntity.isInstanceOf[EntityPlayer]) {
			e.addCapability(new ResourceLocation(ChuuniMod.MODID, "ManaHandler"), ManaHandler.getHandlerInstance)
			e.addCapability(new ResourceLocation(ChuuniMod.MODID, "LevelHandler"), LevelHandler.getHandlerInstance)
		}
	}
	
	@SubscribeEvent def persistPlayerCapabilities(e:PlayerEvent.Clone) {
		ManaHandler.instanceFor(e.getOriginal).copyTo(ManaHandler.instanceFor(e.getEntityPlayer), !e.isWasDeath)
		LevelHandler.instanceFor(e.getOriginal).copyTo(LevelHandler.instanceFor(e.getEntityPlayer))
	}
	
	@SubscribeEvent def updateClientOnJoinWorld(e:EntityJoinWorldEvent) {
		val player = if(e.getEntity.isInstanceOf[EntityPlayer]) e.getEntity.asInstanceOf[EntityPlayer] else return
		
		ManaHandler.instanceFor(player).updateClient(player, true)
		LevelHandler.instanceFor(player).updateClient(player, true)
	}
	
	//===== PLAYER TICK =====//
	
	@SubscribeEvent def onPlayerTick(e:PlayerTickEvent) {
		if(e.phase != Phase.END) return
		
		if(!e.player.worldObj.isRemote) {
			updatePlayerMana(e.player)
			updatePlayerLevel(e.player)
		}
	}
	
	def updatePlayerMana(player:EntityPlayer) {
		val mh = ManaHandler.instanceFor(player)
		
		if(!player.isDead) mh.regenMana(mh.manaRegen)
		mh.updateClient(player)
	}
	
	def updatePlayerLevel(player:EntityPlayer) {
		val lh = LevelHandler.instanceFor(player)

		if(lh.shouldFireLevelUpEvent) MinecraftForge.EVENT_BUS.post(new ChuuniLevelUpEvent(player, lh))
		lh.updateClient(player)
	}
	
	//===== PLAYER KILLS ENEMY =====//
	
	@SubscribeEvent def onPlayerKillEnemy(e:LivingDeathEvent) {
		if(!e.getSource.getEntity.isInstanceOf[EntityPlayer]) return
		
		if(Set("player","fireball","arrow") contains e.getSource.getDamageType) {
			val player = e.getSource.getEntity.asInstanceOf[EntityPlayer]
			val lh = LevelHandler.instanceFor(player)
			
			val expRange:(Float,Float) = e.getEntityLiving match {
				case _:EntityDragon => (95000,150000)
				case _:EntityWither => (4000,6000)
				case _:EntityGhast if e.getSource.getDamageType == "fireball" => (750,1250)
				case _:EntityPlayer => (250,750)
				case _:EntityMob => (50,150)
				case _ => (25,75)
			}
			lh.addExp(ThreadLocalRandom.current.nextDouble(expRange._1, expRange._2).toFloat)
		}
	}
	
	//===== CHUUNI EVENTS =====//
	
	@SubscribeEvent def onPlayerLevelUp(e:ChuuniLevelUpEvent) {
		val mh = ManaHandler.instanceFor(e.player)
		mh.setMaxMana(250*e.lh.level)
	}
	
}
