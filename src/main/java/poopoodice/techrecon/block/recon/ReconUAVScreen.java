package poopoodice.techrecon.block.recon;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import poopoodice.techrecon.TechRecon;
import poopoodice.techrecon.entity.ReconUAVEntity;
import poopoodice.techrecon.packets.CUpdateReconUAVTileEntityMessage;
import poopoodice.techrecon.packets.TechReconPackets;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ReconUAVScreen extends Screen
{
    private static final ResourceLocation RECON_UAV_GUI = new ResourceLocation(TechRecon.MODID + ":textures/gui/recon_uav_gui.png");
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 218;
    private static final String VISUAL = " This only changes the visual of the screen.";
    private static final String PERFORMANCE = " Disabling this might slightly improve client performance.";

    private final ReconUAVTileEntity reconUAVTileEntity;
    private List<Integer> mapColourData = new ArrayList<>();
    private BlockPos uavPos = BlockPos.ZERO;
    private int updatedMapTick = 0;
    private CheckUpdateDataButton scanEffectButton;
    private CheckUpdateDataButton showMyPosButton;
    private CheckUpdateDataButton showUAVPosButton;
    private CheckUpdateDataButton uavColourButton;

    public ReconUAVScreen(ReconUAVTileEntity reconUAVTileEntity)
    {
        super(new StringTextComponent("Recon UAV Control Panel"));
        this.reconUAVTileEntity = reconUAVTileEntity;
    }

    @Override
    public void tick()
    {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null)
            return;
        World world = player.world;
        updatedMapTick++;
        if (world != null && world.getGameTime() % ReconUAVTileEntity.UAV_UPDATE_INTERVAL == 0L)
            gatherData(world.getGameTime() % ReconUAVTileEntity.BLOCK_MAP_COLOUR_UPDATE_INTERVAL == 0L);
    }

    @Override
    protected void init()
    {
        buttons.clear();
        children.clear();
        gatherData(true);
        for (int i=0;i<2;i++)
        {
            ReconUAVEntity.TargetPredicate predicate = ReconUAVEntity.TargetPredicate.byID(i);
            addButton(new UpdateDataButton(209 + 20 * i, 45, predicate.getTarget(), CUpdateReconUAVTileEntityMessage.UPDATE_TARGET_PREDICATE, this::setTargetPredicate));
        }
        for (int i=2;i<4;i++)
        {
            ReconUAVEntity.TargetPredicate predicate = ReconUAVEntity.TargetPredicate.byID(i);
            addButton(new UpdateDataButton(209 + 20 * (i - 2), 65, predicate.getTarget(), CUpdateReconUAVTileEntityMessage.UPDATE_TARGET_PREDICATE, this::setTargetPredicate));
        }
        scanEffectButton = addButton(new CheckUpdateDataButton(235, 133, "Decides whether the screen shows the scanning effect." + VISUAL + PERFORMANCE, CUpdateReconUAVTileEntityMessage.UPDATE_SCAN_EFFECT, (data) -> setScanEffect(bool(data))));
        showMyPosButton = addButton(new CheckUpdateDataButton(235, 148, "Decides whether the screen shows the position of the controller." + VISUAL + PERFORMANCE, CUpdateReconUAVTileEntityMessage.UPDATE_SCAN_EFFECT, (data) -> setShowMyPos(bool(data))));
        showUAVPosButton = addButton(new CheckUpdateDataButton(235, 133, "Decides whether the screen shows the position of UAV." + VISUAL + PERFORMANCE, CUpdateReconUAVTileEntityMessage.UPDATE_SCAN_EFFECT, (data) -> setShowUAVPos(bool(data))));
        uavColourButton = addButton(new CheckUpdateDataButton(235, 133, "Decides the colour of the UAV on the screen." + VISUAL, CUpdateReconUAVTileEntityMessage.UPDATE_SCAN_EFFECT, this::setUAVColour));
    }

    private void gatherData(boolean mapUpdate)
    {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null)
            return;
        if (mapUpdate)
        {
            mapColourData = new ArrayList<>();
            mapColourData = reconUAVTileEntity.getMapColourData();
        }
        Entity uav = player.world.getEntityByID(reconUAVTileEntity.uav);
        if (uav instanceof ReconUAVEntity && uav.isAlive())
            uavPos = uav.getPosition();
        else
            uavPos = BlockPos.ZERO;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        if (minecraft == null)
            return;
        renderBackground(matrixStack);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.textureManager.bindTexture(RECON_UAV_GUI);
        blit(matrixStack, (width - TEXTURE_WIDTH) / 2, (height- TEXTURE_HEIGHT) / 2, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        blit(matrixStack, (width - TEXTURE_WIDTH) / 2 + 219, (height - TEXTURE_HEIGHT) / 2 + 18, 80 + reconUAVTileEntity.getScanTarget().getID() * 20, 219, 20, 20);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        renderBlockColours(matrixStack);
        renderUAV(matrixStack);
        int gMouseX = mouseX - (width - TEXTURE_WIDTH) / 2;
        int gMouseY = mouseY - (height - TEXTURE_HEIGHT) / 2;
        if (gMouseX >= 3 && gMouseX <= 202 && gMouseY >= 2 && gMouseY <= 201)
            renderCoord(matrixStack, gMouseX - 3, gMouseY - 2);
        renderTargetEntities(matrixStack, reconUAVTileEntity.getEntityScanned());
        if (updatedMapTick > 60)
            renderScanEffect(matrixStack);
    }

    private void renderBlockColours(MatrixStack stack)
    {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null)
            return;
//        for (int i = 0; i < mapColourData.size(); i++)
//        {
//            int minX = toGraphX(i % (ReconUAVEntity.SCAN_RADIUS * 2));
//            int minY = toGraphY(i / (ReconUAVEntity.SCAN_RADIUS * 2) + 1);
//            Color colour = new Color(MaterialColor.COLORS[mapColourData.get(mapColourData.get(i))].colorValue);
//            fill(stack, minX, minY, minX + 1, minY + 1, ColorHelper.PackedColor.packColor(255, colour.getRed(), colour.getRed(), colour.getBlue()));
//        }
        int c = 0;
        if (mapColourData.isEmpty()) return;
        for (int x = reconUAVTileEntity.getPos().getX() - ReconUAVEntity.SCAN_RADIUS; x < reconUAVTileEntity.getPos().getX() + ReconUAVEntity.SCAN_RADIUS && c < mapColourData.size(); x++)
        {
            for (int z = reconUAVTileEntity.getPos().getZ() + ReconUAVEntity.SCAN_RADIUS; z > reconUAVTileEntity.getPos().getZ() - ReconUAVEntity.SCAN_RADIUS && c < mapColourData.size(); z--)
            {
                int minX = toGraphX(x);
                int minY = toGraphY(z);
                int index = mapColourData.get(c);
                Color colour = new Color(MaterialColor.COLORS[index].colorValue);

//                Color colour;
//                index = index & 255;
//                if (index / 4 == 0)
//                    colour = new Color(0);
//                else
//                    colour = new Color(MaterialColor.COLORS[index / 4].getMapColor(index & 3));

//                Color colour = new Color(index);
                fill(stack, minX, minY, minX + 1, minY + 1, ColorHelper.PackedColor.packColor(255, colour.getRed(), colour.getGreen(), colour.getBlue()));
                c++;
            }
        }
    }

    private void renderUAV(MatrixStack stack)
    {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null || uavPos.equals(BlockPos.ZERO))
            return;
        Minecraft.getInstance().getTextureManager().bindTexture(RECON_UAV_GUI);
        blit(stack, toGraphX(uavPos.getX()) - 8, toGraphY(uavPos.getZ()) - 7, 160, 219, 16, 14);
    }

    private void renderCoord(MatrixStack stack, int x, int y)
    {
        drawCenteredString(stack, Minecraft.getInstance().fontRenderer, String.valueOf(toWorldX(x)), (width - TEXTURE_WIDTH) / 2 + 233, (height - TEXTURE_HEIGHT) / 2 + 98, 8001);
        drawCenteredString(stack, Minecraft.getInstance().fontRenderer, String.valueOf(toWorldZ(y)), (width - TEXTURE_WIDTH) / 2 + 233, (height - TEXTURE_HEIGHT) / 2 + 115, 8001);
    }

    private void renderTargetEntities(MatrixStack stack, List<LivingEntity> livings)
    {
        Minecraft.getInstance().getTextureManager().bindTexture(RECON_UAV_GUI);
        livings.forEach((entity) -> {
            int textureX = 246;
            if (entity instanceof PlayerEntity)
                textureX += 3;
            else if (!(entity instanceof IMob))
                textureX += 6;
            blit(stack, toGraphX(entity.getPosition().getX()) - 1, toGraphY(entity.getPosition().getZ() - 1), textureX, 219, 3, 3);
        });
    }

    private void renderScanEffect(MatrixStack stack)
    {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int tick = updatedMapTick - 60;
        int minX = (width - TEXTURE_WIDTH) / 2 + (tick - 1) * 20 + 3;
        int maxX = minX + 20;
        if (tick < 10)
            fill(stack, minX, (height - TEXTURE_HEIGHT) / 2 + 2, maxX, (height - TEXTURE_HEIGHT) / 2 + 200 + 3, 0x7300ff01);
        minX = (width - TEXTURE_WIDTH) / 2 + (tick - 2) * 20 + 3;
        maxX = minX + 20;
        if (tick > 1)
            fill(stack, minX, (height - TEXTURE_HEIGHT) / 2 + 2, maxX, (height - TEXTURE_HEIGHT) / 2 + 200 + 3, 0x4000ff01);
        if (updatedMapTick >= 70)
            updatedMapTick = 0;
    }

    private int toWorldX(int x)
    {
        return x - ReconUAVEntity.SCAN_RADIUS - 3 + reconUAVTileEntity.getPos().getX();
    }

    private int toWorldZ(int y)
    {
        return y - ReconUAVEntity.SCAN_RADIUS - 2 + reconUAVTileEntity.getPos().getZ();
    }

    private int toGraphX(int x)
    {
        return ReconUAVEntity.SCAN_RADIUS + (x - reconUAVTileEntity.getPos().getX()) + (width - TEXTURE_WIDTH) / 2 + 3;
    }

    private int toGraphY(int z)
    {
        return ReconUAVEntity.SCAN_RADIUS + (z - reconUAVTileEntity.getPos().getZ()) + (height - TEXTURE_HEIGHT) / 2 + 2;
    }

    private void updateData(int type, int data)
    {
        TechReconPackets.INSTANCE.sendToServer(new CUpdateReconUAVTileEntityMessage(reconUAVTileEntity.getPos(), type, data));
    }

    private void setTargetPredicate(int predicate)
    {
        reconUAVTileEntity.setScanTarget(predicate);
        updateData(CUpdateReconUAVTileEntityMessage.UPDATE_TARGET_PREDICATE, predicate);
    }

    private void setScanEffect(boolean scanEffect)
    {
        reconUAVTileEntity.scanEffect = scanEffect;
        updateData(CUpdateReconUAVTileEntityMessage.UPDATE_TARGET_PREDICATE, lnt(scanEffect));
    }

    private void setShowMyPos(boolean showMyPos)
    {
        reconUAVTileEntity.showMyPos = showMyPos;
        updateData(CUpdateReconUAVTileEntityMessage.UPDATE_TARGET_PREDICATE, lnt(showMyPos));
    }

    private void setShowUAVPos(boolean showUAVPos)
    {
        reconUAVTileEntity.showUAVPos = showUAVPos;
        updateData(CUpdateReconUAVTileEntityMessage.UPDATE_TARGET_PREDICATE, lnt(showUAVPos));
    }

    private void setUAVColour(int uavColour)
    {
        reconUAVTileEntity.uavColour = uavColour;
        updateData(CUpdateReconUAVTileEntityMessage.UPDATE_TARGET_PREDICATE, uavColour);
    }

    private int lnt(boolean bool)
    {
        return bool ? 1 : 0;
    }

    private boolean bool(int data)
    {
        return data == 1;
    }

    private List<LivingEntity> getLivingEntities()
    {
        return getEntities((entity) -> entity instanceof PlayerEntity || entity instanceof IMob);
    }

    private List<LivingEntity> getPlayers()
    {
        return getEntities((entity) -> !(entity instanceof PlayerEntity));
    }

    private List<LivingEntity> getMonsters()
    {
        return getEntities((entity) -> !(entity instanceof IMob));
    }

    private List<LivingEntity> getEntities(Predicate<Entity> predicate)
    {
        List<LivingEntity> entities = getAllEntities();
        entities.removeIf(predicate);
        return entities;
    }

    private List<LivingEntity> getAllEntities()
    {
        return new ArrayList<>(reconUAVTileEntity.getEntityScanned());
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    class UpdateDataButton extends AbstractButton
    {
        private final int data;
        private final Consumer<Integer> consumer;

        public UpdateDataButton(int x, int y, String inf, int data, Consumer<Integer> consumer)
        {
            super((ReconUAVScreen.this.width - TEXTURE_WIDTH) / 2 + x, (ReconUAVScreen.this.height - TEXTURE_HEIGHT) / 2 + y, 20, 20, new StringTextComponent(inf));
            this.data = data;
            this.consumer = consumer;
        }

        @Override
        public void onPress()
        {
            consumer.accept(data);
        }

        @Override
        public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
        {
            if (isHovered())
            {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                fill(matrixStack, x + 1, y + 1, x + 19, y + 19, 0x7300ff01);
                renderToolTip(matrixStack, mouseX, mouseY);
            }
        }

        @Override
        public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY)
        {
            ReconUAVScreen.this.renderTooltip(matrixStack, getMessage(), mouseX, mouseY);
        }
    }

    class CheckUpdateDataButton extends UpdateDataButton
    {
        public boolean checked;

        public CheckUpdateDataButton(int x, int y, String inf, int data, Consumer<Integer> consumer)
        {
            super(x, y, inf, data, consumer);
        }

        @Override
        public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
        {
            if (checked)
            {
                Minecraft.getInstance().getTextureManager().bindTexture(RECON_UAV_GUI);
                blit(matrixStack, x, y, 0, 239, 12, 12);
            }
        }
    }
}
