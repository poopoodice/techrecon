package poopoodice.techrecon.item;

import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

public class ModItems
{
    public static Item TEST_ITEM;
    public static void registerAll(IForgeRegistry<Item> registry)
    {
        registry.registerAll(
                TEST_ITEM = new TestItem().setRegistryName("test_item")
        );
    }
}
