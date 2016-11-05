package chuunimod.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Event;

public class LevelUpEvent extends Event {
	
	public final EntityPlayer player;
	public final int oldLevel;
	public final int newLevel;
	
	public LevelUpEvent(EntityPlayer player, int oldLevel, int newLevel) {
		this.player = player;
		this.oldLevel = oldLevel;
		this.newLevel = newLevel;
	}

}
