package net.fusionlord.mods.oreexcavation.shapeselector.screens.components;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.GuiScrollingList;
import java.util.List;

public class GuiSlotBlockList extends GuiScrollingList {
    private GuiScreen parent;
    private List<IBlockState> entries;
    private int selected;

    public GuiSlotBlockList(GuiScreen parent, List<IBlockState> entries, int listWidth, int slotHeight)
    {
        super(parent.mc, listWidth, parent.height, 32, parent.height - 88 + 4, 10, slotHeight, parent.width, parent.height);
        this.parent = parent;
        this.entries = entries;
    }

    public void add(IBlockState state) {
        entries.add(state);
    }

    @Override
    protected int getSize() {
        return entries.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {
        selected = index;
    }

    @Override
    protected boolean isSelected(int index) {
        return selected == index;
    }

    @Override
    protected void drawBackground() {}

    @Override
    protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
        IBlockState state = entries.get(slotIdx);
        ItemStack stack = new ItemStack(ItemBlock.getItemFromBlock(state.getBlock()), 1, state.getBlock().getMetaFromState(state));
        FontRenderer font     = this.parent.mc.fontRenderer;

        String s = font.trimStringToWidth(stack.getDisplayName(), listWidth - 35).trim();
        if (stack.getDisplayName().length() > s.length())
            s += "...";

        font.drawString(s, 35, slotTop + 4, 0xFFFFFF);
        RenderHelper.enableGUIStandardItemLighting();
        parent.mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 15, slotTop);
    }

    public void remove() {
        entries.remove(selected);
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public int getTop() {
        return top;
    }

    public int getBottom() {
        return bottom;
    }
}
