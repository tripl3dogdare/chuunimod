package chuunimod.capabilities

import java.util.concurrent.Callable

import chuunimod.ChuuniMod
import chuunimod.capabilities.MessageUpdateClientMana.Handler
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
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper
import net.minecraftforge.fml.relauncher.Side

trait ManaHandlerLike {
	var mana,maxMana,manaRegen:Float
	var dirty:Boolean = true
	
	def setMana(amt:Float) = { mana = amt; dirty = true }
	def setMaxMana(amt:Float) = { maxMana = amt; dirty = true }
	def setManaRegen(amt:Float) = { manaRegen = amt; dirty = true }
	
	def consumeMana(amt:Float):Boolean = if(mana < amt) false else { setMana(mana-amt); true; }
	def regenMana(amt:Float):Boolean = if(mana == maxMana) false else { setMana(Math.min(mana+amt, maxMana)); true; }
	
	def copyTo(other:ManaHandlerLike, copyCurrent:Boolean=true) { 
		if(copyCurrent) other.setMana(mana)
		other.setMaxMana(maxMana)
		other.setManaRegen(manaRegen)
	}
	
	def updateClient(player:EntityPlayer) = if(!player.worldObj.isRemote) { 
		if(dirty) ChuuniMod.network.sendTo(new MessageUpdateClientMana(this), player.asInstanceOf[EntityPlayerMP])
		dirty = false
	}
}

abstract class ManaHandler(var mana:Float = 0, var maxMana:Float = 0, var manaRegen:Float = 0) extends ManaHandlerLike

object ManaHandler {
	@CapabilityInject(classOf[ManaHandler]) final val CAP:Capability[ManaHandler] = null
	
	def instanceFor(player:EntityPlayer) = player.getCapability(CAP, null)
	def getStorageInstance = new DefaultStorage
	def getHandlerFactory = new Callable[DefaultHandler] { def call = new DefaultHandler }
	
	class DefaultHandler(cur:Float=0,max:Float=250,regen:Float=.25f) extends ManaHandler(cur,max,regen) with ICapabilitySerializable[NBTTagCompound] {
		def hasCapability(capability:Capability[_], f:EnumFacing) = capability == ManaHandler.CAP
		def getCapability[T](capability:Capability[T], f:EnumFacing) = { if(capability == ManaHandler.CAP) this else null }.asInstanceOf[T]
		
		def serializeNBT:NBTTagCompound = {
			val nbt = new NBTTagCompound
			nbt.setFloat("mana", mana)
			nbt.setFloat("maxMana", maxMana)
			nbt.setFloat("manaRegen", manaRegen)
			nbt
		}
		
		def deserializeNBT(nbt:NBTTagCompound) {
			mana = nbt.getFloat("mana")
			maxMana = nbt.getFloat("maxMana")
			manaRegen = nbt.getFloat("manaRegen")
			dirty = true
		}
	}
	
	class DefaultStorage extends IStorage[ManaHandler] {
		def writeNBT(cap:Capability[ManaHandler], ins:ManaHandler, f:EnumFacing) = ins.asInstanceOf[DefaultHandler].serializeNBT
		def readNBT(cap:Capability[ManaHandler], ins:ManaHandler, f:EnumFacing, nbt:NBTBase) = ins.asInstanceOf[DefaultHandler].deserializeNBT(nbt.asInstanceOf[NBTTagCompound])
	}
}
	
class MessageUpdateClientMana(mh:ManaHandlerLike) extends IMessage with ManaHandlerLike {
	var mana,maxMana,manaRegen:Float = 0
	if(mh != null) mh.copyTo(this)
	
	def this() = this(null)
	
	def fromBytes(buf:ByteBuf) { this.mana = buf.readFloat; this.maxMana = buf.readFloat; this.manaRegen = buf.readFloat }
	def toBytes(buf:ByteBuf) { List(this.mana, this.maxMana, this.manaRegen) foreach buf.writeFloat }
}

object MessageUpdateClientMana {
	class Handler extends IMessageHandler[MessageUpdateClientMana, IMessage] {
		def onMessage(msg:MessageUpdateClientMana, ctx:MessageContext):IMessage = {
			Minecraft.getMinecraft.addScheduledTask(new Runnable { def run = {
				val player = Minecraft.getMinecraft().thePlayer;
				val mh = ManaHandler.instanceFor(player)
				
				msg.copyTo(mh)
			}})
			null
		}
	}
	
	def register(net:SimpleNetworkWrapper, id:Int) = net.registerMessage(classOf[MessageUpdateClientMana.Handler], classOf[MessageUpdateClientMana], id, Side.CLIENT)
}
