package poopoodice.techrecon.entity;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.registries.IForgeRegistry;

public class ModEntities
{
    public static final EntityType<ReconUAVEntity>
            RECON_UAV = (EntityType<ReconUAVEntity>) EntityType.Builder.<ReconUAVEntity>create(ReconUAVEntity::new, EntityClassification.MISC)
            .size(1.5F, 1.0F)
            .build("recon_uav")
            .setRegistryName("recon_uav");

    public static void registerAll(IForgeRegistry<EntityType<?>> registry)
    {
        registry.registerAll(
                RECON_UAV
        );
    }
}
