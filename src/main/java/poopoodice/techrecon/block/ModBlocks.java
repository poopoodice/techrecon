package poopoodice.techrecon.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.registries.IForgeRegistry;
import poopoodice.techrecon.block.recon.ReconUAVBlock;

public class ModBlocks
{
    public static Block EMPTY_CONTROLLER;
    public static Block RECON_UAV;

    public static void registerAllItems(IForgeRegistry<Item> registry)
    {
        registry.registerAll(
                new BlockItem(RECON_UAV, new Item.Properties().group(ItemGroup.MISC)).setRegistryName("recon_uav")
        );
    }

    public static void registerAllBlocks(IForgeRegistry<Block> registry)
    {
        registry.registerAll(
                EMPTY_CONTROLLER = new Block(AbstractBlock.Properties.create(Material.ROCK).hardnessAndResistance(35.0F, 3600000.0F)).setRegistryName("empty_controller"),
                RECON_UAV = new ReconUAVBlock(AbstractBlock.Properties.create(Material.ROCK).hardnessAndResistance(35.0F, 3600000.0F)).setRegistryName("recon_uav")
        );
    }
}
