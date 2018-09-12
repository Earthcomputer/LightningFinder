package net.earthcomputer.lightningtool;

import net.earthcomputer.lightningtool.FortuneManipulator.Ore;
import net.earthcomputer.lightningtool.SearchResult.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JCheckBox;

public class FortuneMultiManipulator extends AbstractManipulator {

    public static final RNGAdvancer<?>[] ADVANCERS = { RNGAdvancer.HOPPER };
    private static final Property<Integer> ORES = Property.create("ores", 4, 1, Property.maximize());
    private static final Property<Integer> REDSTONE = Property.create("redstone", 8, 4, Property.maximize());
    private static final Property<Integer> LAPIZ = Property.create("lapiz", 32, 4, Property.maximize());
    private static final Property<Integer> SAPLINGS = Property.create("saplings", 1, 0, Property.maximize());
    private static final Property<Integer> NETHERWART = Property.create("nehterwart", 7, 2, Property.maximize());
    private static final Property<Integer> POTATO = Property.create("potato", 1, 0, Property.maximize());

    private Ore ore;

    private boolean ores;
    private boolean redstone;
    private boolean lapiz;
    private boolean saplings;
    private boolean nehterwart;
    private boolean potato;

    private int fortuneLevel = 3;

    @Override
    protected boolean parseExtra() {
        ores = frame.getMultiFortune1Ores().isSelected();
        redstone = frame.getMultiFortune2Redstone().isSelected();
        lapiz = frame.getMultiFortune3Lapiz().isSelected();
        saplings = frame.getMultiFortune4Sapling().isSelected();
        nehterwart = frame.getMultiFortune5Netherwart().isSelected();
        potato = frame.getMultiFortune6Potato().isSelected();

        return true;
    }

    @Override
    public SearchResult testRegion(int x, int z) {
        SearchResult result = createSearchResult();
        if (ores) {
            rand.saveState();
            int quanityDropped = quantityDropped(fortuneLevel, rand, ORES);
            result = result.withProperty(ORES, quanityDropped);
            rand.restoreState();
        }
        if (redstone) {
            rand.saveState();
            int quanityDropped = quantityDropped(fortuneLevel, rand, REDSTONE);
            result = result.withProperty(REDSTONE, quanityDropped);
            rand.restoreState();
        }
        if (lapiz) {
            rand.saveState();
            int quanityDropped = quantityDropped(fortuneLevel, rand, LAPIZ);
            result = result.withProperty(LAPIZ, quanityDropped);
            rand.restoreState();
        }
        if (saplings) {
            rand.saveState();
            int quanityDropped = quantityDropped(fortuneLevel, rand, SAPLINGS);
            result = result.withProperty(SAPLINGS, quanityDropped);
            rand.restoreState();
        }
        if (nehterwart) {
            rand.saveState();
            int quanityDropped = quantityDropped(fortuneLevel, rand, NETHERWART);
            result = result.withProperty(NETHERWART, quanityDropped);
            rand.restoreState();
        }
        if (potato) {
            rand.saveState();
            int quanityDropped = quantityDropped(fortuneLevel, rand, POTATO);
            result = result.withProperty(POTATO, quanityDropped);
            rand.restoreState();
        }
        return result;
    }

    @Override
    protected SearchResult createSearchResult() {
        List<Property<?>> properties = new ArrayList<>();
        if (ores)
            properties.add(ORES);
        if (redstone)
            properties.add(REDSTONE);
        if (lapiz)
            properties.add(LAPIZ);
        if (saplings)
            properties.add(SAPLINGS);
        if (nehterwart)
            properties.add(NETHERWART);
        if (potato)
            properties.add(POTATO);

        advancer.addExtraProperties(properties);
        return new SearchResult(properties);
    }

    public int quantityDropped(int fortune, Random rand, Property<Integer> type) {
        int fortuneBonus = 0;
        if (type == SAPLINGS) { // saplings
            return rand.nextInt(20) == 0 ? 1 : 0;
        }
        if (type == POTATO) { // potato
            return rand.nextInt(50) == 0 ? 1 : 0;
        }
        if (type == NETHERWART) { // netherwart
            int result = 2 + rand.nextInt(3);

            if (fortune > 0) {
                result =+ rand.nextInt(fortune + 1);
            }
            return result;
        }
        if (type == REDSTONE) {
            return 4 + rand.nextInt(2) + rand.nextInt(fortune + 1);
        }
        if (fortune == 0) {
            fortuneBonus = 0;
        } else {
            fortuneBonus = rand.nextInt(fortune + 2) - 1;
            if (fortuneBonus < 0)
                fortuneBonus = 0;
        }
        if (type == LAPIZ) {
            return (4 + rand.nextInt(5)) * (fortuneBonus + 1);
        } else {
            return 1 + fortuneBonus;
        }
    }
}
