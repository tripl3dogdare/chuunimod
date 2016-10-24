package chuunimod.event

import java.util.concurrent.ThreadLocalRandom

import chuunimod.ChuuniMod
import chuunimod.capabilities.LevelHandler
import chuunimod.capabilities.ManaHandler
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.monster.EntityGhast
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent

class ChuuniEventHandler {
	
	//===== CAPABILITIES =====//
	
	@SubscribeEvent def addPlayerCapabilities(e:AttachCapabilitiesEvent.Entity) {
		if(e.getEntity.isInstanceOf[EntityPlayer]) {
			ManaHandler.attachToPlayer(e)
			LevelHandler.attachToPlayer(e)
		}
	}
	
	@SubscribeEvent def persistPlayerCapabilities(e:PlayerEvent.Clone) {
		ManaHandler.persistOnPlayer(e)
		LevelHandler.persistOnPlayer(e)
	}
	
	@SubscribeEvent def updateClientOnJoinWorld(e:EntityJoinWorldEvent) {
		val player = if(e.getEntity.isInstanceOf[EntityPlayer]) e.getEntity.asInstanceOf[EntityPlayer] else return
		
		ManaHandler.instanceFor(player).updateClient
		LevelHandler.instanceFor(player).updateClient
	}
	
	//===== HURT/KILL ENEMY =====//
	
	@SubscribeEvent def onPlayerKillEnemy(e:LivingDeathEvent) {
		if(!e.getSource.getEntity.isInstanceOf[EntityPlayer]) return
		
		if(Set("player","fireball","arrow") contains e.getSource.getDamageType) {
			val player = e.getSource.getEntity.asInstanceOf[EntityPlayer]
			val lh = LevelHandler.instanceFor(player)
			
			val expRange:(Int,Int) = e.getEntityLiving match {
				case _:EntityDragon => (95000,150000)
				case _:EntityWither => (4000,6000)
				case _:EntityGhast if e.getSource.getDamageType == "fireball" => (750,1250)
				case _:EntityPlayer => (250,750)
				case _:EntityMob => (50,150)
				case _ => (25,75)
			}
			lh.addExp(ThreadLocalRandom.current.nextInt(expRange._1, expRange._2))
		}
	}
	
	//===== CHUUNI EVENTS =====//
	
	@SubscribeEvent def onPlayerLevelUp(e:LevelHandler.LevelUpEvent) {
		val mh = ManaHandler.instanceFor(e.player)
		mh.setMaxMana(250*e.newLevel) //TODO: balance and change regen (change from max +250 per level to max *2 on even, regen *2 on odd?)
		
		if(e.player.worldObj.isRemote && e.newLevel > e.oldLevel) e.player.addChatComponentMessage(new TextComponentString("You leveled up!"))
	}
	
	@SubscribeEvent def onPlayerLevelMax(e:LevelHandler.LevelMaxEvent) {
		if(e.player.worldObj.isRemote) e.player.addChatComponentMessage(new TextComponentString("You hit the maximum level!"))
	}
	
}
