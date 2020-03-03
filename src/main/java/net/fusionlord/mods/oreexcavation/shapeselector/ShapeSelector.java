package net.fusionlord.mods.oreexcavation.shapeselector;

import com.google.common.collect.Lists;
import net.fusionlord.mods.oreexcavation.shapeselector.screens.ShapeSelectionScreen;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import oreexcavation.client.ExcavationKeys;
import oreexcavation.shapes.ExcavateShape;
import oreexcavation.shapes.ShapeRegistry;

import java.lang.reflect.Field;
import java.util.List;

@Mod(modid = ShapeSelector.MODID, clientSideOnly = true, dependencies = "after:oreexcavation")
@Mod.EventBusSubscriber
public class ShapeSelector
{
    public static final String MODID = "oeshapeselector";

    public static Field curShape;
    public static int depth = 16;
    public static List<IBlockState> blacklist = Lists.newArrayList();

    public static void setShape(int shape)
    {
        try
        {
            curShape.set(ShapeRegistry.INSTANCE, shape);
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPostInitializationEvent event) {
        curShape = ReflectionHelper.findField(ShapeRegistry.class, "curShape");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKeyPressed(InputEvent event)
    {
        if (ExcavationKeys.shapeKey.isPressed() && Minecraft.getMinecraft().currentScreen == null)
            Minecraft.getMinecraft().displayGuiScreen(new ShapeSelectionScreen());
    }

    @SubscribeEvent
    public static void onDrawScreen(RenderGameOverlayEvent event) {
        if (ExcavationKeys.excavateKey.isKeyDown()) {
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution sr = new ScaledResolution(mc);
            GlStateManager.pushMatrix();
            GlStateManager.translate(sr.getScaledWidth() / 2f - 30, sr.getScaledHeight() / 2f, 0);
            GlStateManager.scale(.25, .25, 0);
            ShapeSelectionScreen.drawShape(ShapeRegistry.INSTANCE.getActiveShape(), mc);
            GlStateManager.popMatrix();
        }
    }

//    @SubscribeEvent
//    public static void onPacketExcavation(EventExcavateRequest event) {
//        if (blacklist.contains(Block.getStateById(event.state)))
//            event.shape = null;
//        else
//            event.shape.setMaxDepth(depth);
//    }
}
