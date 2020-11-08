package poopoodice.techrecon.block.recon;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.Heightmap;
import poopoodice.techrecon.block.ModBlocks;
import poopoodice.techrecon.entity.ModEntities;
import poopoodice.techrecon.entity.ReconUAVEntity;
import poopoodice.techrecon.events.CommonModEventBus;

import java.util.ArrayList;
import java.util.List;

public class ReconUAVTileEntity extends TileEntity implements ITickableTileEntity/*, INamedContainerProvider*/
{
    public int uav;
    private boolean summoned;
    private List<LivingEntity> entityScanned = new ArrayList<>();
    private List<Integer> mapColourData = new ArrayList<>();
    private int updateProgressRows;
    private int updateProgressColumns;

    public static final long BLOCK_MAP_COLOUR_UPDATE_INTERVAL = 2000;
    public static final long UAV_UPDATE_INTERVAL = 60;

    public ReconUAVTileEntity()
    {
        super(CommonModEventBus.RECON_UAV_TILE_ENTITY_TYPE);
    }

    @Override
    public void tick()
    {
        if (world != null && !world.isRemote())
        {
            if (!summoned)
            {
                ReconUAVEntity uavEntity = new ReconUAVEntity(ModEntities.RECON_UAV, world, pos);
                uav = uavEntity.getEntityId();
                if (!world.isRemote())
                    world.addEntity(uavEntity);
                summoned = true;
                markDirty();
                notifyClient();
            }
            if (getUAV() == null)
            {
                world.setBlockState(pos, ModBlocks.EMPTY_CONTROLLER.getDefaultState());
                return;
            }
//            if (world.getGameTime() % BLOCK_MAP_COLOUR_UPDATE_INTERVAL == 0L)
            updateMapColourData();
            if (world.getGameTime() % UAV_UPDATE_INTERVAL == 0L)
                notifyClient();
        }
    }

    public void updateMapColourData()
    {
        if (world == null)
            return;
//        for (int x = (getPos().getX() - ReconUAVEntity.SCAN_RADIUS) + updateProgress * 4; x < (getPos().getX() - ReconUAVEntity.SCAN_RADIUS) + updateProgress * 4 + (updateProgress == 49 ? 3 : 4); x++)
//        {
//            for (int z = (getPos().getZ() - ReconUAVEntity.SCAN_RADIUS) + updateProgress * 4; z < (getPos().getZ() - ReconUAVEntity.SCAN_RADIUS) + updateProgress * 4 + (updateProgress == 49 ? 3 : 4); z++)
//                addOrReplaceData(x - (getPos().getX() - ReconUAVEntity.SCAN_RADIUS), world.getBlockState(new BlockPos(x, world.getChunk(x, z).getTopBlockY(Heightmap.Type.WORLD_SURFACE, x, z), z)).getMaterial().getColor().colorIndex);
//        }
        for (int i=0;i<5;i++)
        {
            int x = updateProgressColumns + getPos().getX() - ReconUAVEntity.SCAN_RADIUS;
            int z = getPos().getZ() - updateProgressRows + ReconUAVEntity.SCAN_RADIUS;
            addOrReplaceData(updateProgressRows * (ReconUAVEntity.SCAN_RADIUS * 2 - 1) + updateProgressColumns,
                    world.getBlockState(new BlockPos(x, world.getChunk(x, z).getTopBlockY(Heightmap.Type.WORLD_SURFACE, x, z), z)).getMaterial().getColor().colorIndex
                    );
            updateProgressColumns++;
            if (updateProgressRows > (ReconUAVEntity.SCAN_RADIUS * 2))
                updateProgressRows = 1;
            if (updateProgressColumns > (ReconUAVEntity.SCAN_RADIUS * 2))
            {
                updateProgressColumns = 1;
                updateProgressRows++;
                markDirty();
                notifyClient();
            }
        }
    }

    private void addOrReplaceData(int index, int data)
    {
        if (mapColourData == null)
            mapColourData = new ArrayList<>();
        if (mapColourData.size() <= index)
            mapColourData.add(data);
        else
            mapColourData.set(index, data);
    }

    public void onScanUpdate(List<LivingEntity> targets)
    {
        entityScanned = targets;
        markDirty();
        notifyClient();
    }

    public List<LivingEntity> getEntityScanned()
    {
        return entityScanned;
    }

    public ReconUAVEntity getUAV()
    {
        if (world == null)
            return null;
        Entity entity = world.getEntityByID(uav);
        return entity instanceof ReconUAVEntity ? (ReconUAVEntity) entity : null;
    }

    public ReconUAVEntity.TargetPredicate getScanTarget()
    {
        return getUAV() == null ? ReconUAVEntity.TargetPredicate.ALL_PLAYERS : getUAV().getTargetPredicate();
    }

    public void setScanTarget(int target)
    {
        setScanTarget(ReconUAVEntity.TargetPredicate.byID(target));
    }

    public void setScanTarget(ReconUAVEntity.TargetPredicate target)
    {
        if (getUAV() != null)
            getUAV().setTargetPredicate(target);
        markDirty();
    }

    public List<Integer> getMapColourData()
    {
        return mapColourData == null ? new ArrayList<>() : new ArrayList<>(mapColourData);
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        return write(new CompoundNBT());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(pos, 7414, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {
        if (world == null)
            return;
        handleUpdateTag(world.getBlockState(pos), pkt.getNbtCompound());
    }

    @Override
    public void read(BlockState state, CompoundNBT compound)
    {
        super.read(state, compound);
        uav = compound.getInt("uav");
        updateProgressRows = compound.getInt("progressrows");
        updateProgressColumns = compound.getInt("progresscolumns");
        summoned = compound.getBoolean("summoned");
        if (world != null)
        {
            for (int i = 0; i < compound.getIntArray("entityID").length; i++)
                entityScanned.add((LivingEntity) world.getEntityByID(compound.getIntArray("entityID")[i]));
            mapColourData = new ArrayList<>();
            for (int colour : compound.getIntArray("colour"))
                mapColourData.add(colour);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        compound.putInt("uav", uav);
        compound.putInt("progressrows", updateProgressRows);
        compound.putInt("progresscolumns", updateProgressColumns);
        compound.putBoolean("summoned", summoned);
        ArrayList<Integer> entityID = new ArrayList<>();
        entityScanned.forEach((entity) -> entityID.add(entity.getEntityId()));
        compound.putIntArray("entityID", entityID);
        compound.putIntArray("colour", mapColourData);
        return super.write(compound);
    }

    public void notifyClient()
    {
        if (world != null && !world.isRemote())
        {
            BlockState state = world.getBlockState(getPos());
            world.notifyBlockUpdate(getPos(), state, state, 3);
        }
    }
}
