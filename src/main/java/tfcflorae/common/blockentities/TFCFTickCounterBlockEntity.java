package tfcflorae.common.blockentities;

import net.dries007.tfc.common.blockentities.TickCounterBlockEntity;
import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * TFC Florae 的 tick 计数器。
 * <p>
 * 继承 TFC 的 {@link TickCounterBlockEntity} 而不是自己重写一份 —— 这一点很关键：
 * <ul>
 *   <li>TFC 的 Jade 插件判定条件是 {@code blockEntity instanceof TickCounterBlockEntity}
 *       （见 TFC {@code BlockEntityTooltips} 的 sapling 提示），
 *       如果这里另造一个继承 {@code TFCBlockEntity} 的类，Jade 就不会显示树苗剩余生长天数；</li>
 *   <li>逻辑与 TFC 完全一致（同一个 lastUpdateTick 语义）。</li>
 * </ul>
 * <p>
 * <b>必须覆盖 reset</b>：TFC 的 {@code TickCounterBlockEntity.reset(Level, BlockPos)} 内部是
 * <pre>level.getBlockEntity(pos, TFCBlockEntities.TICK_COUNTER.get())</pre>
 * ——它只认 TFC 自己的方块实体类型。我们的树苗注册在 {@code tfcflorae:tfcf_tick_counter} 上，
 * 如果直接转调 TFC 的 reset，查找会失败并<b>静默什么也不做</b>，
 * {@code lastUpdateTick} 保持 0，导致 {@code getTicksSinceUpdate()} 返回从纪元到现在的全部 tick 数，
 * 树苗就会在放置后的第一个 randomTick 立刻长成大树（而且不吃骨粉、不受树木肥料影响）。
 * <p>
 * 因此这里按 TFC 的语义、用我们自己的类型重新实现一遍。
 */
public class TFCFTickCounterBlockEntity extends TickCounterBlockEntity {
   /**
    * 把 {@code pos} 处的计数器归零。与 TFC 同名方法语义一致，只是查询使用本模组的方块实体类型。
    */
   public static void reset(Level level, BlockPos pos) {
      level.getBlockEntity(pos, TFCFBlockEntities.TICK_COUNTER.get())
         .ifPresent(TickCounterBlockEntity::resetCounter);
   }

   public TFCFTickCounterBlockEntity(BlockPos pos, BlockState state) {
      this(TFCFBlockEntities.TICK_COUNTER.get(), pos, state);
   }

   public TFCFTickCounterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   /**
    * 与 TFC {@link TickCounterBlockEntity#resetCounter()} 完全一致：以服务端日历当前 tick 作为基准。
    */
   @Override
   public void resetCounter() {
      setLastUpdateTick(Calendars.SERVER.getTicks());
      setChanged();
   }
}
