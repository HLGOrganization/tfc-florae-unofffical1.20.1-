package tfcflorae.common.blocks.wood;

import net.dries007.tfc.common.blockentities.TickCounterBlockEntity;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.wood.TFCSaplingBlock;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.registry.RegistryWood;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import tfcflorae.util.TFCFLog;

/**
 * TFC Florae 的树苗。
 * <p>
 * <b>本类不重写任何成长逻辑</b>：{@code randomTick}、{@code mayPlaceOn}、{@code updateShape} 等全部走
 * TFC 原生 {@link TFCSaplingBlock}。1.18.2 版本曾自行实现并额外挂了 {@code fluid} 属性，
 * 而 TFC 1.20.1 的树苗根本没有流体属性。
 * <p>
 * 保留的三件事：
 * <ol>
 *   <li>放置时用 TFC 的 {@link TickCounterBlockEntity#reset} 归零计数器；</li>
 *   <li>日志诊断（{@code -Dtfcf.debug.sapling=true}）；</li>
 *   <li>潜行 + 空手右键树苗时，把生长状态直接显示在聊天栏（任何人在游戏里都能用，不需要看日志）。</li>
 * </ol>
 */
public class TFCFSaplingBlock extends TFCSaplingBlock {
   public final RegistryWood wood;
   private final ExtendedProperties properties;

   public TFCFSaplingBlock(RegistryWood wood, net.dries007.tfc.world.feature.tree.TFCTreeGrower tree, ExtendedProperties properties) {
      super(tree, properties, wood::daysToGrow, false);
      this.properties = properties;
      this.wood = wood;
   }

   public ExtendedProperties getExtendedProperties() {
      return this.properties;
   }

   @Override
   public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      TickCounterBlockEntity.reset(level, pos);
      super.setPlacedBy(level, pos, state, placer, stack);

      if (TFCFLog.sapling()) {
         final var be = level.getBlockEntity(pos);
         final var counter = (be instanceof TickCounterBlockEntity t) ? t : null;
         TFCFLog.saplingInfo("[放置] %s @%s | BE=%s | counter=%s | lastUpdateTick=%s | daysToGrow=%d",
            wood.getSerializedName(), pos,
            be == null ? "null" : be.getClass().getSimpleName(),
            counter == null ? "不是TickCounter" : "OK",
            counter == null ? "-" : String.valueOf(counter.getLastUpdateTick()),
            this.getDaysToGrow());
      }
   }

   @Override
   public net.minecraft.world.InteractionResult use(BlockState state, Level level, BlockPos pos,
                                                   net.minecraft.world.entity.player.Player player,
                                                   net.minecraft.world.InteractionHand hand,
                                                   net.minecraft.world.phys.BlockHitResult hit) {
      // 潜行 + 空手右键：直接在聊天栏报告这棵树的生长状态，方便玩家自行确认
      if (player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()
         && !level.isClientSide() && level instanceof ServerLevel server) {
         final var be = level.getBlockEntity(pos);
         final var counter = (be instanceof TickCounterBlockEntity t) ? t : null;
         final double modifier = TFCConfig.SERVER.globalSaplingGrowthModifier.get();
         final long need = (long) (24000L * this.getDaysToGrow() * modifier);
         player.displayClientMessage(Component.literal(
            "§e[TFC Florae 树苗诊断] " + wood.getSerializedName()), false);
         player.displayClientMessage(Component.literal(
            "  方块实体: " + (be == null ? "§c无" : "§a" + be.getClass().getSimpleName())), false);
         if (counter == null) {
            player.displayClientMessage(Component.literal(
               "  §c计数器缺失 -> 永远不会生长"), false);
         } else {
            final long ticks = counter.getTicksSinceUpdate();
            player.displayClientMessage(Component.literal(
               "  已计时: " + ticks + " tick"), false);
            player.displayClientMessage(Component.literal(
               "  需要:   " + need + " tick (" + this.getDaysToGrow() + " 天 × 系数 " + modifier + ")"), false);
            player.displayClientMessage(Component.literal(
               ticks > need ? "  §a已达到生长条件，下次随机刻尝试成树"
                            : "  §7还需 " + (need - ticks) + " tick"), false);
         }
         final int light = level.getMaxLocalRawBrightness(pos.above());
         player.displayClientMessage(Component.literal(
            "  光照: " + light + (light >= 9 ? " §a(够)" : " §c(不足9，永远不生长)")), false);
         player.displayClientMessage(Component.literal(
            "  注意：TFC 树苗每次随机刻只有 1/7 概率检查，可能看起来很久"), false);
         return net.minecraft.world.InteractionResult.SUCCESS;
      }
      return super.use(state, level, pos, player, hand, hit);
   }

   @Override
   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (TFCFLog.sapling()) {
         final var be = level.getBlockEntity(pos);
         final var counter = (be instanceof TickCounterBlockEntity t) ? t : null;
         final boolean light = level.getMaxLocalRawBrightness(pos.above()) >= 9;
         if (counter == null) {
            TFCFLog.saplingInfo("[随机刻] %s @%s | 光照够=%s | BE=%s -> 没有 TickCounter",
               wood.getSerializedName(), pos, light, be == null ? "null" : be.getClass().getSimpleName());
         } else {
            final long ticks = counter.getTicksSinceUpdate();
            final double need = (double) (24000 * this.getDaysToGrow())
               * TFCConfig.SERVER.globalSaplingGrowthModifier.get();
            TFCFLog.saplingInfo("[随机刻] %s @%s | 光照够=%s | ticks=%d | 需要>%.0f | 通过=%s",
               wood.getSerializedName(), pos, light, ticks, need, ticks > need);
         }
      }
      super.randomTick(state, level, pos, random);
   }
}
