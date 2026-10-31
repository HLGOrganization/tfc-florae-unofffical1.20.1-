package tfcflorae.common.blocks.plant;

import java.util.List;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import net.dries007.tfc.common.blockentities.BerryBushBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.plant.fruit.FruitTreeBranchBlock;
import net.dries007.tfc.common.blocks.plant.fruit.FruitTreeLeavesBlock;
import net.dries007.tfc.common.blocks.plant.fruit.FruitTreeSaplingBlock;
import net.dries007.tfc.common.blocks.plant.fruit.GrowingFruitTreeBranchBlock;

import tfcflorae.common.blockentities.TFCFBlockEntities;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.wood.TFCFWood;
import tfcflorae.util.climate.TFCFClimateRanges;

/**
 * TFC 3.2 新增的「果树」（fruit tree）是一套独立于普通树的方块组，和 {@code TFCFWood} 那套
 * {@code wood/leaves/<wood>} 完全不同：
 * <ul>
 *   <li>{@code plant/<tree>_sapling} 果树树苗（可入盆）</li>
 *   <li>{@code plant/<tree>_growing_branch} 生长中的果树枝干（有方块实体、会自己长成树）</li>
 *   <li>{@code plant/<tree>_branch} 长成的果树枝干</li>
 *   <li>{@code plant/<tree>_leaves} 果树树叶（生命周期的四个贴图：健康/开花/结果/枯萎）</li>
 * </ul>
 *
 * <p>TFC 自己的 {@code FruitBlocks.Tree} 是封闭枚举，附属没法往里加树种，所以这里走 Firmalife
 * 可可树那条路：直接实例化 TFC 的 {@code FruitTree*Block}（它们的构造器都是 public，
 * 且 {@code createTree} 用的就是 TFC 自己的 {@code TickCounterBlockEntity}），
 * 只把「树种」这一维换成 {@link TFCFWood}。
 *
 * <p>杜松（juniper / red_cedar / white_cedar）不在此列：它们是针叶树，树叶不是阔叶形态，
 * 套果树叶子的四个生命周期贴图不合适，用户明确要求跳过。
 */
public final class TFCFFruitTrees
{
    /**
     * 需要按 TFC 果树方式注册的树种。顺序决定创造栏顺序。
     * <p>
     * <b>11 种</b>。已按用户要求移除三种：
     * <ul>
     *   <li>{@code JOSHUA}（短叶丝兰）—— 只删果树形态，保留木材形态，所以它仍留在 {@link TFCFWood} 里；</li>
     *   <li>{@code SLOE}（黑刺李）、{@code ROWAN}（花楸）—— 连同 {@link TFCFWood} 常量一起彻底删除。</li>
     * </ul>
     * 剩下这 11 种反过来：<b>只有果树形态</b>，不注册 {@code wood/*} 木材方块
     * （见 {@link TFCFWood#hasWoodSet()} 的精确名单）。
     */
    public static final List<TFCFWood> TREES = List.of(
        TFCFWood.HAWTHORN,
        TFCFWood.JABUTICABEIRA,
        TFCFWood.MACLURA,
        TFCFWood.MEDLAR,
        TFCFWood.MULBERRY,
        TFCFWood.PEAR,
        TFCFWood.PERSIMMON,
        TFCFWood.QUINCE,
        TFCFWood.SORB,
        TFCFWood.SYZYGIUM,
        TFCFWood.YEW
    );

    private TFCFFruitTrees()
    {
    }

    /** 果树树苗：长成 {@code plant/<tree>_growing_branch}。 */
    public static Block createSapling(TFCFWood wood)
    {
        return new FruitTreeSaplingBlock(
            ExtendedProperties.of(MapColor.PLANT)
                .noCollission()
                .randomTicks()
                .strength(0.0F)
                .sound(SoundType.GRASS)
                .blockEntity(TFCBlockEntities.TICK_COUNTER)
                .flammableLikeLeaves(),
            TFCFBlocks.FRUIT_TREE_GROWING_BRANCHES.get(wood),
            wood.daysToGrow(),
            TFCFClimateRanges.FRUIT_TREES.get(wood),
            wood.getStages()
        );
    }

    /** 盆里的果树树苗。 */
    public static Block createPottedSapling(TFCFWood wood)
    {
        return new FlowerPotBlock(
            () -> (FlowerPotBlock) Blocks.FLOWER_POT,
            TFCFBlocks.FRUIT_TREE_SAPLINGS.get(wood),
            BlockBehaviour.Properties.of().instabreak().noOcclusion()
        );
    }

    /** 果树树叶：四个生命周期各一张贴图，结果状态下右键可取果。 */
    public static Block createLeaves(TFCFWood wood)
    {
        final ExtendedProperties props = ExtendedProperties.of()
            .mapColor(FruitTreeLeavesBlock::getMapColor)
            .strength(0.5F)
            .sound(SoundType.GRASS)
            .randomTicks()
            .noOcclusion()
            // 必须是本模组自己的类型：BlockEntityType 的 validBlocks 固定，
            // 借用 TFC 的 tfc:berry_bush 会让 LevelChunk 判定 isValid 失败而跳过 ticker，
            // serverTick 不执行 → /addday 对果树完全无效。见 TFCFBerryBushBlockEntity。
            // 用 lambda 延迟解析，避免在 TFCFBlocks 静态初始化期间触发 TFCFBlockEntities
            // 的初始化环（FRUIT_TREE_LEAVES 在 BAMBOO_SAPLINGS/WEBBED_CHEST 之前声明）。
            .blockEntity(() -> TFCFBlockEntities.BERRY_BUSH.get())
            .serverTicks(BerryBushBlockEntity::serverTick)
            .flammableLikeLeaves();

        // 桑叶要额外承载「蚕蛾羽化」逻辑。那段逻辑原来挂在 wood/leaves/mulberry 上，
        // 而桑树已改为只有果树形态（wood/leaves/mulberry 不再注册），所以用专用子类接过来。
        // 见 TFCFMulberryLeavesBlock。
        if (wood == TFCFWood.MULBERRY)
        {
            return new TFCFMulberryLeavesBlock(props, wood.getProductItem(), wood.getStages(), TFCFClimateRanges.FRUIT_TREES.get(wood), wood.getFloweringLeavesColor());
        }

        return new FruitTreeLeavesBlock(
            props,
            wood.getProductItem(),
            wood.getStages(),
            TFCFClimateRanges.FRUIT_TREES.get(wood),
            wood.getFloweringLeavesColor()
        );
    }

    /** 长成的果树枝干。 */
    public static Block createBranch(TFCFWood wood)
    {
        return new FruitTreeBranchBlock(
            ExtendedProperties.of(MapColor.WOOD)
                .sound(SoundType.SCAFFOLDING)
                .randomTicks()
                .strength(1.0F)
                .pushReaction(PushReaction.DESTROY)
                .flammableLikeLogs(),
            TFCFClimateRanges.FRUIT_TREES.get(wood)
        );
    }

    /** 生长中的果树枝干：按气候与时间把自己铺成一棵树。 */
    public static Block createGrowingBranch(TFCFWood wood)
    {
        return new GrowingFruitTreeBranchBlock(
            ExtendedProperties.of(MapColor.WOOD)
                .sound(SoundType.SCAFFOLDING)
                .randomTicks()
                .strength(1.0F)
                .pushReaction(PushReaction.DESTROY)
                .blockEntity(TFCBlockEntities.TICK_COUNTER)
                .flammableLikeLogs(),
            TFCFBlocks.FRUIT_TREE_BRANCHES.get(wood),
            TFCFBlocks.FRUIT_TREE_LEAVES.get(wood),
            TFCFClimateRanges.FRUIT_TREES.get(wood)
        );
    }
}
