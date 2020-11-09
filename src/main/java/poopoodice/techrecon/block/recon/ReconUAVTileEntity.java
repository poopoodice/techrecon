package poopoodice.techrecon.block.recon;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
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
    public boolean scanEffect;
    public boolean showMyPos;
    public boolean showUAVPos;
    public int uavColour;

//    private int updateProgressRows;
//    private int updateProgressColumns;

    public static final long BLOCK_MAP_COLOUR_UPDATE_INTERVAL = 400;
    public static final long UAV_UPDATE_INTERVAL = 20;

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
        if (world == null || !(world instanceof ServerWorld))
            return;
        mapColourData.clear();
        mapColourData = new ArrayList<>();
        for (int x = getPos().getX() - ReconUAVEntity.SCAN_RADIUS; x < getPos().getX() + ReconUAVEntity.SCAN_RADIUS; x++)
        {
            for (int z = getPos().getZ() + ReconUAVEntity.SCAN_RADIUS; z > getPos().getZ() - ReconUAVEntity.SCAN_RADIUS; z--)
                mapColourData.add(world.getBlockState(new BlockPos(x, world.getChunk(new BlockPos(x, 0, z)).getTopBlockY(Heightmap.Type.WORLD_SURFACE, x, z), z)).getMaterial().getColor().colorIndex);
        }
//        Biome[] biomes = new Biome[(int) Math.pow(ReconUAVEntity.SCAN_RADIUS * 2, 2)];
//        for (int x = 0; x < ReconUAVEntity.SCAN_RADIUS * 2; x++)
//        {
//            for (int z = 0; z < ReconUAVEntity.SCAN_RADIUS * 2; z++)
//                biomes[z * ReconUAVEntity.SCAN_RADIUS * 2 + x] = world.getBiome(new BlockPos(toWorldX(x), 0, toWorldZ(z)));
//        }
//        for (int graphicalX = 0; graphicalX < ReconUAVEntity.SCAN_RADIUS * 2; graphicalX++)
//        {
//            for (int graphicalZ = 0; graphicalZ < ReconUAVEntity.SCAN_RADIUS * 2; graphicalZ++)
//            {
//                MaterialColor materialcolor = MaterialColor.AIR;
//                int colourOffset = 3;
//                if ((graphicalX > 0 && graphicalX < ReconUAVEntity.SCAN_RADIUS * 2 - 1 && graphicalZ > 0 && graphicalZ < ReconUAVEntity.SCAN_RADIUS * 2 - 1))
//                {
//                    int worldX = toWorldX(graphicalX);
//                    int worldZ = toWorldZ(graphicalZ);
//                    BlockPos worldPos = new BlockPos(worldX, 0, worldZ);
//                    int y = world.getChunk(worldPos).getTopBlockY(Heightmap.Type.WORLD_SURFACE, graphicalX, graphicalZ);
//                    worldPos.add(0, y, 0);
//                    Biome biome = biomes[getIndex(graphicalX, graphicalZ)];
//                    int colourDepth = 8;
//                    if (checkDepth(biomes, graphicalX - 1, graphicalZ - 1))
//                        --colourDepth;
//                    if (checkDepth(biomes, graphicalX - 1, graphicalZ + 1))
//                        --colourDepth;
//                    if (checkDepth(biomes, graphicalX - 1, graphicalZ))
//                        --colourDepth;
//                    if (checkDepth(biomes, graphicalX + 1, graphicalZ - 1))
//                        --colourDepth;
//                    if (checkDepth(biomes, graphicalX + 1, graphicalZ + 1))
//                        --colourDepth;
//                    if (checkDepth(biomes, graphicalX + 1, graphicalZ))
//                        --colourDepth;
//                    if (checkDepth(biomes, graphicalX, graphicalZ - 1))
//                        --colourDepth;
//                    if (checkDepth(biomes, graphicalX, graphicalZ + 1))
//                        --colourDepth;
//                    if (biome.getDepth() < 0.0F)
//                    {
//                        materialcolor = MaterialColor.ADOBE;
//                        if (colourDepth > 7 && graphicalZ % 2 == 0)
//                        {
//                            colourOffset = (graphicalZ + (int) (MathHelper.sin((float) graphicalZ + 0.0F) * 7.0F)) / 8 % 5;
//                            if (colourOffset == 3)
//                                colourOffset = 1;
//                            else if (colourOffset == 4)
//                                colourOffset = 0;
//                        }
//                        else if (colourDepth > 7)
//                            materialcolor = MaterialColor.AIR;
//                        else if (colourDepth > 5)
//                            colourOffset = 1;
//                        else if (colourDepth > 3)
//                            colourOffset = 0;
//                        else if (colourDepth > 1)
//                            colourOffset = 0;
//                    }
//                    else if (colourDepth > 0)
//                    {
//                        materialcolor = MaterialColor.BROWN;
//                        if (colourDepth > 3)
//                            colourOffset = 1;
//                        else
//                            colourOffset = 3;
//                    }
//                }
//                mapColourData.add(materialcolor.colorIndex * 4 + colourOffset);
//            }
//        }
    }

    private int toWorldX(int x)
    {
        return x - ReconUAVEntity.SCAN_RADIUS + getPos().getX();
    }

    private int toWorldZ(int y)
    {
        return ReconUAVEntity.SCAN_RADIUS - y + getPos().getZ();
    }

    private boolean checkDepth(Biome[] biomes, int graphicalX, int graphicalZ)
    {
        return biomes[getIndex(graphicalX, graphicalZ)].getDepth() >= 0.0F;
    }

    private int getIndex(int graphicalX, int graphicalZ)
    {
        return graphicalX + graphicalZ * ReconUAVEntity.SCAN_RADIUS * 2;
    }

    public void onScanUpdate(List<LivingEntity> targets)
    {
        entityScanned = targets;
        markDirty();
        notifyClient();
    }

    public List<LivingEntity> getEntityScanned()
    {
        return new ArrayList<>(entityScanned);
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
        summoned = compound.getBoolean("summoned");
        if (world != null)
        {
            for (int i = 0; i < compound.getIntArray("entityID").length; i++)
                entityScanned.add((LivingEntity) world.getEntityByID(compound.getIntArray("entityID")[i]));
            mapColourData = new ArrayList<>();
            for (int colour : compound.getIntArray("colour"))
                mapColourData.add(colour);
        }
        scanEffect = compound.getBoolean("scaneffect");
        showMyPos = compound.getBoolean("showmypos");
        showUAVPos = compound.getBoolean("showuavpos");
        uavColour = compound.getInt("uavcolour");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        compound.putInt("uav", uav);
        compound.putBoolean("summoned", summoned);
        ArrayList<Integer> entityID = new ArrayList<>();
        entityScanned.forEach((entity) -> entityID.add(entity.getEntityId()));
        compound.putIntArray("entityID", entityID);
        compound.putIntArray("colour", mapColourData);
        compound.putBoolean("scaneffect", scanEffect);
        compound.putBoolean("showmypos", showMyPos);
        compound.putBoolean("showuavpos", showUAVPos);
        compound.putInt("uavcolour", uavColour);
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
