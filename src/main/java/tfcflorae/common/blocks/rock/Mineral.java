package tfcflorae.common.blocks.rock;
import net.minecraft.world.level.material.MapColor;

import net.dries007.tfc.common.blocks.ExtendedBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.util.registry.RegistryRock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.items.TFCFItems;

public enum Mineral {
   ;

   private final boolean graded;

   private Mineral(boolean graded) {
      this.graded = graded;
   }

   public boolean isGraded() {
      return this.graded;
   }

   public Block create(RegistryRock rock) {
      Properties properties = Properties.of().mapColor(MapColor.STONE)
         .sound(SoundType.STONE)
         .strength(rock.category().hardness(6.5F), 10.0F)
         .noCollission()
         .requiresCorrectToolForDrops();
      return new Block(properties);
   }

   @Nullable
   public static Mineral getMineral(ItemStack stack) {
      Mineral mineral = null;
      if (stack != null) {
         for (Mineral minerals : values()) {
            if (stack.getItem().equals(TFCFItems.MINERALS.get(minerals).get())) {
               mineral = minerals;
               break;
            }
         }
      }

      return mineral;
   }

   public static enum Grade {
      POOR,
      NORMAL,
      RICH;

      private static final Mineral.Grade[] VALUES = values();

      private Grade() {
      }

      public static Mineral.Grade valueOf(int i) {
         return i >= 0 && i < VALUES.length ? VALUES[i] : NORMAL;
      }
   }
}
