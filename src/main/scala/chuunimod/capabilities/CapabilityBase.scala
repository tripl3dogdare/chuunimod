package chuunimod.capabilities

import chuunimod.ChuuniMod
import chuunimod.util.MiscUtils.func2runnable
import chuunimod.util.MiscUtils.nbtcomp2map
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.Capability.IStorage
import net.minecraftforge.common.capabilities.ICapabilitySerializable
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent
import net.minecraftforge.fml.common.network.ByteBufUtils
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper
import net.minecraftforge.fml.relauncher.Side
import scala.reflect.macros.blackbox.Context

abstract class CapabilityBase[T <: CapabilityBase[T]](val CAP:() => Capability[T]) extends ICapabilitySerializable[NBTTagCompound] {
	val player:EntityPlayer
	private var ticksAttached = 0
	final def ready = ticksAttached > 20
	
	def hasCapability(capability:Capability[_], f:EnumFacing) = capability == CAP()
	def getCapability[T](capability:Capability[T], f:EnumFacing) = { if(capability == CAP()) this else null }.asInstanceOf[T]
	
	def updateClient:Unit = 
		if(!player.worldObj.isRemote) ChuuniMod.network.sendTo(getClientUpdatePacket, player.asInstanceOf[EntityPlayerMP])
	def getClientUpdatePacket:IMessage
		
	@SubscribeEvent final def onTick_(e:PlayerTickEvent) = { if(e.player == player) onTick(e); ticksAttached += 1 }
	protected def onTick(e:PlayerTickEvent)
	
	def deserializeNBT(nbt:NBTTagCompound) = (nbt:Map[String,Any]) foreach { case (key,value) => {
		this.getClass.getMethods.find(_.getName == key + "_$eq") match {
			case Some(s) => s.invoke(this, value.asInstanceOf[AnyRef])
			case None =>
		}
	}}
}

abstract class StorageBase[T <: CapabilityBase[T]] extends IStorage[T] {
	def writeNBT(cap:Capability[T], ins:T, f:EnumFacing) = ins.serializeNBT
	def readNBT(cap:Capability[T], ins:T, f:EnumFacing, nbt:NBTBase) = ins.deserializeNBT(nbt.asInstanceOf[NBTTagCompound])
}

trait CapabilityCompanion[T <: CapabilityBase[T] with HandlerLike[H], H <: HandlerLike[H]] {
	def CAP:Capability[T]
	val NAME:String
	
	def newInstance(player:EntityPlayer):T
	val handlerFactory:() => T
	
	def instanceFor(player:EntityPlayer):T = player.getCapability(CAP, null)
	class Storage extends StorageBase[T]
	
	def attachToPlayer(e:AttachCapabilitiesEvent.Entity) {
			val handler = newInstance(e.getEntity.asInstanceOf[EntityPlayer])
			e.addCapability(new ResourceLocation(ChuuniMod.MODID, NAME), handler)
			MinecraftForge.EVENT_BUS.register(handler)
	}
	
	def persistOnPlayer(e:PlayerEvent.Clone) {
		instanceFor(e.getOriginal).copyTo(instanceFor(e.getEntityPlayer).asInstanceOf[H])
		MinecraftForge.EVENT_BUS.unregister(instanceFor(e.getOriginal))
	}
	
	abstract class MessageUpdateClientBase(var nbt:NBTTagCompound) extends IMessage {
		def toBytes(buf:ByteBuf) = ByteBufUtils.writeTag(buf, nbt)
		def fromBytes(buf:ByteBuf) = nbt = ByteBufUtils.readTag(buf)
	}
	
	abstract class MessageUpdateClientHandlerBase[T <: MessageUpdateClientBase] extends IMessageHandler[T, IMessage] {
		def onMessage(msg:T, context:MessageContext) = {
			Minecraft.getMinecraft.addScheduledTask(() => {
				val player = Minecraft.getMinecraft.thePlayer
				instanceFor(player).deserializeNBT(msg.nbt)
			})
			null
		}
	}

	def registerClientUpdatePacket[H <: MessageUpdateClientHandlerBase[M], M <: MessageUpdateClientBase]
		(net:SimpleNetworkWrapper, id:Int)(implicit handler:Manifest[H], msg:Manifest[M]) = 
			net.registerMessage(handler.erasure.asInstanceOf[Class[H]], msg.erasure.asInstanceOf[Class[M]], id, Side.CLIENT)
	def registerClientUpdatePacket(net:SimpleNetworkWrapper, id:Int)
}

trait HandlerLike[T <: HandlerLike[T]] { def copyTo(other:T) }
