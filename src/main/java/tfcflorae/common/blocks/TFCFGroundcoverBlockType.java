package tfcflorae.common.blocks;

import java.util.function.Function;
import java.util.function.Supplier;
import net.dries007.tfc.common.TFCCreativeTabs;
import net.dries007.tfc.common.blocks.GroundcoverBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public enum TFCFGroundcoverBlockType {
   SEA_URCHIN(GroundcoverBlock.MEDIUM);

   private final VoxelShape shape;
   @Nullable
   private final Supplier<? extends Item> vanillaItem;

   private TFCFGroundcoverBlockType(VoxelShape shape) {
      this(shape, null);
   }

   private TFCFGroundcoverBlockType(VoxelShape shape, @Nullable Supplier<? extends Item> vanillaItem) {
      this.shape = shape;
      this.vanillaItem = vanillaItem;
   }

   public VoxelShape getShape() {
      return this.shape;
   }

   @Nullable
   public Function<Block, BlockItem> createBlockItem() {
      return this.vanillaItem == null ? block -> new BlockItem(block, new Properties()) : null;
   }

   @Nullable
   public Supplier<? extends Item> getVanillaItem() {
      return this.vanillaItem;
   }
}
