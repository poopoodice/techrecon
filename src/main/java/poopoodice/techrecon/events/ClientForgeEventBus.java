package poopoodice.techrecon.events;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import poopoodice.techrecon.block.recon.ReconUAVScreen;
import poopoodice.techrecon.block.recon.ReconUAVTileEntity;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientForgeEventBus
{
    public static void openReconUAVScreen(ReconUAVTileEntity reconUAVTileEntity)
    {
        Minecraft.getInstance().displayGuiScreen(new ReconUAVScreen(reconUAVTileEntity));
    }

//    @SubscribeEvent
    public static void renderScreenOverlay(RenderGameOverlayEvent event)
    {
//        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL)
//        {
//            PlayerEntity player = Minecraft.getInstance().player;
//            if (player == null)
//                return;
//            int colour = player.world.getBlockState(new BlockPos(-176, 63, 136)).getMaterial().getColor().colorValue;
//            String hex = Integer.toHexString(colour);
//            int f1nal =  Color.decode(hex).getRGB();
//            System.out.println(f1nal);
//            AbstractGui.fill(event.getMatrixStack(), 0, 0, 250, 250, ColorHelper.PackedColor.packColor(255, 255, 255, 255));
//        }
    }

//    public static void receiveTileEntityUpdatePacket(BlockPos pos, int type, CompoundNBT data)
//    {
//
//    }
}
