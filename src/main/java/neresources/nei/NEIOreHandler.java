package neresources.nei;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;
import neresources.api.utils.ColorHelper;
import neresources.api.utils.conditionals.Conditional;
import neresources.config.Settings;
import neresources.reference.Resources;
import neresources.registry.OreRegistry;
import neresources.entries.OreMatchEntry;
import neresources.utils.Font;
import neresources.utils.RenderHelper;
import neresources.utils.TranslationHelper;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.List;

public class NEIOreHandler extends TemplateRecipeHandler
{
    private static final int X_OFFSPRING = 59;
    private static final int Y_OFFSPRING = 52;
    private static final int X_AXIS_SIZE = 90;
    private static final int Y_AXIS_SIZE = 40;
    private static final int X_ITEM = 8;
    private static final int Y_ITEM = 6;

    private static int CYCLE_TIME = (int) (20 * Settings.CYCLE_TIME);

    public static void reloadSettings()
    {
        CYCLE_TIME = (int) (20 * Settings.CYCLE_TIME);
    }

    @Override
    public String getGuiTexture()
    {
        return Resources.Gui.Nei.ORE.toString();
    }

    @Override
    public String getRecipeName()
    {
        return TranslationHelper.translateToLocal("ner.ore.title");
    }

    @Override
    public int recipiesPerPage()
    {
        return 2;
    }

    @Override
    public void loadTransferRects()
    {
        transferRects.add(new TemplateRecipeHandler.RecipeTransferRect(new Rectangle(60, -12, 45, 10), NEIConfig.ORE, new Object()));
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results)
    {
        if (outputId.equals(NEIConfig.ORE))
        {
            for (OreMatchEntry entry : OreRegistry.getOres())
                arecipes.add(new CachedOre(entry));
        } else super.loadCraftingRecipes(outputId, results);
    }

    @Override
    public void loadCraftingRecipes(ItemStack result)
    {
        for (OreMatchEntry entry : OreRegistry.getRegistryMatches(result))
            if (entry != null) arecipes.add(new CachedOre(entry));
    }

    @Override
    public void drawExtras(int recipe)
    {
        RenderHelper.drawArrow(X_OFFSPRING, Y_OFFSPRING, X_OFFSPRING + X_AXIS_SIZE, Y_OFFSPRING, ColorHelper.GRAY);
        RenderHelper.drawArrow(X_OFFSPRING, Y_OFFSPRING, X_OFFSPRING, Y_OFFSPRING - Y_AXIS_SIZE, ColorHelper.GRAY);
        CachedOre cachedOre = (CachedOre) arecipes.get(recipe);
        float[] array = cachedOre.oreMatchEntry.getChances();
        double max = 0;
        for (double d : array)
            if (d > max) max = d;
        double xPrev = X_OFFSPRING;
        double yPrev = Y_OFFSPRING;
        double space = X_AXIS_SIZE / ((array.length - 1) * 1D);
        for (int i = 0; i < array.length; i++)
        {
            double value = array[i];
            double y = Y_OFFSPRING - ((value / max) * Y_AXIS_SIZE);
            if (i > 0) // Only draw a line after the first element (cannot draw line with only one point)
            {
                double x = xPrev + space;
                RenderHelper.drawLine(xPrev, yPrev, x, y, cachedOre.getLineColor());
                xPrev = x;
            }
            yPrev = y;
        }

        Font font = new Font(true);
        font.print("0%", X_OFFSPRING - 10, Y_OFFSPRING - 7);
        font.print(String.format("%.2f", max * 100) + "%", X_OFFSPRING - 20, Y_OFFSPRING - Y_AXIS_SIZE);
        int minY = cachedOre.oreMatchEntry.getMinY() - Settings.EXTRA_RANGE;
        font.print(minY < 0 ? 0 : minY, X_OFFSPRING - 3, Y_OFFSPRING + 2);
        int maxY = cachedOre.oreMatchEntry.getMaxY() + Settings.EXTRA_RANGE;
        font.print(maxY > 255 ? 255 : maxY, X_OFFSPRING + X_AXIS_SIZE, Y_OFFSPRING + 2);
        font.print(TranslationHelper.translateToLocal("ner.ore.bestY") + ": " + cachedOre.oreMatchEntry.getBestY(), X_ITEM - 2, Y_ITEM + 20);

        cachedOre.cycleItemStack(cycleticks);
    }

    @Override
    public List<String> handleTooltip(GuiRecipe gui, List<String> currenttip, int recipe)
    {
        currenttip = super.handleTooltip(gui, currenttip, recipe);

        if (GuiContainerManager.shouldShowTooltip(gui) && currenttip.size() == 0)
        {
            Point offset = gui.getRecipePosition(recipe);
            Point pos = GuiDraw.getMousePosition();
            Point relMouse = new Point(pos.x - gui.guiLeft - offset.x, pos.y - gui.guiTop - offset.y);
            // Check if we are inside the coordinate system
            if (relMouse.x > X_OFFSPRING && relMouse.x < X_OFFSPRING + X_AXIS_SIZE &&
                relMouse.y > Y_OFFSPRING - Y_AXIS_SIZE && relMouse.y < Y_OFFSPRING)
            {
                CachedOre cachedOre = (CachedOre) arecipes.get(recipe);
                float[] chances = cachedOre.oreMatchEntry.getChances();
                double space = X_AXIS_SIZE / (chances.length * 1D);
                // Calculate the hovered over y value
                int index = (int) ((relMouse.x - X_OFFSPRING) / space);
                int yValue = Math.max(0, index + cachedOre.oreMatchEntry.getMinY() - Settings.EXTRA_RANGE + 1);
                if (index >= 0 && index < chances.length)
                    currenttip.add("Y: " + yValue + String.format(" (%.2f%%)", chances[index] * 100));
            }
        }
        return currenttip;
    }

    @Override
    public List<String> handleItemTooltip(GuiRecipe gui, ItemStack stack, List<String> currenttip, int recipe)
    {
        CachedOre cachedOre = (CachedOre) arecipes.get(recipe);
        if (stack != null && cachedOre.contains(stack))
        {
            if (cachedOre.oreMatchEntry.isSilkTouchNeeded(stack))
                currenttip.add(Conditional.silkTouch.toString());
            if (gui.isMouseOver(cachedOre.getResult(), recipe))
                currenttip.addAll(cachedOre.getRestrictions());
        }
        return currenttip;
    }

    public class CachedOre extends TemplateRecipeHandler.CachedRecipe
    {
        private OreMatchEntry oreMatchEntry;
        private List<ItemStack> oresAndDrops;
        private int current, last;
        private long cycleAt;

        public CachedOre(OreMatchEntry oreMatchEntry)
        {
            this.oreMatchEntry = oreMatchEntry;
            this.oresAndDrops = oreMatchEntry.getOresAndDrops();
            this.current = 0;
            this.last = this.oresAndDrops.size() - 1;
            this.cycleAt = -1;
        }

        @Override
        public PositionedStack getResult()
        {
            return new PositionedStack(this.oresAndDrops.get(current), X_ITEM, Y_ITEM);
        }

        public int getLineColor()
        {
            return this.oreMatchEntry.getColour();
        }

        public List<String> getRestrictions()
        {
            return this.oreMatchEntry.getRestrictions();
        }

        public boolean contains(ItemStack itemStack)
        {
            for (ItemStack listStack : this.oresAndDrops)
                if (listStack.isItemEqual(itemStack)) return true;
            return false;
        }

        public void cycleItemStack(long tick)
        {
            if (cycleAt == -1) cycleAt = tick + CYCLE_TIME;

            if (tick >= cycleAt)
            {
                if (++current > last) current = 0;
                cycleAt += CYCLE_TIME;
            }
        }
    }
}