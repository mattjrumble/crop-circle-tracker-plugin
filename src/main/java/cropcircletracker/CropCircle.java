package cropcircletracker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.coords.WorldPoint;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public enum CropCircle
{
    DORICS_HOUSE(1, "Doric's House", new WorldPoint(2953, 3444, 0)),
    YANILLE(2, "Yanille", new WorldPoint(2582, 3104, 0)),
    DRAYNOR(3, "Draynor", new WorldPoint(3115, 3273, 0)),
    RIMMINGTON(4, "Rimmington", new WorldPoint(2978, 3216, 0)),
    GRAND_EXCHANGE(5, "Grand Exchange", new WorldPoint(3141, 3461, 0)),
    FARMING_GUILD(6, "Farming Guild", new WorldPoint(1302, 3711, 0)),
    HOSIDIUS(7, "Hosidius", new WorldPoint(1738, 3533, 0)),
    HARMONY_ISLAND(8, "Harmony Island", new WorldPoint(3810, 2852, 0)),
    GWENITH(9, "Gwenith", new WorldPoint(2195, 3402, 0)),
    CATHERBY(10, "Catherby", new WorldPoint(2819, 3470, 0)),
    TREE_GNOME_STRONGHOLD(11, "Tree Gnome Stronghold", new WorldPoint(2435, 3472, 0)),
    BRIMHAVEN(12, "Brimhaven", new WorldPoint(2808, 3200, 0)),
    MOS_LE_HARMLESS(13, "Mos Le'Harmless", new WorldPoint(3703, 2975, 0)),
    TAVERLEY(14, "Taverley", new WorldPoint(2896, 3406, 0)),
    LUMBRIDGE_MILL(15, "Lumbridge Mill", new WorldPoint(3160, 3299, 0)),
    EAST_ARDOUGNE(16, "East Ardougne", new WorldPoint(2647, 3348, 0)),
    SOUTH_OF_VARROCK(17, "South of Varrock", new WorldPoint(3212, 3345, 0)),
    MISCELLANIA(18, "Miscellania", new WorldPoint(2538, 3845, 0));

    @Getter
    private final int externalId;

    @Getter
    private final String name;

    @Getter
    private final WorldPoint worldPoint;

    public static Map<WorldPoint, CropCircle> mapping()
    {
        Map<WorldPoint, CropCircle> mapping = new HashMap<>();
        for (CropCircle cropCircle: CropCircle.values()) {
            mapping.put(cropCircle.getWorldPoint(), cropCircle);
        }
        return mapping;
    }
}
