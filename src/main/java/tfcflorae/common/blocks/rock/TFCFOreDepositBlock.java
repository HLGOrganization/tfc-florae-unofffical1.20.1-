package tfcflorae.common.blocks.rock;

import net.minecraft.world.level.block.Block;

import net.dries007.tfc.common.blocks.OreDeposit;

/**
 * Ore deposit block, carrying the host rock and deposit kind.
 * <p>
 * 1.18.2 also implemented {@code ItemPropertyProviderBlock} to expose those on the dropped item;
 * TFC 3.x removed that interface, so the block only stores them for blockstate purposes.
 */
public class TFCFOreDepositBlock extends Block
{
    private final TFCFRock rock;
    private final OreDeposit deposit;

    public TFCFOreDepositBlock(Properties properties, TFCFRock rock, OreDeposit deposit)
    {
        super(properties);
        this.rock = rock;
        this.deposit = deposit;
    }

    public TFCFRock getRock()
    {
        return rock;
    }

    public OreDeposit getDeposit()
    {
        return deposit;
    }
}
