package chuunimod.capabilities

import chuunimod.util.MiscUtils.map2nbtcomp
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.event.entity.player.PlayerEvent
import chuunimod.event.ManaFullEvent
import chuunimod.event.ManaEmptyEvent

trait ManaHandlerLike extends HandlerLike[ManaHandlerLike] {
	var mana,maxMana,manaRegen:Float
	
	protected var (lastMana,lastMax,lastRegen) = (mana,maxMana,manaRegen)
	def hasChanged = (lastMana,lastMax,lastRegen) != (mana,maxMana,manaRegen)
	protected def updateLast = { lastMana = mana; lastMax = maxMana; lastRegen = manaRegen }
	
	def setMana(amt:Float) = { updateLast; mana = Math.min(amt, maxMana) }
	def setMaxMana(amt:Float) = { updateLast; maxMana = amt }
	def setManaRegen(amt:Float) = { updateLast; manaRegen = amt }
	
	def consumeMana(amt:Float):Boolean = if(mana < amt) false else { setMana(mana-amt); true }
	def regenMana(amt:Float):Boolean = if(mana == maxMana) false else { setMana(Math.min(mana+amt, maxMana)); true }
	
	def copyTo(other:ManaHandlerLike) { 
		other.setMana(mana)
		other.setMaxMana(maxMana)
		other.setManaRegen(manaRegen)
	}
}

class ManaHandler(val player:EntityPlayer=null) extends CapabilityBase[ManaHandler](ManaHandler.CAP _) with ManaHandlerLike {
	import ManaHandler._
	var (mana,maxMana,manaRegen) = (0f,250f,.25f)
	
	def onTick(e:PlayerTickEvent) {
		if(!e.player.worldObj.isRemote) regenMana(manaRegen)
		
		if(ready && mana != lastMana) {
			if(mana == 0) MinecraftForge.EVENT_BUS.post(new ManaEmptyEvent(player, mana))
			if(mana == maxMana) MinecraftForge.EVENT_BUS.post(new ManaFullEvent(player, mana))
		}
		
		if(hasChanged) { updateClient; updateLast }
	}
	
	def serializeNBT:NBTTagCompound = Map("mana" -> mana, "maxMana" -> maxMana, "manaRegen" -> manaRegen)
	def getClientUpdatePacket = new MessageUpdateClient(this.serializeNBT)
}

object ManaHandler extends CapabilityCompanion[ManaHandler, ManaHandlerLike] {
	def CAP = Capabilities.MANA
	val NAME = "ManaHandler"
	
	def newInstance(player:EntityPlayer) = new ManaHandler(player)
	val handlerFactory = () => new ManaHandler
	
	class MessageUpdateClient(nbt:NBTTagCompound) extends MessageUpdateClientBase(nbt) { def this() = this(null) }
	class MessageUpdateClientHandler extends MessageUpdateClientHandlerBase[MessageUpdateClient]
	def registerClientUpdatePacket(net:SimpleNetworkWrapper, id:Int) = super.registerClientUpdatePacket[MessageUpdateClientHandler,MessageUpdateClient](net, id)
	
	override def persistOnPlayer(e:PlayerEvent.Clone) { super.persistOnPlayer(e); if(e.isWasDeath) instanceFor(e.getEntityPlayer).setMana(0) }
}
