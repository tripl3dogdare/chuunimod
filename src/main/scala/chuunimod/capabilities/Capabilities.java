package chuunimod.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class Capabilities {
	@CapabilityInject(ManaHandler.class) public static final Capability<ManaHandler> MANA = null;
	@CapabilityInject(LevelHandler.class) public static final Capability<LevelHandler> LEVEL = null;
}
