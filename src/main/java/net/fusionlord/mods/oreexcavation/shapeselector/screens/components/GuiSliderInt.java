package net.fusionlord.mods.oreexcavation.shapeselector.screens.components;

import com.google.common.collect.ImmutableSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraftforge.fml.client.gui.widget.Slider;

import java.awt.Color;
import java.util.Collection;
import java.util.function.BiConsumer;

public class GuiSliderInt extends Slider {
    private int colorBackground, colorSliderBackground, colorSlider;
    private BiConsumer<GuiSliderInt, Integer> increment;
    private int value;
    private Minecraft minecraft;

    public GuiSliderInt(int xPos, int yPos, int width, int height, String prefix, String suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr, Color color, IPressable par, BiConsumer<GuiSliderInt, Integer> increment, Minecraft minecraft) {
        super(xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, par);
        colorBackground = getColor(color, 200).getRGB();
        colorSliderBackground = getColor(color.darker(), 200).getRGB();
        colorSlider = getColor(color.brighter().brighter(), 200).getRGB();
        this.increment = increment;
        this.minecraft = minecraft;
    }

    private Color getColor(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        setValue(getValueInt());
    }

    @Override
    public void updateSlider() {
        super.updateSlider();
        int valueInt = getValueInt();
        if (value != valueInt) {
            value = valueInt;
        }
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partial) {
        if (!visible)
            return;

        isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        fill(x, y, x + width, y + height, colorBackground);
        drawBorderedRect(x + (int) (sliderValue * (width - 8)), y, 8, height);
        renderText(minecraft, this);
    }

    private void renderText(Minecraft mc, Widget component) {
        int color = !active ? 10526880 : (isHovered ? 16777120 : -1);
        String buttonText = component.getMessage();
        int strWidth = mc.fontRenderer.getStringWidth(buttonText);
        int ellipsisWidth = mc.fontRenderer.getStringWidth("...");
        if (strWidth > component.getWidth() - 6 && strWidth > ellipsisWidth)
            buttonText = mc.fontRenderer.trimStringToWidth(buttonText, component.getWidth() - 6 - ellipsisWidth).trim() + "...";

        drawCenteredString(mc.fontRenderer, buttonText, component.x + component.getWidth() / 2, component.y + (component.getHeight() - 8) / 2, color);
    }

    @Override
    public void playDownSound(SoundHandler p_playDownSound_1_) {}

    @Override
    public boolean mouseDragged(double mouseX, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
        if (!visible)
            return false;

        if (dragging) {
            sliderValue = (mouseX - (x + 4)) / (float) (width - 8);
            updateSlider();
        }
        return true;
    }

    private void drawBorderedRect(int x, int y, int width, int height) {
        fill(x, y, x + width, y + height, colorSliderBackground);
        fill(++x, ++y, x + width - 2, y + height - 2, colorSlider);
    }

    public Collection<Widget> getComponents() {
        return ImmutableSet.of(this,
                new Button(x - height, y, height, height, "-", btn -> increment.accept(this, -1)),
                new Button(x + width, y, height, height, "+", btn -> increment.accept(this, 1)));
    }
}
