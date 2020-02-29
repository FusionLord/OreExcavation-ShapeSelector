package net.fusionlord.mods.oreexcavation.shapeselector;

import net.fusionlord.mods.oreexcavation.shapeselector.screens.ShapeSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import oreexcavation.client.ExcavationKeys;
import oreexcavation.shapes.ShapeRegistry;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber
@Mod(ShapeSelector.MODID)
public class ShapeSelector
{
    public static final String MODID = "oeshapeselector";

    public static Field curShape;
    public static int depth = 16;

    public ShapeSelector() {
        curShape = ObfuscationReflectionHelper.findField(ShapeRegistry.class, "curShape");
    }

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

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKeyPressed(InputEvent.KeyInputEvent event)
    {
        if(ExcavationKeys.shapeKey.isPressed() && Minecraft.getInstance().currentScreen == null) {
            Minecraft.getInstance().displayGuiScreen(new ShapeSelectionScreen());
        }
    }
}
