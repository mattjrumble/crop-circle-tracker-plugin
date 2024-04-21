package cropcircletracker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.coords.WorldPoint;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public enum CropCircle
{
    // I'd prefer having these ordered by index, but they're ordered alphabetically so that the defaultLocation config
    // dropdown is in alphabetical order. There's probably a better solution to this problem.
    BRIMHAVEN(11, "Brimhaven", new WorldPoint(2808, 3200, 0)),
    CATHERBY(9, "Catherby", new WorldPoint(2819, 3470, 0)),
    CIVITAS_ILLA_FORTIS(15, "Civitas illa Fortis", new WorldPoint(1673, 3049, 0)),
    DORICS_HOUSE(0, "Doric's House", new WorldPoint(2953, 3444, 0)),
    DRAYNOR(2, "Draynor", new WorldPoint(3115, 3273, 0)),
    EAST_ARDOUGNE(17, "East Ardougne", new WorldPoint(2647, 3348, 0)),
    FARMING_GUILD(5, "Farming Guild", new WorldPoint(1302, 3711, 0)),
    GRAND_EXCHANGE(4, "Grand Exchange", new WorldPoint(3141, 3461, 0)),
    GWENITH(8, "Gwenith", new WorldPoint(2195, 3402, 0)),
    HARMONY_ISLAND(7, "Harmony Island", new WorldPoint(3810, 2852, 0)),
    HOSIDIUS(6, "Hosidius", new WorldPoint(1738, 3533, 0)),
    KOUREND_CASTLE(14, "Kourend Castle", new WorldPoint(1660, 3637, 0)),
    LUMBRIDGE_MILL(16, "Lumbridge Mill", new WorldPoint(3160, 3299, 0)),
    MISCELLANIA(19, "Miscellania", new WorldPoint(2538, 3845, 0)),
    MOS_LE_HARMLESS(12, "Mos Le'Harmless", new WorldPoint(3703, 2975, 0)),
    RIMMINGTON(3, "Rimmington", new WorldPoint(2978, 3216, 0)),
    SOUTH_OF_VARROCK(18, "South of Varrock", new WorldPoint(3212, 3345, 0)),
    TAVERLEY(13, "Taverley", new WorldPoint(2896, 3406, 0)),
    TREE_GNOME_STRONGHOLD(10, "Tree Gnome Stronghold", new WorldPoint(2435, 3472, 0)),
    YANILLE(1, "Yanille", new WorldPoint(2582, 3104, 0));

    @Getter
    private final int index;

    @Getter
    private final String name;

    @Getter
    private final WorldPoint worldPoint;

    public static Map<WorldPoint, CropCircle> mapping()
    {
        Map<WorldPoint, CropCircle> mapping = new HashMap<>();
        for (CropCircle cropCircle: CropCircle.values())
        {
            mapping.put(cropCircle.getWorldPoint(), cropCircle);
        }
        return mapping;
    }

    public static CropCircle fromName(String name)
    {
        for (CropCircle cropCircle: CropCircle.values())
        {
            if (cropCircle.getName().equals(name))
            {
                return cropCircle;
            }
        }
        return null;
    }
}
