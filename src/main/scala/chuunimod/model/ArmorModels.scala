package chuunimod.model

import net.minecraft.client.model.ModelRenderer
import net.minecraft.client.model.ModelBiped

class ModelYuutaArmor(modelSize:Float=.2f, texW:Int=64, texH:Int=64) extends ModelBiped(modelSize, 0, texW, texH) {
	private val codTail:ModelRenderer = new ModelRenderer(this, 16, 32)
	
	codTail.addBox(-4.0F, 10.3F, -2.0F, 8, 14, 4, modelSize)
	bipedBody.addChild(codTail)
}

class ModelDekoArmor(modelSize:Float=.7f, texW:Int=64, texH:Int=64) extends ModelBiped(modelSize, 0, texW, texH) {
	private val ttHair = new ModelRenderer(this, 0, 32)
	
	ttHair.addBox(-6.0F, -8.0F, 4.1F, 12, 32, 0, modelSize+0.5F)
	bipedHeadwear.addChild(ttHair)
}
