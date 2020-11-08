package poopoodice.techrecon.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import poopoodice.techrecon.block.recon.ReconUAVTileEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ReconUAVEntity extends Entity
{
    protected BlockPos controllerPos;
    protected AxisAlignedBB area;
    protected int durability;
    protected int scanCD;
    protected BlockPos[] targetPositions;
    protected boolean needChangeOfVelocity;
    protected int currentTargetPosition;
    protected int moved;
    protected TargetPredicate targetPredicate;
    public static final int ALTITUDE = 256;
    public static final int RADIUS = 75;
    public static final int TIME_PER_POINT = 20;
    public static final int SEA_LEVEL = 63;

    public ReconUAVEntity(EntityType<? extends ReconUAVEntity> entityTypeIn, World worldIn)
    {
        super(entityTypeIn, worldIn);
    }

    public ReconUAVEntity(EntityType<? extends ReconUAVEntity> entityTypeIn, World worldIn, BlockPos controllerPos)
    {
        super(entityTypeIn, worldIn);
        this.controllerPos = controllerPos;
        area = new AxisAlignedBB(new BlockPos(controllerPos.getX() - 100, SEA_LEVEL, controllerPos.getZ() - 100), new BlockPos(controllerPos.getX() + 100, 255, controllerPos.getZ() + 100));
        setTargetPredicate(TargetPredicate.ALL_PLAYERS);
        setTargetPositions();
        setPosition(targetPositions[0].getX(), ALTITUDE, targetPositions[0].getZ());
        currentTargetPosition = 0;
        worldIn.getPlayers().forEach((player) -> player.setPositionAndUpdate(getPosX(), getPosY(), getPosZ()));
    }

    @Override
    public boolean hasNoGravity()
    {
        return true;
    }

    @Override
    protected void registerData()
    {

    }

    @Override
    public void tick()
    {
        super.tick();
        if (world != null && !world.isRemote())
        {
            setDirection(getMotion());
            TileEntity tileEntity = world.getTileEntity(controllerPos);
            if (!(tileEntity instanceof ReconUAVTileEntity) || ((ReconUAVTileEntity) tileEntity).getUAV() != this)
            {
                remove();
                return;
            }
            scanCD--;
            if (scanCD <= 0)
                notifyTileEntityScanUpdate(scan());
            if (getPosition().equals(getCurrentTargetPosition()) || moved >= TIME_PER_POINT)
            {
                nextTargetPosition();
                needChangeOfVelocity = true;
            }
            move();
        }
    }

    protected void setDirection(Vector3d vec3d)
    {
        float f = MathHelper.sqrt(horizontalMag(vec3d));
        this.rotationYaw = (float)(MathHelper.atan2(vec3d.x, vec3d.z) * (double)(180F / (float)Math.PI));
        this.rotationPitch = (float)(MathHelper.atan2(vec3d.y, f) * (double)(180F / (float)Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
    }

    private void notifyTileEntityScanUpdate(List<Entity> targets)
    {
        ReconUAVTileEntity controller = getController();
        if (controller != null)
            controller.onScanUpdate(targets);
    }

    private void move()
    {
        moved++;
        Vector3d m = getMotion();
        setPositionAndUpdate(getPosX() + m.x, ALTITUDE, getPosZ() + m.z);
        if (needChangeOfVelocity)
        {
            moved = 0;
            setMotion((float) (getCurrentTargetPosition().getX() - getLastTargetPosition().getX()) / TIME_PER_POINT, 0, (float) (getCurrentTargetPosition().getZ() - getLastTargetPosition().getZ()) / TIME_PER_POINT);
            needChangeOfVelocity = false;
        }
    }

    private List<Entity> scan()
    {
        scanCD = 200;
        ArrayList<Entity> visuals = new ArrayList<>();
        for (Entity entity : world.getEntitiesInAABBexcluding(null, area, (entity) -> entity.isAlive() && targetPredicate.getPredicate().test(entity)))
        {
            if (canBeScan(entity))
                visuals.add(entity);
        }
        return visuals;
    }

    private boolean canBeScan(Entity entity)
    {
        if (entity == null || !entity.isAlive())
            return false;
        return world.rayTraceBlocks(new RayTraceContext(getPositionVec(), entity.getPositionVec().add(0.0F, entity.getHeight(), 0.0F), RayTraceContext.BlockMode.VISUAL, RayTraceContext.FluidMode.NONE, null)).getType() == RayTraceResult.Type.MISS;
    }

    public void setTargetPredicate(TargetPredicate predicate)
    {
        targetPredicate = predicate;
    }

    public void setTargetPredicate(String predicate)
    {
        setTargetPredicate(TargetPredicate.valueOf(predicate));
    }

    public void setTargetPositions()
    {
        targetPositions = new BlockPos[8];
        int x = (int) (Math.cos(45) * RADIUS);
        int z = (int) (Math.sin(45) * RADIUS);
        int r = RADIUS;
        setTargetPosition(0, 0, r);
        setTargetPosition(1, x, z);
        setTargetPosition(2, r, 0);
        setTargetPosition(3, x, -z);
        setTargetPosition(4, 0, -r);
        setTargetPosition(5, -x, -z);
        setTargetPosition(6, -r, 0);
        setTargetPosition(7, -x, z);
    }

    protected void setTargetPosition(int index, int x, int z)
    {
        targetPositions[index] = new BlockPos(controllerPos.getX() + x, ALTITUDE, controllerPos.getZ() + z);
    }

    protected void nextTargetPosition()
    {
        currentTargetPosition++;
        if (currentTargetPosition > 7)
            currentTargetPosition = 0;
    }

    public BlockPos getCurrentTargetPosition()
    {
        return targetPositions[currentTargetPosition];
    }

    public BlockPos getLastTargetPosition()
    {
        int t = currentTargetPosition - 1;
        if (t < 0)
            t = 7;
        return targetPositions[t];
    }

    @Nullable
    protected ReconUAVTileEntity getController()
    {
        TileEntity tileEntity = world.getTileEntity(controllerPos);
        return tileEntity instanceof ReconUAVTileEntity ? (ReconUAVTileEntity) tileEntity : null;
    }

    @Override
    public void readAdditional(CompoundNBT compound)
    {
        controllerPos = NBTUtil.readBlockPos(compound.getCompound("controllerPos"));
        durability = compound.getInt("durability");
        scanCD = compound.getInt("scancd");
        area = new AxisAlignedBB(new BlockPos(controllerPos.getX() - 100, 50, controllerPos.getZ() - 100), new BlockPos(controllerPos.getX() + 100, ALTITUDE, controllerPos.getZ() + 100));
        setTargetPredicate(compound.getString("target"));
        currentTargetPosition = compound.getInt("currenttargetposition");
        setTargetPositions();
        moved = compound.getInt("moved");
    }

    @Override
    public void writeAdditional(CompoundNBT compound)
    {
        compound.put("controllerPos", NBTUtil.writeBlockPos(controllerPos));
        compound.putInt("durability", durability);
        compound.putInt("scancd", scanCD);
        compound.putString("target", targetPredicate.toString());
        compound.putInt("currenttargetposition", currentTargetPosition);
        compound.putInt("moved", moved);
    }

    @Override
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public enum TargetPredicate
    {
        ALL_LIVINGS((entity) -> entity instanceof LivingEntity),
        ALL_MONSTERS((entity) -> entity instanceof IMob),
        ALL_PLAYERS((entity) -> entity instanceof PlayerEntity);

        private final Predicate<Entity> predicate;

        TargetPredicate(Predicate<Entity> predicate)
        {
            this.predicate = predicate;
        }

        public Predicate<Entity> getPredicate()
        {
            return predicate;
        }
    }

//    static class MoveToTargetGoal extends Goal
//    {
//        private final ReconUAVEntity uav;
//        private double movePosX;
//        private double movePosY;
//        private double movePosZ;
//        private final double speed;
//
//        public MoveToTargetGoal(ReconUAVEntity uav)
//        {
//            this.uav = uav;
//            this.speed = 0.2870181669F;
//            this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
//        }
//
//        public boolean shouldExecute()
//        {
//            return true;
//        }
//
//        public void startExecuting()
//        {
//            this.uav.getNavigator().tryMoveToXYZ(this.movePosX, this.movePosY, this.movePosZ, this.speed);
//            this.uav.moveController.setMoveTo();
//        }
//    }
}
