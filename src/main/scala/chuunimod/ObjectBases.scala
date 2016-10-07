package chuunimod

import net.minecraft.block.material.Material
import net.minecraft.item.ItemBlock
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.block.Block
import net.minecraft.item.Item

class BlockBase(name:String, mat:Material, tab:CreativeTabs=null) extends Block(mat) {
	setUnlocalizedName(name)
	setRegistryName(name)
	if(tab != null) setCreativeTab(tab)
	
	val itemBlock = GameRegistry.register(new ItemBlock(this).setUnlocalizedName(name).setRegistryName(name))
}

class ItemBase(name:String, tab:CreativeTabs=null) extends Item {
	setUnlocalizedName(name)
	setRegistryName(name)
	if(tab != null) setCreativeTab(tab)
}
