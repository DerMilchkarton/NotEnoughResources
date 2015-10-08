package neresources.compatibility.cofh;

import cofh.api.world.IFeatureGenerator;
import cofh.core.world.WorldHandler;
import cofh.lib.util.WeightedRandomBlock;
import cofh.lib.world.*;
import cofh.lib.world.feature.FeatureBase;
import cofh.lib.world.feature.FeatureBase.GenRestriction;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import neresources.api.distributions.DistributionCustom;
import neresources.api.messages.RegisterOreMessage;
import neresources.api.utils.DistributionHelpers;
import neresources.compatibility.CompatBase;
import neresources.utils.LoaderHelper;
import neresources.utils.ModList;
import neresources.utils.ReflectionHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.ArrayList;
import java.util.List;

public class CoFHCompat extends CompatBase
{

    private static List<IFeatureGenerator> features;
    public static boolean cofhReplace = false;

    private static Class featureGenUniform;
    private static Class featureGenNormal;
    private static Class featureGenSurface;
    private static Class featureGenLargeVein;
    private static Class featureGenTopBlock;
    private static Class featureGenUnderFluid;
    public static boolean isVersionB6;


    public CoFHCompat()
    {
        if (Loader.isModLoaded(ModList.Names.COFHCORE))
        {
            if (LoaderHelper.isModVersion(ModList.Names.COFHCORE, "1.7.10R3.0.0B6"))
            {
                featureGenUniform = ReflectionHelper.findClass("cofh.lib.world.feature.FeatureOreGenUniform");
                featureGenNormal = ReflectionHelper.findClass("cofh.lib.world.feature.FeatureOreGenNormal");
                featureGenSurface = ReflectionHelper.findClass("cofh.lib.world.feature.FeatureOreGenSurface");
                isVersionB6 = true;
            } else
            {
                featureGenUniform = ReflectionHelper.findClass("cofh.lib.world.feature.FeatureGenUniform");
                featureGenNormal = ReflectionHelper.findClass("cofh.lib.world.feature.FeatureGenNormal");
                featureGenSurface = ReflectionHelper.findClass("cofh.lib.world.feature.FeatureGenSurface");
                featureGenLargeVein = ReflectionHelper.findClass("cofh.lib.world.feature.FeatureGenLargeVein");
                featureGenTopBlock = ReflectionHelper.findClass("cofh.lib.world.feature.FeatureGenTopBlock");
                featureGenUnderFluid = ReflectionHelper.findClass("cofh.lib.world.feature.FeatureGenUnderfluid");
                isVersionB6 = false;
            }
            Class worldHandler = ReflectionHelper.findClass("cofh.core.world.WorldHandler");
            if (worldHandler != null)
                cofhReplace = ReflectionHelper.getBoolean(worldHandler, "genReplaceVanilla", null);
        }
    }

    @Override
    public void init()
    {
        cofhReplace = WorldHandler.genReplaceVanilla;
        features = (ArrayList<IFeatureGenerator>) ReflectionHelper.getObject(WorldHandler.class, "features", null);
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) registerOres();
    }

    private void registerOres()
    {
        for (IFeatureGenerator feature : features)
        {
            GenRestriction dimensionRestriction;
            GenRestriction biomeRestriction;
            if (feature instanceof FeatureBase)
            {
                dimensionRestriction = (GenRestriction) ReflectionHelper.getObject(FeatureBase.class, "dimensionRestriction", feature);
                biomeRestriction = (GenRestriction) ReflectionHelper.getObject(FeatureBase.class, "biomeRestriction", feature);
            }
            if (feature.getClass() == featureGenUniform)
            {
                int maxY = ReflectionHelper.getInt(featureGenUniform, "maxY", feature);
                int minY = ReflectionHelper.getInt(featureGenUniform, "minY", feature);
                int count = ReflectionHelper.getInt(featureGenUniform, "count", feature);
                WorldGenerator worldGen = (WorldGenerator) ReflectionHelper.getObject(featureGenUniform, "worldGen", feature);
                CoFHWorldGen oreGen = getCoFHWorldGen(worldGen);

                if (oreGen.ores != null)
                    registerOreEntries(oreGen.ores, getChancesForUniform(minY, maxY, oreGen.veinSize, count));
            } else if (feature.getClass() == featureGenNormal)
            {
                int maxVar = ReflectionHelper.getInt(featureGenNormal, "maxVar", feature);
                int meanY = ReflectionHelper.getInt(featureGenNormal, "meanY", feature);
                int count = ReflectionHelper.getInt(featureGenNormal, "count", feature);
                WorldGenerator worldGen = (WorldGenerator) ReflectionHelper.getObject(featureGenNormal, "worldGen", feature);
                CoFHWorldGen oreGen = getCoFHWorldGen(worldGen);

                if (oreGen.ores != null)
                    registerOreEntries(oreGen.ores, getChancesForNormal(meanY, maxVar, oreGen.veinSize, count));
            } else if (feature.getClass() == featureGenSurface)
            {
                int count = ReflectionHelper.getInt(featureGenSurface, "count", feature);
                WorldGenerator worldGen = (WorldGenerator) ReflectionHelper.getObject(featureGenSurface, "worldGen", feature);
                CoFHWorldGen oreGen = getCoFHWorldGen(worldGen);

                if (oreGen.ores != null)
                {
                    int diameter = (int) Math.pow(oreGen.veinSize, 0.333333D);
                    registerOreEntries(oreGen.ores, DistributionHelpers.multiplyArray(DistributionHelpers.getOverworldSurfaceDistribution(diameter), count));
                }
            } else if (feature.getClass() == featureGenLargeVein)
            {
                int count = ReflectionHelper.getInt(featureGenLargeVein, "count", feature);
                int minY = ReflectionHelper.getInt(featureGenLargeVein, "minY", feature);
                int veinHeight = ReflectionHelper.getInt(featureGenLargeVein, "veinHeight", feature);

                float[] verticalDistribution = DistributionHelpers.getRoundedSquareDistribution((int) (minY + 0.125 * veinHeight), (int) (minY + 0.25 * veinHeight), (int) (minY + 0.75 * veinHeight), (int) (minY + 0.875 * veinHeight), 1F);
                verticalDistribution = DistributionHelpers.divideArray(verticalDistribution, DistributionHelpers.sum(verticalDistribution));
                float[] oreDistribution = new float[256];
                for (int i = 0; i < oreDistribution.length; i++)
                {
                    float midY = verticalDistribution[i] / (veinHeight / 2);
                    if (midY == 0) continue;
                    DistributionHelpers.addDistribution(oreDistribution, DistributionHelpers.getTriangularDistribution(i, veinHeight / 2, midY));
                }
                WorldGenerator worldGen = (WorldGenerator) ReflectionHelper.getObject(featureGenLargeVein, "worldGen", feature);
                CoFHWorldGen oreGen = getCoFHWorldGen(worldGen);

                if (oreGen.ores != null)
                    registerOreEntries(oreGen.ores, DistributionHelpers.multiplyArray(oreDistribution, (float) count * oreGen.veinSize / 256F));
            } else if (feature.getClass() == featureGenTopBlock)
            {
                int count = ReflectionHelper.getInt(featureGenTopBlock, "count", feature);
                WorldGenerator worldGen = (WorldGenerator) ReflectionHelper.getObject(featureGenTopBlock, "worldGen", feature);
                CoFHWorldGen oreGen = getCoFHWorldGen(worldGen);

                if (oreGen.ores != null)
                {
                    registerOreEntries(oreGen.ores, DistributionHelpers.multiplyArray(DistributionHelpers.getOverworldSurface(), count * oreGen.veinSize));
                }
            } else if (feature.getClass() == featureGenUnderFluid)
            {
                boolean water = ReflectionHelper.getBoolean(featureGenUnderFluid, "water", feature);
                if (!water) continue; //TODO: Not sure how to handle non water stuff
                int count = ReflectionHelper.getInt(featureGenUnderFluid, "count", feature);
                WorldGenerator worldGen = (WorldGenerator) ReflectionHelper.getObject(featureGenUnderFluid, "worldGen", feature);
                CoFHWorldGen oreGen = getCoFHWorldGen(worldGen);

                if (oreGen.ores != null)
                    registerOreEntries(oreGen.ores, getChancesForUnderwater(oreGen.veinSize, count));
            }
        }
    }

    private float[] getChancesForUnderwater(int veinSize, int numVeins)
    {
        float chance = 0.6F * ((float) veinSize * numVeins) / 256;
        return DistributionHelpers.getUnderwaterDistribution(chance);
    }

    private float[] getChancesForUniform(int minY, int maxY, int veinSize, int numVeins)
    {
        int safeMinY = Math.max(minY, 0);
        int safeMaxY = Math.min(maxY, 255);
        float chance = (float) numVeins / (safeMaxY - safeMinY + 1) * veinSize / 256F;
        return DistributionHelpers.getRoundedSquareDistribution(Math.max(0, minY - veinSize / 2), safeMinY, safeMaxY, Math.min(maxY + veinSize / 2, 255), chance);
    }

    private float[] getChancesForNormal(int meanY, int maxVar, int veinSize, int numVeins)
    {
        float[] normalDistribution = DistributionHelpers.getTriangularDistribution(meanY, maxVar + veinSize / 2, 1F);
        float total = DistributionHelpers.sum(normalDistribution);
        float chance = (float) numVeins / total * veinSize / 256F;
        return DistributionHelpers.multiplyArray(normalDistribution, chance);
    }

    private void registerOreEntries(List<WeightedRandomBlock> ores, float[] baseChance)
    {
        float totalWeight = 0;
        for (WeightedRandomBlock ore : ores)
            totalWeight += ore.itemWeight;
        for (WeightedRandomBlock ore : ores)
        {
            if (ore.block == Blocks.gravel || ore.block == Blocks.dirt) return;
            registerOre(new RegisterOreMessage(new ItemStack(ore.block, 1, ore.metadata), new DistributionCustom(DistributionHelpers.multiplyArray(baseChance, (float) ore.itemWeight / totalWeight))));
        }
    }

    public CoFHWorldGen getCoFHWorldGen(WorldGenerator worldGen)
    {
        CoFHWorldGen oreGen = new CoFHWorldGen();
        if (worldGen instanceof WorldGenMinableCluster) oreGen = new CoFHWorldGen((WorldGenMinableCluster) worldGen);
        else if (worldGen instanceof WorldGenSparseMinableCluster)
            oreGen = new CoFHWorldGen((WorldGenSparseMinableCluster) worldGen);
        else if (!isVersionB6 && worldGen instanceof WorldGenMinableLargeVein)
            oreGen = new CoFHWorldGen((WorldGenMinableLargeVein) worldGen);
        else if (!isVersionB6 && worldGen instanceof WorldGenGeode) oreGen = new CoFHWorldGen((WorldGenGeode) worldGen);
        else if (!isVersionB6 && worldGen instanceof WorldGenDecoration)
            oreGen = new CoFHWorldGen((WorldGenDecoration) worldGen);
        else if (!isVersionB6 && worldGen instanceof WorldGenBoulder)
            oreGen = new CoFHWorldGen((WorldGenBoulder) worldGen);
        return oreGen;
    }


    private class CoFHWorldGen
    {
        int veinSize;
        ArrayList<WeightedRandomBlock> ores;
        WeightedRandomBlock[] genBlock;

        public CoFHWorldGen()
        {
        }

        public CoFHWorldGen(WorldGenMinableCluster worldGen)
        {
            ores = (ArrayList<WeightedRandomBlock>) ReflectionHelper.getObject(WorldGenMinableCluster.class, "cluster", worldGen);
            veinSize = ReflectionHelper.getInt(WorldGenMinableCluster.class, "genClusterSize", worldGen);
            genBlock = (WeightedRandomBlock[]) ReflectionHelper.getObject(WorldGenMinableCluster.class, "genBlock", worldGen);
        }

        public CoFHWorldGen(WorldGenMinableLargeVein worldGen)
        {
            ores = (ArrayList<WeightedRandomBlock>) ReflectionHelper.getObject(WorldGenMinableLargeVein.class, "cluster", worldGen);
            veinSize = ReflectionHelper.getInt(WorldGenMinableLargeVein.class, "genVeinSize", worldGen);
            genBlock = (WeightedRandomBlock[]) ReflectionHelper.getObject(WorldGenMinableLargeVein.class, "genBlock", worldGen);
        }

        public CoFHWorldGen(WorldGenSparseMinableCluster worldGen)
        {
            ores = (ArrayList<WeightedRandomBlock>) ReflectionHelper.getObject(WorldGenSparseMinableCluster.class, "cluster", worldGen);
            veinSize = ReflectionHelper.getInt(WorldGenSparseMinableCluster.class, "genClusterSize", worldGen);
            genBlock = (WeightedRandomBlock[]) ReflectionHelper.getObject(WorldGenSparseMinableCluster.class, "genBlock", worldGen);
        }

        public CoFHWorldGen(WorldGenGeode worldGen)
        {

        }

        public CoFHWorldGen(WorldGenDecoration worldGen)
        {

        }

        public CoFHWorldGen(WorldGenBoulder worldGen)
        {
            ores = (ArrayList<WeightedRandomBlock>) ReflectionHelper.getObject(WorldGenBoulder.class, "cluster", worldGen);
            genBlock = (WeightedRandomBlock[]) ReflectionHelper.getObject(WorldGenBoulder.class, "genBlock", worldGen);
            int minSize = ReflectionHelper.getInt(WorldGenBoulder.class, "size", worldGen);
            int count = worldGen.clusters;
            int var = worldGen.sizeVariance;
            float radius = minSize + (var - 1) * 0.5F + 0.5F;
            veinSize = (int) (4D / 3 * DistributionHelpers.PI * radius * radius * radius) * count;
        }
    }
}
