package tfcflorae.world.feature.tree;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.util.RandomSource;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TrunkVineDecorator;
import tfcflorae.world.feature.TFCFFeatures;

public class BambooLeavesDecorator extends TrunkVineDecorator {
   public static final Codec<BambooLeavesDecorator> CODEC = BlockStateProvider.CODEC
      .fieldOf("block_provider")
      .xmap(BambooLeavesDecorator::new, decorator -> decorator.inputState)
      .codec();
   private final BlockStateProvider inputState;

   public BambooLeavesDecorator(BlockStateProvider inputState) {
      this.inputState = inputState;
   }

   protected TreeDecoratorType<?> type() {
      return TFCFFeatures.BAMBOO_LEAVES.get();
   }

   public void place(
      LevelSimulatedReader levelReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource random, List<BlockPos> listBlockPos, List<BlockPos> listBlockPos2
   ) {
      BlockPos newpos = listBlockPos.get(listBlockPos.size() - 1);
      BlockPos up1 = new BlockPos(newpos.getX(), newpos.getY() + 5, newpos.getZ());
      BlockPos up2 = new BlockPos(newpos.getX(), newpos.getY() + 6, newpos.getZ());
      BlockPos px1 = new BlockPos(newpos.getX() + 2, newpos.getY(), newpos.getZ());
      BlockPos px2 = new BlockPos(newpos.getX() + 2, newpos.getY(), newpos.getZ() + 1);
      BlockPos px3 = new BlockPos(newpos.getX() + 2, newpos.getY(), newpos.getZ() - 1);
      BlockPos nx1 = new BlockPos(newpos.getX() - 2, newpos.getY(), newpos.getZ());
      BlockPos nx2 = new BlockPos(newpos.getX() - 2, newpos.getY(), newpos.getZ() + 1);
      BlockPos nx3 = new BlockPos(newpos.getX() - 2, newpos.getY(), newpos.getZ() - 1);
      BlockPos pz1 = new BlockPos(newpos.getX(), newpos.getY(), newpos.getZ() + 2);
      BlockPos pz2 = new BlockPos(newpos.getX() + 1, newpos.getY(), newpos.getZ() + 2);
      BlockPos pz3 = new BlockPos(newpos.getX() - 1, newpos.getY(), newpos.getZ() + 2);
      BlockPos nz1 = new BlockPos(newpos.getX(), newpos.getY(), newpos.getZ() - 2);
      BlockPos nz2 = new BlockPos(newpos.getX() + 1, newpos.getY(), newpos.getZ() - 2);
      BlockPos nz3 = new BlockPos(newpos.getX() - 1, newpos.getY(), newpos.getZ() - 2);
      BlockPos px = new BlockPos(newpos.getX() + 1, newpos.getY(), newpos.getZ());
      BlockPos nx = new BlockPos(newpos.getX() - 1, newpos.getY(), newpos.getZ());
      BlockPos pz = new BlockPos(newpos.getX(), newpos.getY(), newpos.getZ() + 1);
      BlockPos nz = new BlockPos(newpos.getX(), newpos.getY(), newpos.getZ() - 1);
      BlockPos pxt = px.above();
      BlockPos nxt = nx.above();
      BlockPos pzt = pz.above();
      BlockPos nzt = nz.above();
      BlockPos pxq = pxt.above();
      BlockPos nxq = nxt.above();
      BlockPos pzq = pzt.above();
      BlockPos nzq = nzt.above();
      BlockPos pxc = pxq.above();
      BlockPos nxc = nxq.above();
      BlockPos pzc = pzq.above();
      BlockPos nzc = nzq.above();
      BlockPos pxz = pxc.above();
      BlockPos nxz = nxc.above();
      BlockPos pzz = pzc.above();
      BlockPos nzz = nzc.above();
      BlockPos pxo = px.below();
      BlockPos nxo = nx.below();
      BlockPos pzo = pz.below();
      BlockPos nzo = nz.below();
      BlockPos pxm = pxo.below();
      BlockPos nxm = nxo.below();
      BlockPos pzm = pzo.below();
      BlockPos nzm = nzo.below();
      BlockPos pxp = pxm.below();
      BlockPos nxp = nxm.below();
      BlockPos pzp = pzm.below();
      BlockPos nzp = nzm.below();
      if (levelReader.isStateAtPosition(px1, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(px1, this.inputState.getState(random, px1));
      }

      if (levelReader.isStateAtPosition(px2, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(px2, this.inputState.getState(random, px2));
      }

      if (levelReader.isStateAtPosition(px3, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(px3, this.inputState.getState(random, px3));
      }

      if (levelReader.isStateAtPosition(nx1, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(nx1, this.inputState.getState(random, nx1));
      }

      if (levelReader.isStateAtPosition(nx2, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(nx2, this.inputState.getState(random, nx2));
      }

      if (levelReader.isStateAtPosition(nx3, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(nx3, this.inputState.getState(random, nx3));
      }

      if (levelReader.isStateAtPosition(pz1, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(pz1, this.inputState.getState(random, pz1));
      }

      if (levelReader.isStateAtPosition(pz2, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(pz2, this.inputState.getState(random, pz2));
      }

      if (levelReader.isStateAtPosition(pz3, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(pz3, this.inputState.getState(random, pz3));
      }

      if (levelReader.isStateAtPosition(nz1, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(nz1, this.inputState.getState(random, nz1));
      }

      if (levelReader.isStateAtPosition(nz2, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(nz2, this.inputState.getState(random, nz2));
      }

      if (levelReader.isStateAtPosition(nz3, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(nz3, this.inputState.getState(random, nz3));
      }

      if (levelReader.isStateAtPosition(px, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(px, this.inputState.getState(random, px));
      }

      if (levelReader.isStateAtPosition(nx, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(nx, this.inputState.getState(random, nx));
      }

      if (levelReader.isStateAtPosition(pz, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(pz, this.inputState.getState(random, pz));
      }

      if (levelReader.isStateAtPosition(nz, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(nz, this.inputState.getState(random, nz));
      }

      if (levelReader.isStateAtPosition(pxt, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(pxt, this.inputState.getState(random, pxt));
      }

      if (levelReader.isStateAtPosition(nxt, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(nxt, this.inputState.getState(random, nxt));
      }

      if (levelReader.isStateAtPosition(pzt, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(pzt, this.inputState.getState(random, pzt));
      }

      if (levelReader.isStateAtPosition(nzt, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(nzt, this.inputState.getState(random, nzt));
      }

      if (levelReader.isStateAtPosition(pxq, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(pxq, this.inputState.getState(random, pxq));
      }

      if (levelReader.isStateAtPosition(nxq, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(nxq, this.inputState.getState(random, nxq));
      }

      if (levelReader.isStateAtPosition(pzq, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(pzq, this.inputState.getState(random, pzq));
      }

      if (levelReader.isStateAtPosition(nzq, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(nzq, this.inputState.getState(random, nzq));
      }

      if (levelReader.isStateAtPosition(pxc, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(pxc, this.inputState.getState(random, pxc));
      }

      if (levelReader.isStateAtPosition(nxc, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(nxc, this.inputState.getState(random, nxc));
      }

      if (levelReader.isStateAtPosition(pzc, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(pzc, this.inputState.getState(random, pzc));
      }

      if (levelReader.isStateAtPosition(nzc, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(nzc, this.inputState.getState(random, nzc));
      }

      if (levelReader.isStateAtPosition(pxz, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(pxz, this.inputState.getState(random, pxz));
      }

      if (levelReader.isStateAtPosition(nxz, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(nxz, this.inputState.getState(random, nxz));
      }

      if (levelReader.isStateAtPosition(pzz, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(pzz, this.inputState.getState(random, pzz));
      }

      if (levelReader.isStateAtPosition(nzz, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(nzz, this.inputState.getState(random, nzz));
      }

      if (levelReader.isStateAtPosition(up1, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(up1, this.inputState.getState(random, up1));
      }

      if (levelReader.isStateAtPosition(up2, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(up2, this.inputState.getState(random, up2));
      }

      if (levelReader.isStateAtPosition(pxo, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(pxo, this.inputState.getState(random, pxo));
      }

      if (levelReader.isStateAtPosition(nxo, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(nxo, this.inputState.getState(random, nxo));
      }

      if (levelReader.isStateAtPosition(pzo, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(pzo, this.inputState.getState(random, pzo));
      }

      if (levelReader.isStateAtPosition(nzo, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(nzo, this.inputState.getState(random, nzo));
      }

      if (levelReader.isStateAtPosition(pxm, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(pxm, this.inputState.getState(random, pxm));
      }

      if (levelReader.isStateAtPosition(nxm, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(nxm, this.inputState.getState(random, nxm));
      }

      if (levelReader.isStateAtPosition(pzm, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(pzm, this.inputState.getState(random, pzm));
      }

      if (levelReader.isStateAtPosition(nzm, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(nzm, this.inputState.getState(random, nzm));
      }

      if (levelReader.isStateAtPosition(pxp, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(pxp, this.inputState.getState(random, pxp));
      }

      if (levelReader.isStateAtPosition(nxp, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(nxp, this.inputState.getState(random, nxp));
      }

      if (levelReader.isStateAtPosition(pzp, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(pzp, this.inputState.getState(random, pzp));
      }

      if (levelReader.isStateAtPosition(nzp, net.minecraft.world.level.block.state.BlockState::isAir)) {
         biConsumer.accept(nzp, this.inputState.getState(random, nzp));
      }
   }
}
