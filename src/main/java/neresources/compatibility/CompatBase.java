package neresources.compatibility;

import neresources.api.messages.RegisterOreMessage;
import neresources.entries.MobEntry;
import neresources.registry.*;
import neresources.entries.OreEntry;
import neresources.utils.LogHelper;
import neresources.utils.ModList;

public class CompatBase
{
    public boolean load(ModList mod)
    {
        if (mod.isLoaded())
        {
            LogHelper.info("Loading compatibility for " + mod.toString());
            init();
            return true;
        } else
        {
            LogHelper.info(mod.toString() + " not loaded - skipping");
        }
        return false;
    }

    protected void init()
    {
    }

    public void registerMob(MobEntry entry)
    {
        MobRegistry.getInstance().registerMob(entry);
    }

    public void registerOre(RegisterOreMessage message)
    {
        NewOreRegistry.registerOre(message);
    }

    public void registerOre(OreEntry entry)
    {
        OreRegistry.getInstance().register(entry);
    }

    public void registerPlant(PlantEntry entry)
    {
        PlantRegistry.getInstance().registerPlant(entry);
    }
}
