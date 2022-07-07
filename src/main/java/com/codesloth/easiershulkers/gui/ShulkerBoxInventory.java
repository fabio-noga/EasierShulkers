package com.codesloth.easiershulkers.gui;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import javax.annotation.Nullable;
import java.util.Objects;

public class ShulkerBoxInventory implements Container, MenuProvider {

    protected NonNullList<ItemStack> items;
    protected ItemStack shulkerBox;
    private static final int INV_SIZE = 27;
    protected CompoundTag blockEntityTag;

    protected ResourceLocation lootTable;
    protected long lootTableSeed;

    public ShulkerBoxInventory(Player player, ItemStack shulkerBox) {
        this.shulkerBox = shulkerBox;
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        startOpen(player);

        CompoundTag c = shulkerBox.getTag();

        if (c == null || !c.contains("BlockEntityTag")) {
            return;
        }

        blockEntityTag = c.getCompound("BlockEntityTag");

        if (blockEntityTag.contains("Items")) {
            ContainerHelper.loadAllItems(blockEntityTag, items);
        } else if (blockEntityTag.contains("LootTable")) {
            lootTable = new ResourceLocation(blockEntityTag.getString("LootTable"));
            lootTableSeed = blockEntityTag.getLong("LootTableSeed");
            fillWithLoot(player);
            blockEntityTag.remove("LootTable");
            blockEntityTag.remove("LootTableSeed");
        }
    }

    public void fillWithLoot(@Nullable Player player) {
        if (lootTable != null && player != null) {
            LootTable loottable = Objects.requireNonNull(player.level.getServer()).getLootTables().get(lootTable);
            lootTable = null;

            LootContext.Builder builder = new LootContext.Builder((ServerLevel) player.level);

            if (lootTableSeed != 0L) {
                builder.withOptionalRandomSeed(lootTableSeed);
            }

            builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);

            loottable.fill(this, builder.create(LootContextParamSets.CHEST));
            setChanged();
        }
    }

    @Override
    public int getContainerSize() {
        return INV_SIZE;
    }

    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack itemstack = ContainerHelper.removeItem(items, index, count);
        setChanged();
        return itemstack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack stack = ContainerHelper.takeItem(items, index);
        setChanged();
        return stack;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        items.set(index, stack);
        setChanged();
    }

    @Override
    public void setChanged() {
        shulkerBox.getOrCreateTag()
                  .put("BlockEntityTag",
                          Objects.requireNonNullElseGet(blockEntityTag, () -> blockEntityTag = new CompoundTag()));

        ContainerHelper.saveAllItems(blockEntityTag, items, true);
    }

    @Override
    public void startOpen(Player player) {
        player.level.playSound(null, player.getX(), player.getY(), player.getZ(), getOpenSound(), SoundSource.BLOCKS, 0.5F, 1.0f);
    }

    @Override
    public void stopOpen(Player player) {
        setChanged();
        player.level.playSound(null, player.getX(), player.getY(), player.getZ(), getCloseSound(), SoundSource.BLOCKS, 0.5F, player.level.random.nextFloat() * 0.1F + 0.9F);
    }

    protected SoundEvent getOpenSound() {
        return SoundEvents.SHULKER_BOX_OPEN;
    }

    protected SoundEvent getCloseSound() {
        return SoundEvents.SHULKER_BOX_CLOSE;
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public boolean stillValid(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (player.getItemInHand(hand).equals(shulkerBox)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Component getDisplayName() {
        return shulkerBox.getHoverName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new ShulkerBoxMenu(i, playerInventory);
    }

}
