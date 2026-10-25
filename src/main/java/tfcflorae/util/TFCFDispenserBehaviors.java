package tfcflorae.util;

import net.dries007.tfc.common.blocks.wood.Wood.BlockType;
import net.dries007.tfc.util.DispenserBehaviors;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.items.TFCFItems;

public final class TFCFDispenserBehaviors {
   public static final DispenseItemBehavior DEFAULT = new DefaultDispenseItemBehavior();

   public TFCFDispenserBehaviors() {
   }

   public static void registerDispenserBehaviors() {
      TFCFBlocks.WOODS
         .values()
         .stream()
         .map(map -> (Block)map.get(BlockType.CHEST).get())
         .forEach(chest -> DispenserBlock.registerBehavior(chest, DispenserBehaviors.CHEST_BEHAVIOR));
      TFCFItems.CHEST_MINECARTS.values().forEach(reg -> DispenserBlock.registerBehavior((ItemLike)reg.get(), DispenserBehaviors.MINECART_BEHAVIOR));
   }
}
