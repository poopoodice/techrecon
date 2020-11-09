package poopoodice.techrecon.events;

import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ColorHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import poopoodice.techrecon.block.recon.ReconUAVScreen;
import poopoodice.techrecon.block.recon.ReconUAVTileEntity;

import java.awt.*;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientForgeEventBus
{
    public static void openReconUAVScreen(ReconUAVTileEntity reconUAVTileEntity)
    {
        Minecraft.getInstance().displayGuiScreen(new ReconUAVScreen(reconUAVTileEntity));
    }

    @SubscribeEvent
    public static void renderScreenOverlay(RenderGameOverlayEvent event)
    {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL)
        {
            int index = Material.LAVA.getColor().colorIndex;
            Color colour = new Color(MaterialColor.COLORS[index].colorValue);
//            System.out.println(MaterialColor.COLORS[index].colorValue);
            AbstractGui.fill(event.getMatrixStack(), 0, 0, 50, 50, ColorHelper.PackedColor.packColor(255, colour.getRed(), colour.getGreen(), colour.getBlue()));
        }
    }

//    public static void receiveTileEntityUpdatePacket(BlockPos pos, int type, CompoundNBT data)
//    {
//
//    }
}
