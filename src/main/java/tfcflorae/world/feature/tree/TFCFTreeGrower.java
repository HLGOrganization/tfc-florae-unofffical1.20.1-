package tfcflorae.world.feature.tree;

import net.dries007.tfc.util.registry.RegistryWood;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.chunkdata.ForestType;
import net.dries007.tfc.world.feature.tree.TFCTreeGrower;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import tfcflorae.util.TFCFLog;

/**
 * Sapling growth follows the former TFC Florae placement path: place at the
 * no-leaves surface height and use TFC forest data to choose old-growth trees.
 */
public class TFCFTreeGrower extends TFCTreeGrower
{
    public final RegistryWood wood;

    public TFCFTreeGrower(RegistryWood wood, ResourceLocation normalTree, ResourceLocation oldGrowthTree)
    {
        super(normalTree, oldGrowthTree);
        this.wood = wood;
    }

    @Override
    public boolean growTree(ServerLevel level, ChunkGenerator generator, BlockPos saplingPos, BlockState saplingState, RandomSource random)
    {
        final boolean debug = TFCFLog.sapling();
        final BlockPos treePos = new BlockPos(
            saplingPos.getX(),
            level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, saplingPos.getX(), saplingPos.getZ()),
            saplingPos.getZ()
        );

        final Registry<ConfiguredFeature<?, ?>> features = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        final ConfiguredFeature<?, ?> normalFeature = getNormalFeature(features);
        final ConfiguredFeature<?, ?> oldGrowthFeature = getOldGrowthFeature(features);

        // Keep the former TFC Florae order: remove the sapling before querying
        // TFC placement conditions and attempting the selected feature.
        level.setBlock(saplingPos, Blocks.AIR.defaultBlockState(), 4);
        final ConfiguredFeature<?, ?> feature = selectFeature(level, treePos, normalFeature, oldGrowthFeature, debug);

        if (debug)
        {
            TFCFLog.saplingInfo("[生成] wood=%s saplingPos=%s treePos=%s feature=%s",
                wood.getSerializedName(), saplingPos, treePos,
                feature == oldGrowthFeature ? "old_growth" : "normal");
        }

        final boolean placed = feature.place(level, generator, random, treePos);

        if (placed)
        {
            if (debug)
            {
                TFCFLog.saplingInfo("[生成] feature.place=true wood=%s treePos=%s root=%s",
                    wood.getSerializedName(), treePos, level.getBlockState(treePos).getBlock());
            }
            return true;
        }

        level.setBlock(saplingPos, saplingState, 4);
        if (debug)
        {
            TFCFLog.saplingInfo("[生成] feature.place=false，已恢复树苗 wood=%s saplingPos=%s treePos=%s",
                wood.getSerializedName(), saplingPos, treePos);
        }
        return false;
    }

    private ConfiguredFeature<?, ?> selectFeature(
        ServerLevel level,
        BlockPos treePos,
        ConfiguredFeature<?, ?> normalFeature,
        ConfiguredFeature<?, ?> oldGrowthFeature,
        boolean debug
    )
    {
        if (oldGrowthFeature == null || oldGrowthFeature == normalFeature)
        {
            return normalFeature;
        }

        try
        {
            final ChunkDataProvider provider = ChunkDataProvider.get(level);
            if (provider == null)
            {
                return normalFeature;
            }
            final ChunkData data = provider.get(level, treePos);
            if (data == null)
            {
                return normalFeature;
            }

            final float rainfall = data.getRainfall(treePos);
            final float forestDensity = data.getForestDensity();
            final ForestType forestType = data.getForestType();
            final boolean oldGrowth = rainfall > 0.0F
                && rainfall < 500.0F
                && forestDensity > 0.6F
                && forestType == ForestType.OLD_GROWTH;

            if (debug)
            {
                TFCFLog.saplingInfo("[选择] wood=%s rainfall=%.1f density=%.2f forest=%s old_growth=%s",
                    wood.getSerializedName(), rainfall, forestDensity, forestType, oldGrowth);
            }
            return oldGrowth ? oldGrowthFeature : normalFeature;
        }
        catch (RuntimeException exception)
        {
            if (debug)
            {
                TFCFLog.saplingInfo("[选择] TFC 森林数据读取失败，回退普通树特征：%s", exception.toString());
            }
            return normalFeature;
        }
    }
}
