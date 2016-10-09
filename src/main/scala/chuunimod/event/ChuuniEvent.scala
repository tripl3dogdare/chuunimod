package chuunimod.event

import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraft.entity.player.EntityPlayer
import chuunimod.capabilities.LevelHandler

class ChuuniLevelUpEvent(val player:EntityPlayer, val lh:LevelHandler) extends Event
