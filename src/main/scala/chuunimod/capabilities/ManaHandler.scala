package chuunimod.capabilities

import net.minecraftforge.common.capabilities.ICapabilitySerializable
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.Capability
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability.IStorage
import net.minecraft.nbt.NBTBase
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import io.netty.buffer.ByteBuf
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.client.Minecraft

trait ManaHandler {
	var mana,maxMana,manaRegen:Float = 0
	var dirty:Boolean = true
	
	def set(mana:Float=this.mana, maxMana:Float=this.mana, manaRegen:Float=this.manaRegen) {
		this.mana = mana
		this.maxMana = maxMana
		this.manaRegen = this.manaRegen
		dirty = true
	}
	
	def consumeMana(amt:Float):Boolean = if(mana < amt) false else { set(mana=mana-amt); true; }
	def regenMana(amt:Float):Boolean = if(mana == maxMana) false else { set(mana=Math.min(mana+manaRegen, maxMana)); true; }
	
}

object ManaHandler {
	@CapabilityInject(classOf[ManaHandler]) final val CAP:Capability[ManaHandler] = null
	
	class DefaultHandler(cur:Float=0,max:Float=250,regen:Float=.25f) extends ManaHandler with ICapabilitySerializable[NBTTagCompound] {
		set(mana=cur, maxMana=max, manaRegen=regen)
		
		def hasCapability(capability:Capability[_], f:EnumFacing) = { capability == ManaHandler.CAP; }
		def getCapability[T](capability:Capability[T], f:EnumFacing) = { if(capability == ManaHandler.CAP) this else null }.asInstanceOf[T]
		
		def serializeNBT:NBTTagCompound = {
			val nbt = new NBTTagCompound
			nbt.setFloat("mana", mana)
			nbt.setFloat("maxMana", maxMana)
			nbt.setFloat("manaRegen", manaRegen)
			nbt
		}
		
		def deserializeNBT(nbt:NBTTagCompound) = set(
			mana = nbt.getFloat("mana"),
			maxMana = nbt.getFloat("maxMana"),
			manaRegen = nbt.getFloat("manaRegen")
		)
	}
	
	object DefaultHandler {
		def get = new DefaultHandler
	}
	
	class DefaultStorage extends IStorage[ManaHandler] {
		def writeNBT(cap:Capability[ManaHandler], ins:ManaHandler, f:EnumFacing) = ins.asInstanceOf[DefaultHandler].serializeNBT
		def readNBT(cap:Capability[ManaHandler], ins:ManaHandler, f:EnumFacing, nbt:NBTBase) = ins.asInstanceOf[DefaultHandler].deserializeNBT(nbt.asInstanceOf[NBTTagCompound])
	}
}
	
class MessageUpdateClientMana extends IMessage {
	var mana,maxMana,manaRegen:Float = 0
	
	def this(mh:ManaHandler) { this; this.mana = mh.mana; this.maxMana = mh.maxMana; this.manaRegen = mh.manaRegen }
	
	def fromBytes(buf:ByteBuf) { this.mana = buf.readFloat; this.maxMana = buf.readFloat; this.manaRegen = buf.readFloat }
	def toBytes(buf:ByteBuf) { List(this.mana, this.maxMana, this.manaRegen) foreach buf.writeFloat }
	
	class Handler extends IMessageHandler[MessageUpdateClientMana, IMessage] {
		def onMessage(msg:MessageUpdateClientMana, ctx:MessageContext):IMessage = {
			Minecraft.getMinecraft.addScheduledTask(new Runnable { def run = () => {
				val player = Minecraft.getMinecraft().thePlayer;
				val mh = player.getCapability(ManaHandler.CAP, null)
				
				mh.set(mana=msg.mana, maxMana=msg.maxMana, manaRegen=msg.manaRegen)
			}})
			null
		}
	}
}
