package tfcflorae.common.blocks.rock;

import net.dries007.tfc.common.blocks.ExtendedBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.util.registry.RegistryRock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;

public enum TFCFOre {
   ANTHRACITE(false);

   private final boolean graded;

   private TFCFOre(boolean graded) {
      this.graded = graded;
   }

   public boolean isGraded() {
      return this.graded;
   }

   public Block create(RegistryRock rock) {
      Properties properties = Properties.of().mapColor(MapColor.STONE)
         .sound(SoundType.STONE)
         .strength(rock.category().hardness(6.5F), 10.0F)
         .requiresCorrectToolForDrops();
      return (Block)(this == ANTHRACITE ? new ExtendedBlock(ExtendedProperties.of(properties).flammable(10, 100)) : new Block(properties));
   }

   public static enum Grade {
      POOR,
      NORMAL,
      RICH;

      private static final TFCFOre.Grade[] VALUES = values();

      private Grade() {
      }

      public static TFCFOre.Grade valueOf(int i) {
         return i >= 0 && i < VALUES.length ? VALUES[i] : NORMAL;
      }
   }
}
