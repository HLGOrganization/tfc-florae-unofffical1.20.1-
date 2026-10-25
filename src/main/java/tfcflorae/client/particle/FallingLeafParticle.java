package tfcflorae.client.particle;

import net.dries007.tfc.client.TFCColors;
import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.plant.fruit.FruitTreeLeavesBlock;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.common.blocks.plant.fruit.FruitBlocks.Tree;
import net.dries007.tfc.common.blocks.wood.ILeavesBlock;
import net.dries007.tfc.util.Helpers;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;
import tfcflorae.common.blocks.wood.TFCFLeavesBlock;
import tfcflorae.common.blocks.wood.TFCFMangroveLeavesBlock;
import tfcflorae.common.blocks.wood.TFCFWood;
import tfcflorae.util.TFCFHelpers;

public class FallingLeafParticle extends CherryParticle {
   protected FallingLeafParticle(ClientLevel level, double x, double y, double z, SpriteSet set, boolean tinted, BlockState state) {
      super(level, x, y, z, set);
      BlockPos pos = BlockPos.containing(x, y, z);
      int color = -1;
      if (state.getBlock() instanceof FruitTreeLeavesBlock fruit) {
         for (Tree tree : Tree.values()) {
            if (state.getBlock() == ((RegistryObject)TFCBlocks.FRUIT_TREE_LEAVES.get(tree)).get()) {
               color = TFCFHelpers.getFlowerColor(tree);
            }
         }
      } else if (state.getBlock() instanceof ILeavesBlock) {
         if (isAcceptedLeaves(state)) {
            for (TFCFWood wood : TFCFWood.values()) {
               if (wood.isFruitTree() && !wood.hasFruitingLog() && !wood.hasLeavesOnly() && wood.getFloweringLeavesColor() != -1) {
                  color = wood.getFloweringLeavesColor();
               }
            }
         } else if (tinted) {
            color = Helpers.isBlock(state, Blocks.SEASONAL_LEAVES) ? TFCColors.getSeasonalFoliageColor(pos, 0, 0) : TFCColors.getFoliageColor(pos, 0);
         } else {
            color = TFCFHelpers.getAverageColor(level, state).getRGB();
         }
      }

      if (color != -1) {
         this.setColor((float)(color >> 16 & 0xFF) / 255.0F, (float)(color >> 8 & 0xFF) / 255.0F, (float)(color & 0xFF) / 255.0F);
      }
   }

   public static boolean isAcceptedLeaves(BlockState state) {
      return state.getBlock() instanceof TFCFLeavesBlock && state.getValue(TFCFLeavesBlock.LIFECYCLE) == Lifecycle.FLOWERING
         || state.getBlock() instanceof TFCFMangroveLeavesBlock && state.getValue(TFCFMangroveLeavesBlock.LIFECYCLE) == Lifecycle.FLOWERING;
   }

   public static record Provider(SpriteSet set, boolean tinted) implements ParticleProvider<BlockParticleOption> {
      public Particle createParticle(BlockParticleOption type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         FallingLeafParticle particle = new FallingLeafParticle(level, x, y, z, this.set, this.tinted, type.getState());
         particle.pickSprite(this.set);
         return particle;
      }
   }
}
