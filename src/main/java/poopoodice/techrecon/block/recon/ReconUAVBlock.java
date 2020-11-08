package poopoodice.techrecon.block.recon;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import poopoodice.techrecon.events.ClientForgeEventBus;

public class ReconUAVBlock extends Block
{
    public ReconUAVBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof ReconUAVTileEntity)
        {
            DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ClientForgeEventBus.openReconUAVScreen((ReconUAVTileEntity) tileentity));
            if (!worldIn.isRemote())
            {
                tileentity.markDirty();
                ((ReconUAVTileEntity) tileentity).notifyClient();
            }
            return ActionResultType.func_233537_a_(worldIn.isRemote);
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new ReconUAVTileEntity();
    }
}
