package com.codesloth.easiershulkers;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

import static com.codesloth.easiershulkers.Utils.isShulkerBox;

@Mod.EventBusSubscriber(modid = EasierShulkers.MOD_ID)
public class Events {

    public static boolean IS_SERVER;

    @SubscribeEvent
    public void onJoinWorld(WorldEvent.Load event) {
        try{
            boolean isLan = Objects.requireNonNull(Minecraft.getInstance().getCurrentServer()).isLan();
            if (IS_SERVER == isLan){
                IS_SERVER = !isLan;
            }
        } catch (Exception ignore){}
    }

    @SubscribeEvent
    public void onPlaceBlock(PlayerInteractEvent.RightClickBlock event){
        Player player = event.getPlayer();
        if(IS_SERVER || player == null || player instanceof FakePlayer || player.containerMenu instanceof ShulkerBoxMenu)
            return;

        ItemStack mainHandItem = player.getMainHandItem();
        if(!isShulkerBox(mainHandItem))
            return;

        InteractionHand hand = player.getUsedItemHand();
        if (hand.equals(InteractionHand.OFF_HAND) && Utils.getShulkerBox(player, InteractionHand.MAIN_HAND) != null) {
            event.setCanceled(true);
            return;
        }

        ItemStack stack = Utils.getShulkerBox(player, hand);
        if (!player.isShiftKeyDown()) {
            event.setCanceled(true);
            Utils.openShulkerBox(player, stack);
        }
    }

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getPlayer();
        if (IS_SERVER || player instanceof FakePlayer || player.containerMenu instanceof ShulkerBoxMenu)
            return;

        InteractionHand hand = event.getHand();
        if (hand.equals(InteractionHand.OFF_HAND) && Utils.getShulkerBox(player, InteractionHand.MAIN_HAND) != null) {
            event.setCanceled(true);
            return;
        }

        ItemStack stack = Utils.getShulkerBox(player, hand);
        if (stack != null) {
            Utils.openShulkerBox(player, stack);
        }
    }

}
