package poopoodice.techrecon.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Color;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class TestItem extends Item
{
    public TestItem()
    {
        super(new Properties());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn)
    {
        int x = playerIn.getPosition().getX();
        int z = playerIn.getPosition().getZ();
        int y = worldIn.getChunk(x, z).getTopBlockY(Heightmap.Type.WORLD_SURFACE, x, z);
        BlockPos pos = new BlockPos(x, y, z);
        System.out.println("y " + y);
        System.out.println("pos " + pos);
        System.out.println("colour value   " + worldIn.getBlockState(pos).getMaterial().getColor().colorValue);
        System.out.println("colour value 2 " + worldIn.getBlockState(pos).getMaterialColor(worldIn, pos).colorValue);
        System.out.println("hex " + Color.fromInt(worldIn.getBlockState(pos).getMaterialColor(worldIn, pos).colorValue).getColor());
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }
}
