package neresources.nei;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import neresources.config.Settings;
import neresources.reference.Resources;
import neresources.api.utils.PlantDrop;
import neresources.entries.PlantEntry;
import neresources.registry.PlantRegistry;
import neresources.utils.Font;
import neresources.utils.TranslationHelper;
import net.minecraft.item.ItemStack;

import java.awt.Rectangle;
import java.util.List;

public class NEIAdvSeedHandler extends TemplateRecipeHandler
{
    private static final int Y = 16;

    private static int CYCLE_TIME = (int) (20 * Settings.CYCLE_TIME);

    public static void reloadSettings()
    {
        CYCLE_TIME = (int) (20 * Settings.CYCLE_TIME);
    }

    @Override
    public String getGuiTexture()
    {
        return Resources.Gui.Nei.ADV_PLANT.toString();
    }

    @Override
    public String getRecipeName()
    {
        return TranslationHelper.translateToLocal("ner.advPlant.title");
    }

    @Override
    public void loadTransferRects()
    {
        transferRects.add(new TemplateRecipeHandler.RecipeTransferRect(new Rectangle(60, 15, 28, 18), NEIConfig.PLANT, new Object()));
    }

    @Override
    public void loadUsageRecipes(String inputId, Object... ingredients)
    {

	if (ingredients.length == 0) return;

        if (ingredients[0] instanceof ItemStack)
        {
            ItemStack ingredient = (ItemStack) ingredients[0];
            if (PlantRegistry.getInstance().contains(ingredient))
                arecipes.add(new CachedPlant(PlantRegistry.getInstance().getEntry(ingredient)));
        } else super.loadUsageRecipes(inputId, ingredients);
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results)
    {
        if (outputId.equals(NEIConfig.PLANT))
        {
            for (PlantEntry entry : PlantRegistry.getInstance().getAllPlants())
                arecipes.add(new CachedPlant(entry));
        } else super.loadCraftingRecipes(outputId, results);
    }

    @Override
    public void loadCraftingRecipes(ItemStack result)
    {
        if (PlantRegistry.getInstance().containsDrop(result))
            arecipes.add(new CachedSeed(result));
    }

    @Override
    public void drawExtras(int recipe)
    {
        CachedAbstract cachedAbstract = (CachedAbstract) arecipes.get(recipe);

        float chance = cachedAbstract.getChance();
        String toPrint;
        if (Float.isNaN(chance))
        {
            int[] minMax = cachedAbstract.getMinMax();
            toPrint = minMax[0] + " - " + minMax[1];
        }
        else
            toPrint = String.format("%2.2f", chance * 100).replace(",", ".") + "%";

        Font.normal.print(toPrint, 56, Y + 20);

        cachedAbstract.cycleOutput(cycleticks);
    }

    public abstract class CachedAbstract extends TemplateRecipeHandler.CachedRecipe
    {
        protected int i, lastI;
        protected long cycleAt;

        public CachedAbstract(int size)
        {
            this.i = 0;
            this.lastI = size;
            this.cycleAt = -1;
        }

        public void cycleOutput(long tick)
        {
            if (cycleAt == -1) cycleAt = tick + CYCLE_TIME;

            if (cycleAt <= tick)
            {
                if (++i >= lastI) i = 0;
                cycleAt += CYCLE_TIME;
            }
        }

        public abstract float getChance();

        public abstract int[] getMinMax();
    }

    public class CachedSeed extends CachedAbstract
    {
        private List<PlantEntry> entrys;
        private ItemStack seed;


        public CachedSeed(ItemStack seed)
        {
            super(PlantRegistry.getInstance().getEntriesForDrop(seed).size());
            this.seed = seed;
            this.entrys = PlantRegistry.getInstance().getEntriesForDrop(seed);
        }

        @Override
        public PositionedStack getResult()
        {
            return new PositionedStack(entrys.get(i).getPlant(), 34, Y);
        }

        @Override
        public PositionedStack getOtherStack()
        {
            return new PositionedStack(seed, 94, Y);
        }

        @Override
        public float getChance()
        {
            PlantEntry entry = entrys.get(i);
            PlantDrop drop = entry.getDrop(seed);
            switch (drop.getDropKind())
            {
                case chance:
                    return drop.getChance();
                case weight:
                    return  (float)drop.getWeight() / entry.getTotalWeight();
                case minMax:
                    return Float.NaN;
                default:
                    return 0;
            }
        }

        @Override
        public int[] getMinMax()
        {
            PlantEntry entry = entrys.get(i);
            PlantDrop drop = entry.getDrop(seed);
            return new int[] {drop.getMinDrop(), drop.getMaxDrop()};
        }
    }

    public class CachedPlant extends CachedAbstract
    {
        private PlantEntry entry;

        public CachedPlant(PlantEntry entry)
        {
            super(entry.getDrops().size());
            this.entry = entry;
        }

        @Override
        public PositionedStack getResult()
        {
            return new PositionedStack(entry.getPlant(), 34, Y);
        }

        @Override
        public PositionedStack getOtherStack()
        {
            return new PositionedStack(entry.getDrops().get(i).getDrop(), 94, Y);
        }

        @Override
        public float getChance()
        {
            PlantDrop drop = entry.getDrops().get(i);
            switch (drop.getDropKind())
            {
               case chance:
                   return drop.getChance();
               case weight:
                   return  (float)drop.getWeight() / entry.getTotalWeight();
               case minMax:
                   return Float.NaN;
               default:
                   return 0;
            }
        }

        @Override
        public int[] getMinMax()
        {
            PlantDrop drop = entry.getDrops().get(i);
            return new int[] {drop.getMinDrop(), drop.getMaxDrop()};
        }
    }
}
