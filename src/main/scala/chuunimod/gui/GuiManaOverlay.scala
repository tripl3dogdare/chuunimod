package chuunimod.gui

import chuunimod.capabilities.ManaHandler
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.resources.I18n
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class GuiManaOverlay extends Gui {
	
	@SubscribeEvent def onRenderGameOverlay(e:RenderGameOverlayEvent.Post) {
		if(e.getType() != ElementType.EXPERIENCE) return;
		val mc = Minecraft.getMinecraft();
		val fr = mc.fontRendererObj;
		val mh = mc.thePlayer.getCapability(ManaHandler.CAP, null);
		
		drawString(fr, I18n.format("gui.chuunimod.mana", mh.mana.toInt.asInstanceOf[Object], mh.maxMana.toInt.asInstanceOf[Object]), 10, 10, 16777215)
	}
	
}
