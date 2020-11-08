package poopoodice.techrecon.entity.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import poopoodice.techrecon.TechRecon;

public class EmptyRenderer<T extends Entity> extends EntityRenderer<T>
{
    private static final ResourceLocation TEXTURE = new ResourceLocation(TechRecon.MODID, "textures/entity/recon_uav.png");

    public EmptyRenderer(EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn);
    }

    @Override
    public void render(T entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
    {
    }

    @Override
    public ResourceLocation getEntityTexture(T entity)
    {
        return TEXTURE;
    }
}
