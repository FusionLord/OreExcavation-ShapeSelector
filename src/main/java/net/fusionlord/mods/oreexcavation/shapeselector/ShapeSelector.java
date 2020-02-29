package net.fusionlord.mods.oreexcavation.shapeselector;

import net.fusionlord.mods.oreexcavation.shapeselector.screens.ShapeSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import oreexcavation.client.ExcavationKeys;
import oreexcavation.shapes.ShapeRegistry;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;

@Mod(modid = ShapeSelector.MODID, clientSideOnly = true, dependencies = "after:oreexcavation")
public class ShapeSelector
{
    public static final String MODID = "oeshapeselector";
    public static final KeyBinding shapeSelector = new KeyBinding(MODID + ".key", Keyboard.KEY_V, I18n.format(ShapeSelector.MODID + ".title"));

    public static Field curShape;
    public static int depth = 16;

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
        ClientRegistry.registerKeyBinding(shapeSelector);
        MinecraftForge.EVENT_BUS.register(this);
        curShape = ReflectionHelper.findField(ShapeRegistry.class, "curShape");
        KeyBinding[] keybinds = Minecraft.getMinecraft().gameSettings.keyBindings;
        Minecraft.getMinecraft().gameSettings.keyBindings = ArrayUtils.remove(keybinds, ArrayUtils.indexOf(keybinds, ExcavationKeys.shapeKey));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onKeyPressed(InputEvent event)
    {
        if(shapeSelector.isPressed() && Minecraft.getMinecraft().currentScreen == null)
            Minecraft.getMinecraft().displayGuiScreen(new ShapeSelectionScreen());
    }
}
