package net.fusionlord.mods.oreexcavation.shapeselector.screens.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import java.util.function.Predicate;

public class ActionButton extends GuiButton
{
    private Predicate<Boolean> action;

    public ActionButton(int id, int x, int y, int width, int height, String text, Predicate<Boolean> action) {
        super(id, x, y, width, height, text);
        this.action = action;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        super.mousePressed(mc, mouseX, mouseY);

        if( !(mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height) )
            return false;

        this.action.test(true);
        return true;
    }
}
