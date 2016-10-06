package chuunimod

import net.minecraft.block.material.Material
import net.minecraft.item.ItemBlock
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraft.creativetab.CreativeTabs

class Block(name:String, mat:Material, tab:CreativeTabs=null) extends net.minecraft.block.Block(mat) {
	setUnlocalizedName(name)
	setRegistryName(name)
	if(tab != null) setCreativeTab(tab)
	
	val itemBlock = GameRegistry.register(new ItemBlock(this).setUnlocalizedName(name).setRegistryName(name))
}

class Item(name:String, tab:CreativeTabs=null) extends net.minecraft.item.Item {
	setUnlocalizedName(name)
	setRegistryName(name)
	if(tab != null) setCreativeTab(tab)
}
