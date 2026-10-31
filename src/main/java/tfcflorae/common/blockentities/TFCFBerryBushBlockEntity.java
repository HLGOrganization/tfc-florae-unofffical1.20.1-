package tfcflorae.common.blockentities;

import net.dries007.tfc.common.blockentities.BerryBushBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * TFC Florae 的果树树叶方块实体。
 *
 * <p><b>为什么必须另建一个类，而不是直接用 TFC 的 {@link BerryBushBlockEntity}</b>
 * （对照 Firmalife 的 {@code FLBerryBushBlockEntity}，它做的是同一件事）：
 *
 * <ol>
 *   <li>{@code BlockEntityType} 自带一份<b>固定的</b> {@code validBlocks}。TFC 的
 *       {@code tfc:berry_bush} 只登记了 TFC 自己的方块，我们的
 *       {@code tfcflorae:plant/<tree>_leaves} 不在其中。</li>
 *
 *   <li>真正致命的判定在 {@code LevelChunk.BoundTickingBlockEntity.tick()}：
 *       <pre>
 *       if (blockEntity.getType().isValid(state)) {
 *           ticker.tick(level, pos, state, blockEntity);   // 只有这里才真正调用 serverTick
 *       } else {
 *           if (!loggedInvalidBlockState) LOGGER.warn("Block entity {} @ {} state {} invalid for ticking:", ...);
 *       }
 *       </pre>
 *       类型不认这个方块，ticker 就<b>永远不被调用</b>，只留一条 warning。
 *       于是 {@code BerryBushBlockEntity.serverTick} 永不执行 →
 *       {@code checkForCalendarUpdate} 永不执行 → {@code onCalendarUpdate} 永不执行 →
 *       <b>{@code /addday} 对果树完全无效</b>，只剩 {@code IBushBlock.randomTick}
 *       每游戏日约一次地逐叶推进，表现为「挨个开花、频率很低」。
 *       日志里那条 {@code "Block entity tfc:berry_bush @ ... invalid for ticking"} 就是它。</li>
 *
 *   <li>TFC 的 {@code BerryBushBlockEntity(BlockPos, BlockState)} 构造器把类型<b>写死</b>成
 *       {@code TFCBlockEntities.BERRY_BUSH.get()}，所以光在 {@code ExtendedProperties} 里换类型没用
 *       —— 必须由子类把类型改绑到我们自己的注册项
 *       （{@code TFCFBlockEntities.BERRY_BUSH}），它的 {@code validBlocks} 才包含我们的树叶。</li>
 * </ol>
 *
 * <p><b>为什么树苗和生长中的树枝不需要同样的处理</b>：那两处 {@code ExtendedProperties}
 * 只有 {@code blockEntity(TICK_COUNTER)}，<b>没有 {@code serverTicks(...)}</b>，
 * 逻辑走的是方块自己的 {@code randomTick}/{@code tick} 读 {@code getTicksSinceUpdate()}，
 * 不依赖方块实体 ticker；而且 TFC 的 {@code FruitTreeSaplingBlock.setPlacedBy} 会调用
 * {@code TickCounterBlockEntity.reset(level, pos)}，该方法<b>按 TFC 的 {@code TICK_COUNTER} 类型查方块实体</b>，
 * 所以那两处必须继续用 TFC 的 {@code TICK_COUNTER}，否则 reset 找不到、{@code lastUpdateTick} 停在 0、
 * 树苗会在第一个 randomTick 立刻长成。
 */
public class TFCFBerryBushBlockEntity extends BerryBushBlockEntity
{
    public TFCFBerryBushBlockEntity(BlockPos pos, BlockState state)
    {
        super(TFCFBlockEntities.BERRY_BUSH.get(), pos, state);
    }
}
