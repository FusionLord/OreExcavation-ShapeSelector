package net.fusionlord.mods.oreexcavation.shapeselector.screens;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import net.fusionlord.mods.oreexcavation.shapeselector.ShapeSelector;
import net.fusionlord.mods.oreexcavation.shapeselector.screens.components.GuiSliderInt;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import oreexcavation.client.ExcavationKeys;
import oreexcavation.client.GuiEditShapes;
import oreexcavation.shapes.ExcavateShape;
import oreexcavation.shapes.ShapeRegistry;

import java.awt.*;
import java.util.Collection;

public class ShapeSelectionScreen extends Screen {
    private GuiSliderInt slider;
    private Collection<Widget> sliderParts;
    private ShapeRegistry registry;
    private int cooldown;

    public ShapeSelectionScreen() {
        super(new StringTextComponent("Shape Selection!"));
    }

    @Override
    public void init() {
        super.init();

        registry = ShapeRegistry.INSTANCE;

        addButton(new Button(width / 2 - 105, height - 30, 100, 20, I18n.format(ShapeSelector.MODID + ".editshapes"), button -> {
            minecraft.displayGuiScreen(new GuiEditShapes());
        }));

        addButton(new Button(width / 2 + 5, height - 30, 100, 20, I18n.format(ShapeSelector.MODID + ".noshape"), send -> {
                ShapeSelector.setShape(0);
                sliderParts.forEach(c -> c.visible = false);
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
                    slider -> ShapeSelector.depth = ((GuiSliderInt)slider).getValueInt(),
                    (slider, amount) -> {
                        int value = slider.getValueInt();
                        int valueNew = MathHelper.clamp(value + amount, 1, 32);
                        slider.setValue(valueNew);
                        slider.updateSlider();
                    },
                    minecraft
                );
        slider.precision = 1;
        (sliderParts = slider.getComponents()).forEach(this::addButton);
        if (registry.getActiveShape() == null)
            sliderParts.forEach(c -> c.visible = false);
    }

    @Override
    public void render(int mx, int my, float partialTicks) {
        this.renderBackground();
        ExcavateShape shape = registry.getActiveShape();

        drawCenteredString(font, I18n.format(ShapeSelector.MODID + ".title"), width / 2,  10, Color.WHITE.getRGB());

        if (shape != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef(width / 2f - 80, height / 2f - 80, 0f);
            if (!slider.visible) sliderParts.forEach(c -> c.visible = true);

            drawCenteredString(font, shape.getName(), 80, -11, Color.WHITE.getRGB());

            fill(-1, -1, 161, 161, Color.WHITE.getRGB());
            fill(0, 0, 160, 160, Color.BLACK.getRGB());
            GlStateManager.color3f(1, 1, 1);

            int mask = shape.getShapeMask();
            int off = shape.getReticle();

            GlStateManager.translatef(160, 160, 0);
            for(int x = 0; x < 5; ++x)
            {
                for(int y = 0; y < 5; ++y)
                {
                    int flag = ExcavateShape.posToMask(x, y);
                    if((mask & flag) != flag)
                    {
                        minecraft.getRenderManager().textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
                        GlStateManager.pushMatrix();
                        GlStateManager.scalef(-2.0F, -2.0F, 0F);
                        GlStateManager.translatef((float) (x * 16), (float) (y * 16), 0.0F);
                        blit(0, 0, 100, 16, 16, minecraft.getTextureMap().getAtlasSprite("minecraft:block/stone"));
                        GlStateManager.popMatrix();
                    }

                    if(off == y * 5 + x)
                    {
                        minecraft.getTextureManager().bindTexture(GUI_ICONS_LOCATION);
                        GlStateManager.pushMatrix();
                        GlStateManager.scalef(-2.0F, -2.0F, 1.0F);
                        GlStateManager.translatef((float) (x * 16), (float) (y * 16), 0.0F);
                        blit(0, 0, 0, 0, 16, 16);
                        GlStateManager.popMatrix();
                    }
                }
            }
            GlStateManager.popMatrix();
        } else {
            drawCenteredString(font, I18n.format(ShapeSelector.MODID + ".noshape"), width / 2, height / 2, Color.WHITE.getRGB());
        }

        drawCenteredString(font, I18n.format(ShapeSelector.MODID + ".tip"), width / 2, height - 40, Color.YELLOW.getRGB());

        super.render(mx, my, partialTicks);
    }

    @Override
    public boolean keyReleased(int key, int scancode, int modifiers) {
        if (ExcavationKeys.shapeKey.matchesKey(key, scancode)) {
            if (registry.getActiveShape() != null)
                registry.getActiveShape().setMaxDepth(ShapeSelector.depth);
//            minecraft.displayGuiScreen(null);
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        ImmutableSet<KeyBinding> set = ImmutableSet.of(minecraft.gameSettings.keyBindForward, minecraft.gameSettings.keyBindLeft, minecraft.gameSettings.keyBindBack, minecraft.gameSettings.keyBindRight, minecraft.gameSettings.keyBindSneak, minecraft.gameSettings.keyBindSprint, minecraft.gameSettings.keyBindJump);
        for (KeyBinding k : set)
            KeyBinding.setKeyBindState(k.getKey(), k.isPressed());
        cooldown--;
    }

    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {

        if (cooldown <= 0) {
            double dir = MathHelper.clamp(p_mouseScrolled_5_, -1, 1);
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
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
