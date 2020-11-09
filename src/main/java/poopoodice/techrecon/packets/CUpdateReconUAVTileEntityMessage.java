package poopoodice.techrecon.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import poopoodice.techrecon.block.recon.ReconUAVTileEntity;

import java.util.function.Supplier;

public class CUpdateReconUAVTileEntityMessage
{
    private final BlockPos blockPos;
    private final int type;
    private final int data;

    public static final int UPDATE_TARGET_PREDICATE = 0;
    public static final int UPDATE_SCAN_EFFECT = 1;
    public static final int UPDATE_SHOW_MY_POS = 2;
    public static final int UPDATE_SHOW_UAV_POS = 3;
    public static final int UPDATE_UAV_COLOUR = 4;

    public CUpdateReconUAVTileEntityMessage(BlockPos blockPos, int type, int data)
    {
        this.blockPos = blockPos;
        this.type = type;
        this.data = data;
    }

    public static void encode(CUpdateReconUAVTileEntityMessage msg, PacketBuffer packetBuffer)
    {
        packetBuffer.writeBlockPos(msg.blockPos);
        packetBuffer.writeInt(msg.type);
        packetBuffer.writeInt(msg.data);
    }

    public static CUpdateReconUAVTileEntityMessage decode(PacketBuffer packetBuffer)
    {
        return new CUpdateReconUAVTileEntityMessage(packetBuffer.readBlockPos(), packetBuffer.readInt(), packetBuffer.readInt());
    }

    public static void handle(CUpdateReconUAVTileEntityMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null)
            {
                TileEntity tileEntity = player.world.getTileEntity(message.blockPos);
                if (tileEntity instanceof ReconUAVTileEntity)
                {
                    ReconUAVTileEntity reconUAVTileEntity = (ReconUAVTileEntity) tileEntity;
                    int data = message.data;
                    switch (message.type)
                    {
                        case UPDATE_TARGET_PREDICATE:
                        default:
                            reconUAVTileEntity.setScanTarget(data);
                            break;
                        case UPDATE_SCAN_EFFECT:
                            reconUAVTileEntity.scanEffect = bool(message.data);
                            break;
                        case UPDATE_SHOW_MY_POS:
                            reconUAVTileEntity.showMyPos = bool(message.data);
                            break;
                        case UPDATE_SHOW_UAV_POS:
                            reconUAVTileEntity.showUAVPos = bool(message.data);
                            break;
                        case UPDATE_UAV_COLOUR:
                            reconUAVTileEntity.uavColour = message.data;
                            break;
                    }
                    reconUAVTileEntity.markDirty();
                    reconUAVTileEntity.notifyClient();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static boolean bool(int data)
    {
        return data == 1;
    }
}
