package chuunimod.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ManaFullEvent extends Event {

	public final EntityPlayer player;
	public final float mana;
	
	public ManaFullEvent(EntityPlayer player, float mana) {
		this.player = player;
		this.mana = mana;
	}
	
}
