package net.fusionlord.mods.oreexcavation.shapeselector.screens;

import com.google.common.collect.ImmutableSet;
import net.fusionlord.mods.oreexcavation.shapeselector.ShapeSelector;
import net.fusionlord.mods.oreexcavation.shapeselector.screens.components.ActionButton;
import net.fusionlord.mods.oreexcavation.shapeselector.screens.components.GuiSliderInt;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.MathHelper;
import oreexcavation.client.GuiEditShapes;
import oreexcavation.shapes.ExcavateShape;
import oreexcavation.shapes.ShapeRegistry;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.Collection;

public class ShapeSelectionScreen extends GuiScreen {
    private GuiSliderInt slider;
    private Collection<GuiButton> sliderParts;
    private ShapeRegistry registry;
    private int cooldown;

    @Override
    public void initGui() {
        super.initGui();

        registry = ShapeRegistry.INSTANCE;

        addButton(new ActionButton(buttonList.size(), width / 2 - 105, height - 30, 100, 20, I18n.format(ShapeSelector.MODID + ".editshapes"), send -> {
            if (send) mc.displayGuiScreen(new GuiEditShapes());
            return false;
        }));

        addButton(new ActionButton(buttonList.size(), width / 2 + 5, height - 30, 100, 20, I18n.format(ShapeSelector.MODID + ".noshape"), send -> {
            if (send) {
                ShapeSelector.setShape(0);
                sliderParts.forEach(c -> c.visible = false);
            }
            return false;
        }));

        slider = new GuiSliderInt(
                    width / 2 - 50,
                    height / 2 + 86,
                    100,
                    20,
                    I18n.format(ShapeSelector.MODID + ".range") + ": ",
                    " " + I18n.format(ShapeSelector.MODID + ".unit"),
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

    @Override
    public void drawScreen(int mx, int my, float partialTicks) {
        this.drawDefaultBackground();
        ExcavateShape shape = registry.getActiveShape();

        drawCenteredString(mc.fontRenderer, I18n.format(ShapeSelector.MODID + ".title"), width / 2,  10, Color.WHITE.getRGB());

        if (shape != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(width / 2f - 80, height / 2f - 80, 0f);
            if (!slider.visible) sliderParts.forEach(c -> c.visible = true);

            drawCenteredString(mc.fontRenderer, shape.getName(), 80, -11, Color.WHITE.getRGB());

            drawRect(-1, -1, 161, 161, Color.WHITE.getRGB());
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
                        this.mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                        GlStateManager.pushMatrix();
                        GlStateManager.scale(-2.0F, -2.0F, 0F);
                        GlStateManager.translate((float) (x * 16), (float) (y * 16), 0.0F);
                        this.drawTexturedModalRect(0, 0, this.mc.getTextureMapBlocks().getAtlasSprite("minecraft:blocks/stone"), 16, 16);
                        GlStateManager.popMatrix();
                    }

                    if(off == y * 5 + x)
                    {
                        this.mc.renderEngine.bindTexture(ICONS);
                        GlStateManager.pushMatrix();
                        GlStateManager.scale(-2.0F, -2.0F, 1.0F);
                        GlStateManager.translate((float) (x * 16), (float) (y * 16), 0.0F);
                        this.drawTexturedModalRect(0, 0, 0, 0, 16, 16);
                        GlStateManager.popMatrix();
                    }
                }
            }
            GlStateManager.popMatrix();
        } else {
            drawCenteredString(mc.fontRenderer, I18n.format(ShapeSelector.MODID + ".noshape"), width / 2, height / 2, Color.WHITE.getRGB());
        }

        drawCenteredString(mc.fontRenderer, I18n.format(ShapeSelector.MODID + ".tip"), width / 2, height - 40, Color.YELLOW.getRGB());

        super.drawScreen(mx, my, partialTicks);
    }

    @Override
    public void updateScreen() {
        if (!GameSettings.isKeyDown(ShapeSelector.shapeSelector)) {
            if (registry.getActiveShape() != null)
                registry.getActiveShape().setMaxDepth(ShapeSelector.depth);
            mc.displayGuiScreen(null);
        }

        ImmutableSet<KeyBinding> set = ImmutableSet.of(mc.gameSettings.keyBindForward, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindSneak, mc.gameSettings.keyBindSprint, mc.gameSettings.keyBindJump);
        for (KeyBinding k : set)
            KeyBinding.setKeyBindState(k.getKeyCode(), GameSettings.isKeyDown(k));

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
                    cooldown = 3;
                }
        }
        cooldown--;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
