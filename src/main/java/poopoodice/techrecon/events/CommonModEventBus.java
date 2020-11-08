package poopoodice.techrecon.events;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import poopoodice.techrecon.block.ModBlocks;
import poopoodice.techrecon.block.recon.ReconUAVTileEntity;
import poopoodice.techrecon.entity.ModEntities;
import poopoodice.techrecon.item.ModItems;
import poopoodice.techrecon.packets.TechReconPackets;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonModEventBus
{
    public static TileEntityType<ReconUAVTileEntity> RECON_UAV_TILE_ENTITY_TYPE;
//    public static ContainerType<ReconUAVContainer> RECON_UAV_CONTAINER_TYPE;

    @SubscribeEvent
    public static void onSetupCommon(FMLCommonSetupEvent event)
    {
        TechReconPackets.registerAllPackets();
    }

    @SubscribeEvent
    public static void onItemsRegister(RegistryEvent.Register<Item> event)
    {
        ModItems.registerAll(event.getRegistry());
        ModBlocks.registerAllItems(event.getRegistry());
    }

    @SubscribeEvent
    public static void onBlocksRegister(RegistryEvent.Register<Block> event)
    {
        ModBlocks.registerAllBlocks(event.getRegistry());
    }

    @SubscribeEvent
    public static void onEntitiesRegister(RegistryEvent.Register<EntityType<?>> event)
    {
        ModEntities.registerAll(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event)
    {
        RECON_UAV_TILE_ENTITY_TYPE = TileEntityType.Builder.create(ReconUAVTileEntity::new, ModBlocks.RECON_UAV).build(null);
        event.getRegistry().registerAll(
                RECON_UAV_TILE_ENTITY_TYPE.setRegistryName("recon_uav_tile_entity")
        );
    }

//    @SubscribeEvent
//    public static void registerContainer(RegistryEvent.Register<ContainerType<?>> event)
//    {
//        RECON_UAV_CONTAINER_TYPE = new ContainerType<>(ReconUAVContainer::new);
//        event.getRegistry().registerAll(
//                RECON_UAV_CONTAINER_TYPE.setRegistryName("recon_uav_container")
//        );
//    }
}
