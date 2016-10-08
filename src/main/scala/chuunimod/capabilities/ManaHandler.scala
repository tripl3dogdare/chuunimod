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
	
	def setMana(amt:Float) = mana = amt
	def setMaxMana(amt:Float) = maxMana = amt
	def setManaRegen(amt:Float) = manaRegen = amt
	
	def consumeMana(amt:Float):Boolean = if(mana < amt) false else { setMana(mana-amt); true }
	def regenMana(amt:Float):Boolean = if(mana == maxMana) false else { setMana(Math.min(mana+amt, maxMana)); true }
	
	def copyTo(other:ManaHandlerLike, copyCurrent:Boolean=true) { 
		if(copyCurrent) other.setMana(mana)
		other.setMaxMana(maxMana)
		other.setManaRegen(manaRegen)
	}
}

abstract class ManaHandler(var mana:Float = 0, var maxMana:Float = 0, var manaRegen:Float = 0) extends ManaHandlerLike {
	private var dirty = true
	
	override def setMana(amt:Float) = { super.setMana(amt); dirty = true }
	override def setMaxMana(amt:Float) = { super.setMaxMana(amt); dirty = true }
	override def setManaRegen(amt:Float) = { super.setManaRegen(amt); dirty = true }
	
	def updateClient(player:EntityPlayer) = 
		if(!player.worldObj.isRemote && dirty) { ChuuniMod.network.sendTo(new MessageUpdateClientMana(this), player.asInstanceOf[EntityPlayerMP]); dirty = false }
}

object ManaHandler {
	def instanceFor(player:EntityPlayer) = player.getCapability(Capabilities.MANA, null)
	def getHandlerInstance = new DefaultManaHandler
	def getStorageInstance = new DefaultManaHandler.Storage
	def getHandlerFactory = new Callable[DefaultManaHandler] { def call = new DefaultManaHandler }
}
	
class DefaultManaHandler(cur:Float=0,max:Float=250,regen:Float=.25f) extends ManaHandler(cur,max,regen) with ICapabilitySerializable[NBTTagCompound] {
	def hasCapability(capability:Capability[_], f:EnumFacing) = capability == Capabilities.MANA
	def getCapability[T](capability:Capability[T], f:EnumFacing) = { if(capability == Capabilities.MANA) this else null }.asInstanceOf[T]
	
	def serializeNBT:NBTTagCompound = new NBTManaHandler(this).nbt
	def deserializeNBT(nbt:NBTTagCompound) = new NBTManaHandler(nbt).copyTo(this)
}

object DefaultManaHandler {
	class Storage extends IStorage[ManaHandler] {
		def writeNBT(cap:Capability[ManaHandler], ins:ManaHandler, f:EnumFacing) = ins.asInstanceOf[DefaultManaHandler].serializeNBT
		def readNBT(cap:Capability[ManaHandler], ins:ManaHandler, f:EnumFacing, nbt:NBTBase) = ins.asInstanceOf[DefaultManaHandler].deserializeNBT(nbt.asInstanceOf[NBTTagCompound])
	}
}

class NBTManaHandler(val nbt:NBTTagCompound) extends ManaHandlerLike {
	var mana = nbt.getFloat("mana")
	var maxMana = nbt.getFloat("maxMana")
	var manaRegen = nbt.getFloat("manaRegen")
	
	def this(mh:ManaHandlerLike) = this({
		val nbt = new NBTTagCompound
		nbt.setFloat("mana", mh.mana)
		nbt.setFloat("maxMana", mh.maxMana)
		nbt.setFloat("manaRegen", mh.manaRegen)
		nbt
	})
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
				val player = Minecraft.getMinecraft.thePlayer
				val mh = ManaHandler.instanceFor(player)
				
				msg.copyTo(mh)
			}})
			null
		}
	}
	
	def register(net:SimpleNetworkWrapper, id:Int) = net.registerMessage(classOf[MessageUpdateClientMana.Handler], classOf[MessageUpdateClientMana], id, Side.CLIENT)
}
