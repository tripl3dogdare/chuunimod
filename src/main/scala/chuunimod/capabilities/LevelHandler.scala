package chuunimod.capabilities

import java.util.concurrent.Callable

import chuunimod.ChuuniMod
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.Capability.IStorage
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.ICapabilitySerializable
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper
import net.minecraftforge.fml.relauncher.Side

trait LevelHandlerLike {
	var level,maxLevel:Int
	var exp:Float
	def expToNext = if(level >= maxLevel) 0 else level*level*100
	
	def setLevel(lvl:Int) { level = lvl; updateLevelInfo }
	def setExp(amt:Float) { exp = amt; updateLevelInfo }
	def setMaxLevel(lvl:Int) = maxLevel = lvl
	def addExp(amt:Float) = setExp(exp+amt)
	
	def updateLevelInfo {
		while(exp > expToNext) {
			exp -= expToNext
			level += 1
		}
		
		if(level >= maxLevel) { level = maxLevel; exp = 0 }
	}
	
	def copyTo(lh:LevelHandlerLike) {
		lh.setMaxLevel(maxLevel)
		lh.setLevel(level)
		lh.setExp(exp)
	}
}

abstract class LevelHandler(var level:Int = 0, var exp:Float = 0, var maxLevel:Int = 0) extends LevelHandlerLike {
	private var dirty = true
	
	override def setLevel(lvl:Int) { val old = level; super.setLevel(lvl); dirty = dirty || old != level }
	override def setExp(amt:Float) { val old = exp; super.setExp(amt); dirty = dirty || old != exp }
	override def setMaxLevel(lvl:Int) { val old = maxLevel; super.setMaxLevel(lvl); dirty = dirty || old != maxLevel }

	def updateClient(player:EntityPlayer) = 
		if(!player.worldObj.isRemote && dirty) { ChuuniMod.network.sendTo(new MessageUpdateClientLevel(this), player.asInstanceOf[EntityPlayerMP]); dirty = false }
}

object LevelHandler {
	def instanceFor(player:EntityPlayer) = player.getCapability(Capabilities.LEVEL, null)
	def getHandlerInstance = new DefaultLevelHandler
	def getStorageInstance = new DefaultLevelHandler.Storage
	def getHandlerFactory = new Callable[DefaultLevelHandler] { def call = new DefaultLevelHandler }
}

class DefaultLevelHandler(lvl:Int=1,xp:Float=0,max:Int=100) extends LevelHandler(lvl,xp,max) with ICapabilitySerializable[NBTTagCompound] {
	def hasCapability(capability:Capability[_], f:EnumFacing) = capability == Capabilities.LEVEL
	def getCapability[T](capability:Capability[T], f:EnumFacing) = { if(capability == Capabilities.LEVEL) this else null }.asInstanceOf[T]
	
	def serializeNBT:NBTTagCompound = new NBTLevelHandler(this).nbt
	def deserializeNBT(nbt:NBTTagCompound) = new NBTLevelHandler(nbt).copyTo(this)
}

object DefaultLevelHandler {
	class Storage extends IStorage[LevelHandler] {
		def writeNBT(cap:Capability[LevelHandler], ins:LevelHandler, f:EnumFacing) = ins.asInstanceOf[DefaultLevelHandler].serializeNBT
		def readNBT(cap:Capability[LevelHandler], ins:LevelHandler, f:EnumFacing, nbt:NBTBase) = ins.asInstanceOf[DefaultLevelHandler].deserializeNBT(nbt.asInstanceOf[NBTTagCompound])
	}
}

class NBTLevelHandler(val nbt:NBTTagCompound) extends LevelHandlerLike {
	var level = nbt.getInteger("level")
	var exp = nbt.getFloat("exp")
	var maxLevel = nbt.getInteger("maxLevel")
	
	def this(lh:LevelHandlerLike) = this({
		val nbt = new NBTTagCompound
		nbt.setInteger("level", lh.level)
		nbt.setFloat("exp", lh.exp)
		nbt.setInteger("maxLevel", lh.maxLevel)
		nbt
	})
}

class MessageUpdateClientLevel(lh:LevelHandlerLike) extends IMessage with LevelHandlerLike {
	var level,maxLevel:Int = 0
	var exp:Float = 0
	if(lh != null) lh.copyTo(this)
	
	def this() = this(null)
	
	def fromBytes(buf:ByteBuf) { level = buf.readInt; exp = buf.readFloat; maxLevel = buf.readInt }
	def toBytes(buf:ByteBuf) { buf.writeInt(level); buf.writeFloat(exp); buf.writeInt(maxLevel) }
}

object MessageUpdateClientLevel {
	class Handler extends IMessageHandler[MessageUpdateClientLevel, IMessage] {
		def onMessage(msg:MessageUpdateClientLevel, ctx:MessageContext):IMessage = {
			Minecraft.getMinecraft.addScheduledTask(new Runnable { def run = {
				val player = Minecraft.getMinecraft.thePlayer
				val lh = LevelHandler.instanceFor(player)
				
				msg.copyTo(lh)
			}})
			null
		}
	}
	
	def register(net:SimpleNetworkWrapper, id:Int) = net.registerMessage(classOf[MessageUpdateClientLevel.Handler], classOf[MessageUpdateClientLevel], id, Side.CLIENT)
}
