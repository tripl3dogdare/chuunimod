package chuunimod.capabilities

import chuunimod.capabilities.LevelHandler.MessageUpdateClientHandler
import chuunimod.util.MiscUtils.map2nbtcomp
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper

trait LevelHandlerLike extends HandlerLike[LevelHandlerLike] {
	var level,exp,maxLevel:Int
	def expToNext = if(level == maxLevel) 0 else Math.pow(level, 1.1).toInt*100
	def isLevelMaxed = level == maxLevel
	
	protected var (lastLevel,lastExp,lastMax) = (level,exp,maxLevel)
	def hasChanged = (level,exp,maxLevel) != (lastLevel,lastExp,lastMax)
	protected def updateLast = { lastLevel = level; lastExp = exp; lastMax = maxLevel }
	
	def setLevel(lvl:Int) = updateLevelInfo(level = lvl)
	def setExp(xp:Int) = updateLevelInfo(exp = xp)
	def setMaxLevel(lvl:Int) = updateLevelInfo(maxLevel = lvl)
	def addLevel(lvl:Int) = setLevel(level+lvl)
	def addExp(xp:Int) = setExp(exp+xp)
	
	protected def updateLevelInfo(level:Int=this.level, exp:Int=this.exp, maxLevel:Int=this.maxLevel) {
		updateLast
		
		this.maxLevel = maxLevel
		this.level = Math.min(level, maxLevel)
		this.exp = exp
		
		while(this.exp >= expToNext && !isLevelMaxed) { 
			this.exp -= expToNext
			this.level += 1
		}
		
		if(isLevelMaxed) this.exp = 0
	}
	
	def copyTo(other:LevelHandlerLike) { other.setMaxLevel(maxLevel); other.setLevel(level); other.setExp(exp) }
}

class LevelHandler(val player:EntityPlayer=null) extends CapabilityBase[LevelHandler](LevelHandler.CAP _) with LevelHandlerLike {
	import LevelHandler._
	var (level,exp,maxLevel) = (1,0,100)
	
	def onTick(e:PlayerTickEvent) {
		if(ready && level != lastLevel) {
			MinecraftForge.EVENT_BUS.post(LevelUpEvent(player, lastLevel, level))
			if(level == maxLevel) MinecraftForge.EVENT_BUS.post(LevelMaxEvent(player, lastLevel, level))
		}
		
		if(hasChanged) { updateClient; updateLast }
	}
	
	def serializeNBT:NBTTagCompound = Map("level" -> level, "exp" -> exp, "maxLevel" -> maxLevel)
	def getClientUpdatePacket = new MessageUpdateClient(this.serializeNBT)
}

object LevelHandler extends CapabilityCompanion[LevelHandler, LevelHandlerLike] {
	def CAP = Capabilities.LEVEL
	val NAME = "LevelHandler"
	
	def newInstance(player:EntityPlayer) = new LevelHandler(player)
	val handlerFactory = () => new LevelHandler
	
	class MessageUpdateClient(nbt:NBTTagCompound) extends MessageUpdateClientBase(nbt) { def this() = this(null) }
	class MessageUpdateClientHandler extends MessageUpdateClientHandlerBase[MessageUpdateClient]
	def registerClientUpdatePacket(net:SimpleNetworkWrapper, id:Int) = super.registerClientUpdatePacket[MessageUpdateClientHandler,MessageUpdateClient](net, id)
	
	case class LevelUpEvent(val player:EntityPlayer, val oldLevel:Int, val newLevel:Int) extends Event
	case class LevelMaxEvent(val player:EntityPlayer, val oldLevel:Int, val newLevel:Int) extends Event
}


