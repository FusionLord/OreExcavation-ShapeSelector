package net.fusionlord.mods.oreexcavation.shapeselector.screens;

import com.google.common.collect.ImmutableSet;
import net.fusionlord.mods.oreexcavation.shapeselector.ShapeSelector;
import net.fusionlord.mods.oreexcavation.shapeselector.screens.components.ActionButton;
import net.fusionlord.mods.oreexcavation.shapeselector.screens.components.GuiSliderInt;
import net.fusionlord.mods.oreexcavation.shapeselector.screens.components.GuiSlotBlockList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.MathHelper;
import oreexcavation.client.ExcavationKeys;
import oreexcavation.client.GuiEditShapes;
import oreexcavation.shapes.ExcavateShape;
import oreexcavation.shapes.ShapeRegistry;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.Collection;

public class ShapeSelectionScreen extends GuiScreen {
    private GuiSliderInt slider;
    private Collection<GuiButton> sliderParts;
    private ShapeRegistry registry;
    private int cooldown;
    private GuiSlotBlockList blacklist;

    @Override
    public void initGui() {
        super.initGui();

        registry = ShapeRegistry.INSTANCE;

        addButton(new ActionButton(buttonList.size(), width / 2 - 105, height - 30, 100, 20, I18n.format("oeshapeselector.editshapes"), send -> {
            if (send) mc.displayGuiScreen(new GuiEditShapes());
            return send;
        }));

        addButton(new ActionButton(buttonList.size(), width / 2 + 5, height - 30, 100, 20, I18n.format("oeshapeselector.noshape"), send -> {
            if (send) {
                ShapeSelector.setShape(0);
                sliderParts.forEach(c -> c.visible = false);
            }
            return send;
        }));

        blacklist = new GuiSlotBlockList(this, ShapeSelector.blacklist, 100, 20);
        
        addButton(new ActionButton(buttonList.size(), (blacklist.getLeft() + blacklist.getRight()) / 2 - 50, blacklist.getBottom() + 5, 49, 20, I18n.format("oeshapeselector.shapeblacklist.add"), send -> {
            if (send) {
                IBlockState state = mc.world.getBlockState(mc.objectMouseOver.getBlockPos());
                if (!ShapeSelector.blacklist.contains(state))
                    ShapeSelector.blacklist.add(state);
            }
            return send;
        }));

        addButton(new ActionButton(buttonList.size(), (blacklist.getLeft() + blacklist.getRight()) / 2 + 1, blacklist.getBottom() + 5, 49, 20, I18n.format("oeshapeselector.shapeblacklist.remove"), send -> {
            if (send) blacklist.remove();
            return send;
        }));

        slider = new GuiSliderInt(
                    width / 2 - 50,
                    height / 2 + 86,
                    100,
                    20,
                    I18n.format("oeshapeselector.range") + ": ",
                    " " + I18n.format("oeshapeselector.unit"),
                    -1,
                    32,
                    ShapeSelector.depth,
                    false,
                    true,
                    Color.DARK_GRAY,
                    slider -> ShapeSelector.depth = slider.getValueInt(),
                    (slider, amount) -> {
                        int value = slider.getValueInt();
                        int valueNew = MathHelper.clamp(value + amount, 1, 32);
                        slider.setValue(valueNew);
                        slider.updateSlider();
                    }
                );
        slider.precision = 1;

        (sliderParts = slider.getComponents()).forEach(this::addButton);

        if (registry.getActiveShape() == null)
            sliderParts.forEach(c -> c.visible = false);
    }

    private void addToShapeBlacklist(IBlockState state) {
        blacklist.add(state);
    }

    @Override
    public void drawScreen(int mx, int my, float partialTicks) {
        this.drawDefaultBackground();
        GlStateManager.pushMatrix();
        ExcavateShape shape = registry.getActiveShape();

        drawCenteredString(mc.fontRenderer, I18n.format("oeshapeselector.title"), width / 2,  10, Color.WHITE.getRGB());

        if (shape != null) {
            drawCenteredString(mc.fontRenderer, shape.getName(), width / 2, height / 2 - 91, Color.WHITE.getRGB());
            GlStateManager.pushMatrix();
            GlStateManager.translate(width / 2f, height / 2f, 0f);
            drawShape(shape, mc);
            GlStateManager.popMatrix();
        } else {
            drawCenteredString(mc.fontRenderer, I18n.format("oeshapeselector.noshape"), width / 2, height / 2, Color.WHITE.getRGB());
        }

        drawCenteredString(mc.fontRenderer, I18n.format("oeshapeselector.tip"), width / 2, height - 40, Color.YELLOW.getRGB());
        GlStateManager.popMatrix();

        drawCenteredString(fontRenderer, I18n.format("oeshapeselector.shapeblacklist"), (blacklist.getLeft() + blacklist.getRight())/2,  blacklist.getTop() - 15, 0xFFFFFF);
        blacklist.drawScreen(mx, my, partialTicks);

        super.drawScreen(mx, my, partialTicks);
    }

    public static void drawShape(ExcavateShape shape, Minecraft mc) {
        if (shape == null) return;
        GlStateManager.translate(-80, -80, 10);
        drawRect(-2, -2, 162, 162, Color.WHITE.getRGB());
        drawRect(0, 0, 160, 160, Color.BLACK.getRGB());
        GlStateManager.color(1, 1, 1);

        int mask = shape.getShapeMask();
        int off = shape.getReticle();

        GlStateManager.translate(160, 160, 0);
        for(int x = 0; x < 5; ++x)
        {
            for(int y = 0; y < 5; ++y)
            {
                int flag = ExcavateShape.posToMask(x, y);
                if((mask & flag) != flag)
                {
                    mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(-2F, -2F, 0F);
                    GlStateManager.translate(x * 16, y * 16, 0.0F);
                    drawTexturedRect(0, 0, mc.getTextureMapBlocks().getAtlasSprite("minecraft:blocks/stone"), 16, 16);

                    GlStateManager.popMatrix();
                }

                if (off == y * 5 + x) {
                    mc.renderEngine.bindTexture(ICONS);
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(-2.0F, -2.0F, 1.0F);
                    GlStateManager.translate((float) (x * 16), (float) (y * 16), 0.0F);
                    drawTexturedRect(0, 0, 0, 0, 16, 16);
                    GlStateManager.popMatrix();
                }
            }
        }
    }

    public static void drawRect(int left, int top, int right, int bottom, int color)
    {
        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        if (Minecraft.getMinecraft().currentScreen != null) {
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
        GlStateManager.color(f, f1, f2, f3);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(left, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, top, 0.0D).endVertex();
        bufferbuilder.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        if (Minecraft.getMinecraft().currentScreen != null) {
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        }
    }

    private static void drawTexturedRect(int xCoord, int yCoord, TextureAtlasSprite textureSprite, int widthIn, int heightIn)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(xCoord, yCoord + heightIn, 0).tex(textureSprite.getMinU(), textureSprite.getMaxV()).endVertex();
        bufferbuilder.pos(xCoord + widthIn, yCoord + heightIn, 0).tex(textureSprite.getMaxU(), textureSprite.getMaxV()).endVertex();
        bufferbuilder.pos(xCoord + widthIn, yCoord, 0).tex(textureSprite.getMaxU(), textureSprite.getMinV()).endVertex();
        bufferbuilder.pos(xCoord, yCoord, 0).tex(textureSprite.getMinU(), textureSprite.getMinV()).endVertex();
        tessellator.draw();
    }

    private static void drawTexturedRect(float xCoord, float yCoord, int minU, int minV, int maxU, int maxV)
    {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(xCoord, yCoord + (float)maxV, 0).tex((float)minU * 0.00390625F, (float)(minV + maxV) * 0.00390625F).endVertex();
        bufferbuilder.pos(xCoord + (float)maxU, yCoord + (float)maxV, 0).tex((float)(minU + maxU) * 0.00390625F, (float)(minV + maxV) * 0.00390625F).endVertex();
        bufferbuilder.pos(xCoord + (float)maxU, yCoord, 0).tex((float)(minU + maxU) * 0.00390625F, (float)minV * 0.00390625F).endVertex();
        bufferbuilder.pos(xCoord, yCoord, 0).tex((float)minU * 0.00390625F, (float)minV * 0.00390625F).endVertex();
        tessellator.draw();
    }

    @Override
    public void updateScreen() {
        if (!Keyboard.isKeyDown(ExcavationKeys.shapeKey.getKeyCode())) {
            if (registry.getActiveShape() != null)
                registry.getActiveShape().setMaxDepth(ShapeSelector.depth);
//            mc.displayGuiScreen(null);
        }

        ImmutableSet<KeyBinding> set = ImmutableSet.of(mc.gameSettings.keyBindForward, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindSneak, mc.gameSettings.keyBindSprint, mc.gameSettings.keyBindJump);
        for (KeyBinding k : set)
            KeyBinding.setKeyBindState(k.getKeyCode(), GameSettings.isKeyDown(k));
        cooldown--;
    }

    @Override
    public void handleMouseInput() throws IOException {
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        if (mouseX > blacklist.getLeft() && mouseX < blacklist.getRight() && mouseY > blacklist.getTop() && mouseY < blacklist.getBottom()) {
            blacklist.handleMouseInput(mouseX, mouseY);
            return;
        }

        if (cooldown <= 0) {
            int dir = MathHelper.clamp(Mouse.getDWheel(), -1, 1);
            if (dir != 0)
            {
                int curIdx = registry.getShapeList().indexOf(registry.getActiveShape());

                curIdx += dir;

                if(curIdx < 0)
                    curIdx = registry.getShapeList().size() - 1;
                if(curIdx >= registry.getShapeList().size())
                    curIdx = 0;

                ShapeSelector.setShape(curIdx + 1);
                System.out.println(curIdx);
                if (!slider.visible) sliderParts.forEach(c -> c.visible = true);
                cooldown = 3;
            }
        }
        super.handleMouseInput();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
