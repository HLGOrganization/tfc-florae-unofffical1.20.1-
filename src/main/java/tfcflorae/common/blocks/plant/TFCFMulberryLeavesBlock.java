package tfcflorae.common.blocks.plant;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.plant.fruit.FruitTreeLeavesBlock;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.util.calendar.Season;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateRange;

import tfcflorae.common.entities.Silkmoth;
import tfcflorae.common.entities.TFCFEntities;

/**
 * 桑叶（{@code plant/mulberry_leaves}）的专用果叶子类。
 *
 * <p><b>为什么需要它</b>：蚕蛾（本模组唯一的生物）只认桑叶 —— 夏季夜晚、气温 ≥ 2℃ 时，
 * 桑叶有小概率直接羽化出一只蚕蛾（{@code SilkmothNestBlock} 也靠桑叶判断能否驻留）。
 * 这段逻辑原本写在 1.18.2 时代的混合树叶 {@code TFCFLeavesBlock.randomTick} 里，
 * 判断条件是 {@code this == WOODS_SEASONAL_LEAVES.get(MULBERRY).get()}。
 *
 * <p>现在桑树（MULBERRY）归入「只有果树形态」的树种，{@code wood/leaves/mulberry} 不再注册，
 * 那段判断会取到 {@code null} 并 NPE。桑叶只剩 {@code plant/mulberry_leaves}（本类），
 * 所以把繁殖逻辑搬到这里，行为与原版逐行一致。
 */
public class TFCFMulberryLeavesBlock extends FruitTreeLeavesBlock
{
    public TFCFMulberryLeavesBlock(ExtendedProperties properties, Supplier<? extends Item> productItem, Lifecycle[] stages, Supplier<ClimateRange> climateRange, int flowerColor)
    {
        super(properties, productItem, stages, climateRange, flowerColor);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
    {
        super.randomTick(state, level, pos, random);

        // 与原先 TFCFLeavesBlock 里的判断逐条一致：夜间、非枯萎、1/26400、夏季、气温 ≥ 2℃。
        if (!level.isDay() && state.getValue(LIFECYCLE) != Lifecycle.DORMANT)
        {
            final Month currentMonth = Calendars.SERVER.getCalendarMonthOfYear();
            final Season season = currentMonth.getSeason();
            if (random.nextInt(26400) == 0 && season == Season.SUMMER && Climate.getTemperature(level, pos) >= 2.0F)
            {
                final boolean flag = !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
                final Direction direction = Direction.getRandom(random);
                final Entity entity = TFCFEntities.SILKMOTH.get().create(level);
                if (entity instanceof Silkmoth moth)
                {
                    if (moth.getSavedTargetPos() != null && !moth.hasSavedTargetPos() && level.random.nextFloat() < 0.9F)
                    {
                        moth.setSavedTargetPos(moth.getSavedTargetPos());
                    }

                    final float f = entity.getBbWidth();
                    final double d3 = flag ? 0.0 : 0.55 + (double) (f / 2.0F);
                    final double d0 = (double) pos.getX() + 0.5 + d3 * (double) direction.getStepX();
                    final double d1 = (double) pos.getY() + 0.5 - (double) (entity.getBbHeight() / 2.0F);
                    final double d2 = (double) pos.getZ() + 0.5 + d3 * (double) direction.getStepZ();
                    entity.moveTo(d0, d1, d2, entity.getYRot(), entity.getXRot());

                    level.playSound(null, pos, SoundEvents.BEEHIVE_EXIT, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.addFreshEntity(entity);
                }
            }
        }
    }
}
