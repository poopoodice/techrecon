package poopoodice.techrecon.block.recon;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import poopoodice.techrecon.block.ModBlocks;
import poopoodice.techrecon.entity.ModEntities;
import poopoodice.techrecon.entity.ReconUAVEntity;
import poopoodice.techrecon.events.CommonModEventBus;

import java.util.ArrayList;
import java.util.List;

public class ReconUAVTileEntity extends TileEntity implements ITickableTileEntity
{
    public int uav;
    private boolean summoned;
    private List<LivingEntity> entityScanned = new ArrayList<>();
    private List<Integer> mapColourData = new ArrayList<>();
//    private int updateProgressRows;
//    private int updateProgressColumns;

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
                updateMapColourData();
                markDirty();
                notifyClient();
            }
            else if (world.getGameTime() % BLOCK_MAP_COLOUR_UPDATE_INTERVAL == 0L)
                updateMapColourData();
            if (getUAV() == null)
            {
                world.setBlockState(pos, ModBlocks.EMPTY_CONTROLLER.getDefaultState());
                return;
            }
            if (world.getGameTime() % UAV_UPDATE_INTERVAL == 0L)
                notifyClient();
        }
    }

    public void updateMapColourData()
    {
        if (world == null)
            return;
//        for (int x = (getPos().getX() - ReconUAVEntity.SCAN_RADIUS); x < (getPos().getX() - ReconUAVEntity.SCAN_RADIUS); x++)
//        {
//            for (int z = (getPos().getZ() - ReconUAVEntity.SCAN_RADIUS); z < (getPos().getZ() - ReconUAVEntity.SCAN_RADIUS); z++)
//            {
//                addOrReplaceData(x, world.getBlockState(new BlockPos(x, world.getChunk(x, z).getTopBlockY(Heightmap.Type.WORLD_SURFACE, x, z), z)).getMaterial().getColor().colorIndex);
//
//            }
//        }
//        markDirty();
//        notifyClient();
//        for (int i=0;i<5;i++)
//        {
//            int x = updateProgressColumns + getPos().getX() - ReconUAVEntity.SCAN_RADIUS;
//            int z = getPos().getZ() - updateProgressRows + ReconUAVEntity.SCAN_RADIUS;
//            addOrReplaceData(updateProgressRows * (ReconUAVEntity.SCAN_RADIUS * 2 - 1) + updateProgressColumns,
//                    world.getBlockState(new BlockPos(x, world.getChunk(x, z).getTopBlockY(Heightmap.Type.WORLD_SURFACE, x, z), z)).getMaterial().getColor().colorIndex
//                    );
//            updateProgressColumns++;
//            if (updateProgressRows > (ReconUAVEntity.SCAN_RADIUS * 2))
//                updateProgressRows = 1;
//            if (updateProgressColumns > (ReconUAVEntity.SCAN_RADIUS * 2))
//            {
//                updateProgressColumns = 1;
//                updateProgressRows++;
//                markDirty();
//                notifyClient();
//            }
//        }


        int scale = 1 << 1;
        int centreX = getPos().getX();
        int centreZ = getPos().getZ();
        int graphicalPlayerX = MathHelper.floor(viewer.getPosX() - (double)centreX) / scale + ReconUAVEntity.SCAN_RADIUS;
        int graphicalPlayerZ = MathHelper.floor(viewer.getPosZ() - (double)centreZ) / scale + ReconUAVEntity.SCAN_RADIUS;
        int radius = ReconUAVEntity.SCAN_RADIUS * 2 / scale;
        if (world.getDimensionType().getHasCeiling())
            radius /= 2;
        for(int graphicalX = graphicalPlayerX - radius + 1; graphicalX < graphicalPlayerX + radius; ++graphicalX)
        {
            double d0 = 0.0D;
            for(int graphicalZ = graphicalPlayerZ - radius - 1; graphicalZ < graphicalPlayerZ + radius; ++graphicalZ)
            {
                if (graphicalX >= 0 && graphicalZ >= -1 && graphicalX < ReconUAVEntity.SCAN_RADIUS * 2 && graphicalZ < ReconUAVEntity.SCAN_RADIUS * 2)
                {
                    int distanceX = graphicalX - graphicalPlayerX;
                    int distanceZ = graphicalZ - graphicalPlayerZ;
                    boolean flag1 = distanceX * distanceX + distanceZ * distanceZ > (radius - 2) * (radius - 2);
                    int worldX = (centreX / scale + graphicalX - ReconUAVEntity.SCAN_RADIUS) * scale;
                    int worldZ = (centreZ / scale + graphicalZ - ReconUAVEntity.SCAN_RADIUS) * scale;
                    Multiset<MaterialColor> multiset = LinkedHashMultiset.create();
                    Chunk chunk = world.getChunkAt(new BlockPos(worldX, 0, worldZ));
                    if (!chunk.isEmpty())
                    {
                        ChunkPos chunkpos = chunk.getPos();
                        int xInChunk = worldX & 15;
                        int zInChunk = worldZ & 15;
                        int k3 = 0;
                        double d1 = 0.0D;
                        if (world.getDimensionType().getHasCeiling())
                        {
                            int l3 = worldX + worldZ * 231871;
                            l3 = l3 * l3 * 31287121 + l3 * 11;
                            if ((l3 >> 20 & 1) == 0)
                                multiset.add(Blocks.DIRT.getDefaultState().getMaterialColor(world, BlockPos.ZERO), 10);
                            else
                                multiset.add(Blocks.STONE.getDefaultState().getMaterialColor(world, BlockPos.ZERO), 100);
                            d1 = 100.0D;
                        }
                        else
                        {
                            BlockPos.Mutable blockpos$mutable1 = new BlockPos.Mutable();
                            BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

                            //these should be for the map scaling e.g. 1 pixel -> 4 blocks
                            for(int blockCountX = 0; blockCountX < scale; ++blockCountX)
                            {
                                for(int blockCountZ = 0; blockCountZ < scale; ++blockCountZ)
                                {
                                    int worldPosTopY = chunk.getTopBlockY(Heightmap.Type.WORLD_SURFACE, blockCountX + xInChunk, blockCountZ + zInChunk) + 1;
                                    BlockState blockstate;
                                    if (worldPosTopY <= 1)
                                        blockstate = Blocks.BEDROCK.getDefaultState();
                                    else
                                    {
                                        do
                                        {
                                            --worldPosTopY;
                                            blockpos$mutable1.setPos(chunkpos.getXStart() + blockCountX + xInChunk, worldPosTopY, chunkpos.getZStart() + blockCountZ + zInChunk);
                                            blockstate = chunk.getBlockState(blockpos$mutable1);
                                        }
                                        while(blockstate.getMaterialColor(world, blockpos$mutable1) == MaterialColor.AIR && worldPosTopY > 0);
                                        if (worldPosTopY > 0 && !blockstate.getFluidState().isEmpty())
                                        {
                                            int l4 = worldPosTopY - 1;
                                            blockpos$mutable.setPos(blockpos$mutable1);
                                            BlockState blockstate1;
                                            do
                                                {
                                                blockpos$mutable.setY(l4--);
                                                blockstate1 = chunk.getBlockState(blockpos$mutable);
                                                ++k3;
                                            } while(l4 > 0 && !blockstate1.getFluidState().isEmpty());
                                            blockstate = isWaterLogged(world, blockstate, blockpos$mutable1);
                                        }
                                    }
                                    d1 += (double)worldPosTopY / (double)(scale * scale);
                                    multiset.add(blockstate.getMaterialColor(world, blockpos$mutable1));
                                }
                            }
                        }
                        k3 = k3 / (scale * scale);
                        double d2 = (d1 - d0) * 4.0D / (double)(scale + 4) + ((double)(graphicalX + graphicalZ & 1) - 0.5D) * 0.4D;
                        int i5 = 1;
                        if (d2 > 0.6D)
                            i5 = 2;
                        if (d2 < -0.6D)
                            i5 = 0;
                        MaterialColor materialcolor = Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MaterialColor.AIR);
                        if (materialcolor == MaterialColor.WATER)
                        {
                            d2 = (double)k3 * 0.1D + (double)(graphicalX + graphicalZ & 1) * 0.2D;
                            i5 = 1;
                            if (d2 < 0.5D)
                                i5 = 2;
                            if (d2 > 0.9D)
                                i5 = 0;
                        }
                        d0 = d1;
                        if (graphicalZ >= 0 && distanceX * distanceX + distanceZ * distanceZ < radius * radius && (!flag1 || (graphicalX + graphicalZ & 1) != 0))
                        {
                            byte b0 = data.colors[graphicalX + graphicalZ * 128];
                            byte b1 = (byte)(materialcolor.colorIndex * 4 + i5);
                            if (b0 != b1)
                            {
                                data.colors[graphicalX + graphicalZ * 128] = b1;
                                data.updateMapData(graphicalX, graphicalZ);
                            }
                        }
                    }
                }
            }
        }
        markDirty();
        notifyClient();
    }

    private BlockState isWaterLogged(World worldIn, BlockState state, BlockPos pos)
    {
        FluidState fluidstate = state.getFluidState();
        return !fluidstate.isEmpty() && !state.isSolidSide(worldIn, pos, Direction.UP) ? fluidstate.getBlockState() : state;
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
//        updateProgressRows = compound.getInt("progressrows");
//        updateProgressColumns = compound.getInt("progresscolumns");
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
//        compound.putInt("progressrows", updateProgressRows);
//        compound.putInt("progresscolumns", updateProgressColumns);
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
