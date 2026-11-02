package tfcflorae.common.blocks.rock;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.dries007.tfc.common.blocks.ExtendedProperties;

import org.jetbrains.annotations.Nullable;

/**
 * 间歇泉方块：周期性地朝上喷射粒子与音效，作为地热/火山区的景观地标。
 * <p>
 * 本类原继承 {@code TFCFPointedDripstoneBlock}（1.18 版），滴水石整体移除后
 * 改为独立的普通方块，只保留「向上喷粒子」这一可见效果。
 */
public class Geyser extends Block
{
    private final boolean spawnParticles;

    public static Geyser create(ExtendedProperties properties, @Nullable Supplier<? extends Block> thisBlock, boolean spawnParticles)
    {
        return new Geyser(properties, spawnParticles);
    }

    public Geyser(ExtendedProperties properties, boolean spawnParticles)
    {
        super(properties.properties());
        this.spawnParticles = spawnParticles;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, RandomSource random)
    {
        // 服务端随机刻里通过 level.sendParticles 广播，客户端自然能看到
        spawn(level, pos, random);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random)
    {
        if (level.isClientSide)
        {
            spawn(level, pos, random);
        }
    }

    private void spawn(Level level, BlockPos pos, RandomSource random)
    {
        if (!spawnParticles)
        {
            return;
        }
        final double x = pos.getX() + 0.5;
        final double y = pos.getY() + random.nextDouble() + random.nextDouble();
        final double z = pos.getZ() + 0.5;
        final double spread = Mth.clamp(random.nextDouble() * 0.1, 0.0, 0.07);
        final boolean offset = random.nextBoolean();

        if (random.nextDouble() < 0.05 - spread)
        {
            level.playLocalSound(x, pos.getY() + 0.5, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
        }

        level.addParticle(ParticleTypes.CLOUD, true,
            x + random.nextDouble() / 2.5 * (offset ? 1.0 : -1.0),
            y,
            z + random.nextDouble() / 2.5 * (offset ? 1.0 : -1.0),
            spread, 0.07 + spread, spread);
    }
}
