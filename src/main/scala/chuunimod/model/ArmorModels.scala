package chuunimod.model

import net.minecraft.client.model.ModelRenderer
import net.minecraft.client.model.ModelBiped

class ModelYuutaArmor(modelSize:Float=.2f, texW:Int=64, texH:Int=64) extends ModelBiped(modelSize, 0, texW, texH) {
	private val codTail:ModelRenderer = new ModelRenderer(this, 16, 32)
	
	codTail.addBox(-4.0F, 10.3F, -2.0F, 8, 14, 4, modelSize)
	bipedBody.addChild(codTail)
}
