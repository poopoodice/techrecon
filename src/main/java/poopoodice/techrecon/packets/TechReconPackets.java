package poopoodice.techrecon.packets;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import poopoodice.techrecon.TechRecon;

public class TechReconPackets
{

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(TechRecon.MODID, "main"),
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals
    );

    public static void registerAllPackets()
    {
        int id = 0;
        INSTANCE.registerMessage(++id, UpdateReconUAVTileEntityPredicateMessage.class, UpdateReconUAVTileEntityPredicateMessage::encode, UpdateReconUAVTileEntityPredicateMessage::decode, UpdateReconUAVTileEntityPredicateMessage::handle);
    }
}
