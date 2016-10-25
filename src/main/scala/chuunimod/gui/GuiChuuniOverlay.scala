package chuunimod.gui

import chuunimod.capabilities.LevelHandler
import chuunimod.capabilities.ManaHandler
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.resources.I18n
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class GuiChuuniOverlay extends Gui {
	
	@SubscribeEvent def onRenderGameOverlay(e:RenderGameOverlayEvent.Post) {
		if(e.getType != ElementType.EXPERIENCE) return
		val mc = Minecraft.getMinecraft
		val fr = mc.fontRendererObj
		
		val mh = ManaHandler.instanceFor(mc.thePlayer)
		drawString(fr, I18n.format("gui.chuunimod.mana", mh.mana.toInt.asInstanceOf[Object], mh.maxMana.toInt.asInstanceOf[Object]), 10, 10, 16777215)
		
		val lh = LevelHandler.instanceFor(mc.thePlayer)
		drawString(fr, I18n.format("gui.chuunimod.level", lh.level.asInstanceOf[Object]), 10, 20, 16777215)
		drawString(fr, I18n.format("gui.chuunimod.exp", lh.exp.toInt.asInstanceOf[Object], lh.expToNext.toInt.asInstanceOf[Object]), 10, 30, 16777215)
	}
	
}
