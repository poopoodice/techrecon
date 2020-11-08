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
import poopoodice.techrecon.packets.TechReconPackets;
import poopoodice.techrecon.packets.UpdateReconUAVTileEntityPredicateMessage;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ReconUAVScreen extends Screen
{
    private static final ResourceLocation RECON_UAV_GUI = new ResourceLocation(TechRecon.MODID + ":textures/gui/recon_uav_gui.png");
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 218;

    private final ReconUAVTileEntity reconUAVTileEntity;
    private List<Integer> mapColourData = new ArrayList<>();
    private BlockPos uavPos;
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
        if (world != null && world.getGameTime() % ReconUAVTileEntity.UAV_UPDATE_INTERVAL == 0L)
            gatherData(world.getGameTime() % ReconUAVTileEntity.BLOCK_MAP_COLOUR_UPDATE_INTERVAL == 0L);
    }

    @Override
    protected void init()
    {
        super.init();
        buttons.clear();
        children.clear();
        gatherData(true);
        for (int i=0;i<2;i++)
        {
            ReconUAVEntity.TargetPredicate predicate = ReconUAVEntity.TargetPredicate.byID(i);
            addButton(new ChooseTargetPredicateButton((width - TEXTURE_WIDTH) / 2 + 209 + 20 * i, (height- TEXTURE_HEIGHT) / 2 + 45, predicate.getTarget(), predicate));
        }
        for (int i=2;i<4;i++)
        {
            ReconUAVEntity.TargetPredicate predicate = ReconUAVEntity.TargetPredicate.byID(i);
            addButton(new ChooseTargetPredicateButton((width - TEXTURE_WIDTH) / 2 + 209 + 20 * (i - 2), (height- TEXTURE_HEIGHT) / 2 + 65, predicate.getTarget(), predicate));
        }
    }

    private void gatherData(boolean mapUpdate)
    {
        PlayerEntity player = Minecraft.getInstance().player;
        if (mapUpdate)
            mapColourData = reconUAVTileEntity.getMapColourData();
        if (player != null)
        {
            Entity uav = player.world.getEntityByID(reconUAVTileEntity.uav);
            if (uav instanceof ReconUAVEntity && uav.isAlive())
                uavPos = uav.getPosition();
            else
                uavPos = BlockPos.ZERO;
        }
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
//        int gMouseX = mouseX - (width - TEXTURE_WIDTH) / 2;
//        int gMouseY = mouseY - (height - TEXTURE_HEIGHT) / 2;
//        if (gMouseX >= 3 && gMouseX <= 202 && gMouseY >= 2 && gMouseY <= 201)
//            renderCoord(matrixStack, gMouseX, gMouseY);
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
        for (int x = reconUAVTileEntity.getPos().getX() - ReconUAVEntity.SCAN_RADIUS; x < reconUAVTileEntity.getPos().getX() + ReconUAVEntity.SCAN_RADIUS; x++)
            for (int z = reconUAVTileEntity.getPos().getZ() - ReconUAVEntity.SCAN_RADIUS; z < reconUAVTileEntity.getPos().getZ() + ReconUAVEntity.SCAN_RADIUS; z++)
            {
                int minX = toGraphX(x);
                int minY = toGraphY(z);
                Color colour = new Color(MaterialColor.COLORS[mapColourData.get(c)].colorValue);
                fill(stack, minX, minY, minX + 1, minY + 1, ColorHelper.PackedColor.packColor(255, colour.getRed(), colour.getRed(), colour.getBlue()));
                c++;
            }
    }

    private void renderUAV(MatrixStack stack)
    {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null || uavPos.equals(BlockPos.ZERO))
            return;
        Minecraft.getInstance().getTextureManager().bindTexture(RECON_UAV_GUI);
        blit(stack, toGraphX(uavPos.getX()), toGraphY(uavPos.getZ()), 160, 219, 16, 14);
    }

    private void renderCoord(MatrixStack stack, int x, int y)
    {
        drawCenteredString(stack, Minecraft.getInstance().fontRenderer, String.valueOf(toWorldX(x)), (width - TEXTURE_WIDTH) / 2 + 233, (height - TEXTURE_HEIGHT) / 2 + 98, 8001);
        drawCenteredString(stack, Minecraft.getInstance().fontRenderer, String.valueOf(toWorldZ(y)), (width - TEXTURE_WIDTH) / 2 + 233, (height - TEXTURE_HEIGHT) / 2 + 115, 8001);
    }

    private int toWorldX(int x)
    {
        return x - ReconUAVEntity.SCAN_RADIUS - 3 - (width - TEXTURE_WIDTH) / 2 + reconUAVTileEntity.getPos().getX();
    }

    private int toWorldZ(int y)
    {
        return y - ReconUAVEntity.SCAN_RADIUS - 2 - (height - TEXTURE_HEIGHT) / 2 + reconUAVTileEntity.getPos().getZ();
    }

    private int toGraphX(int x)
    {
        return ReconUAVEntity.SCAN_RADIUS + (x - reconUAVTileEntity.getPos().getX()) + (width - TEXTURE_WIDTH) / 2 + 3;
    }

    private int toGraphY(int z)
    {
        return ReconUAVEntity.SCAN_RADIUS - (z - reconUAVTileEntity.getPos().getZ()) + (height - TEXTURE_HEIGHT) / 2 + 2;
    }

    private void setPredicate(ReconUAVEntity.TargetPredicate predicate)
    {
        reconUAVTileEntity.setScanTarget(predicate);
        TechReconPackets.INSTANCE.sendToServer(new UpdateReconUAVTileEntityPredicateMessage(reconUAVTileEntity));
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

    class ChooseTargetPredicateButton extends AbstractButton
    {
        private final ReconUAVEntity.TargetPredicate predicate;
        public ChooseTargetPredicateButton(int x, int y, String inf, ReconUAVEntity.TargetPredicate predicate)
        {
            super(x, y, 20, 20, new StringTextComponent(inf));
            this.predicate = predicate;
        }

        @Override
        public void onPress()
        {
            ReconUAVScreen.this.setPredicate(predicate);
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
}
