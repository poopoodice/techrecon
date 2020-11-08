package poopoodice.techrecon.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import poopoodice.techrecon.block.recon.ReconUAVTileEntity;
import poopoodice.techrecon.entity.ReconUAVEntity;

import java.util.function.Supplier;

public class UpdateReconUAVTileEntityPredicateMessage
{
    private final BlockPos blockPos;
    private final int predicate;

    public UpdateReconUAVTileEntityPredicateMessage(ReconUAVTileEntity reconUAVTileEntity)
    {
        this(reconUAVTileEntity.getPos(), reconUAVTileEntity.getScanTarget());
    }

    public UpdateReconUAVTileEntityPredicateMessage(BlockPos blockPos, ReconUAVEntity.TargetPredicate predicate)
    {
        this(blockPos, predicate.getID());
    }

    public UpdateReconUAVTileEntityPredicateMessage(BlockPos blockPos, int predicate)
    {
        this.blockPos = blockPos;
        this.predicate = predicate;
    }

    public static void encode(UpdateReconUAVTileEntityPredicateMessage msg, PacketBuffer packetBuffer)
    {
        packetBuffer.writeBlockPos(msg.blockPos);
        packetBuffer.writeInt(msg.predicate);
    }

    public static UpdateReconUAVTileEntityPredicateMessage decode(PacketBuffer packetBuffer)
    {
        return new UpdateReconUAVTileEntityPredicateMessage(packetBuffer.readBlockPos(), packetBuffer.readInt());
    }

    public static void handle(UpdateReconUAVTileEntityPredicateMessage message, Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null)
            {
                TileEntity tileEntity = player.world.getTileEntity(message.blockPos);
                if (tileEntity instanceof ReconUAVTileEntity)
                    ((ReconUAVTileEntity) tileEntity).setScanTarget(message.predicate);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
