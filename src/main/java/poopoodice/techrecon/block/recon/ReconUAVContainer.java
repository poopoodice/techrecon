package poopoodice.techrecon.block.recon;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntArray;
import net.minecraft.util.math.BlockPos;
import poopoodice.techrecon.block.ModBlocks;
import poopoodice.techrecon.events.CommonModEventBus;

//public class ReconUAVContainer extends Container
//{
//    private final IWorldPosCallable worldPosCallable;
//    private final BlockPos pos;
//
//    public ReconUAVContainer(int id, PlayerInventory playerInventory)
//    {
//        this(id, IWorldPosCallable.DUMMY, BlockPos.ZERO, new IntArray(4));
//    }
//
//    public ReconUAVContainer(int id, IWorldPosCallable worldPosCallable, BlockPos pos, IIntArray trackArray)
//    {
//        super(CommonModEventBus.RECON_UAV_CONTAINER_TYPE, id);
//        this.worldPosCallable = worldPosCallable;
//        this.pos = pos;
//        trackIntArray(trackArray);
//    }
//
//    @Override
//    public boolean canInteractWith(PlayerEntity playerIn)
//    {
//        return isWithinUsableDistance(worldPosCallable, playerIn, ModBlocks.RECON_UAV);
//    }
//}
