package com.codesloth.easiershulkers;

import com.codesloth.easiershulkers.gui.ShulkerBoxInventory;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraftforge.network.NetworkHooks;

public class Utils {

    public static ItemStack getShulkerBox(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isShulkerBox(stack)) {
            return stack;
        }
        return null;
    }

    public static boolean isShulkerBox(ItemStack stack) {
        return stack != null && Block.byItem(stack.getItem()) instanceof ShulkerBoxBlock;
    }

    public static void openShulkerBox(Player player, ItemStack stack) {
        if (!player.level.isClientSide && player instanceof ServerPlayer) {
            NetworkHooks.openGui((ServerPlayer) player, new MenuProvider() {
                @Override
                public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                    return new ShulkerBoxMenu(id, playerInventory, new ShulkerBoxInventory(player, stack));
                }

                @Override
                public Component getDisplayName() {
                    return stack.getHoverName();
                }
            });
        }
    }

}
