package cropcircletracker;

import net.runelite.client.ui.ColorScheme;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldType;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Table extends JPanel {
    private static final Color HEADING_COLOR = ColorScheme.SCROLL_TRACK_COLOR;
    private static final Color ROW_COLOR_1 = ColorScheme.DARK_GRAY_COLOR;
    private static final Color ROW_COLOR_2 = new Color(44, 44, 44);
    private static final Color LIKELIHOOD_COLOR_1 = new Color(0, 255, 0);
    private static final Color LIKELIHOOD_COLOR_2 = new Color(128, 255, 0);
    private static final Color LIKELIHOOD_COLOR_3 = new Color(255, 255, 0);
    private static final Color LIKELIHOOD_COLOR_4 = new Color(255, 128, 0);
    private static final Color LIKELIHOOD_COLOR_5 = new Color(255, 0, 0);
    private static final Color DANGEROUS_WORLD_TYPE_COLOR = new Color(255, 0, 0);
    private static final List<WorldType> worldTypesToDisplay = Arrays.asList(
            WorldType.SKILL_TOTAL,
            WorldType.HIGH_RISK,
            WorldType.PVP,
            WorldType.MEMBERS,
            WorldType.LAST_MAN_STANDING
    );
    private static final List<WorldType> dangerousWorldTypes = Arrays.asList(
            WorldType.HIGH_RISK,
            WorldType.PVP
    );

    private final CropCircleTrackerPlugin plugin;

    public Table(CropCircleTrackerPlugin plugin) {
        super();
        this.plugin = plugin;
        setLayout(new GridLayout(0, 1));
        addHeadings();
    }

    private void addHeadings()
    {
        add(new TableRow("World", "Likelihood", "World Type", null, null, null, HEADING_COLOR));
    }

    public void update(List<List<Object>> worldLikelihoodPairs)
    {
        removeAll();
        addHeadings();
        AtomicInteger rowIndex = new AtomicInteger();
        for (List<Object> pair: worldLikelihoodPairs)
        {
            String world = (String) pair.get(0);
            double likelihood = (double) pair.get(1);
            if (shouldDisplayWorld(Integer.parseInt(world)))
            {
                Color rowColor = rowIndex.get() % 2 == 0 ? ROW_COLOR_1 : ROW_COLOR_2;
                rowIndex.getAndIncrement();
                add(new TableRow(
                        world, getLikelihoodString(likelihood), getWorldTypeString(Integer.parseInt(world)),
                        null, getLikelihoodColor(likelihood), getWorldTypeColor(Integer.parseInt(world)), rowColor
                ));
            }
        };
        revalidate();
        repaint();
    }

    public void clear()
    {
        removeAll();
        revalidate();
        repaint();
    }

    /*
    Decide if we should display sightings for the given world. Don't bother displaying sightings for "weird" world
    types since these are unlikely to be useful for anyone.
    */
    private boolean shouldDisplayWorld(int worldID)
    {
        World world = plugin.worldMapping.get(worldID);
        for (WorldType worldType: world.getTypes())
        {
            // If the given world has any world types that are not considered "good" then don't display it. Even if
            // we don't know about the world type (e.g. if a new world type is added after this code has been written),
            // it's safer to not display worlds than it is to display worlds we know nothing about.
            if (!worldTypesToDisplay.contains(worldType))
            {
                return false;
            }
        }
        return true;
    }

    private String getLikelihoodString(double likelihood)
    {
        if (likelihood >= 0.945)
        {
            return "95%+";
        }
        else if (likelihood <= 0.01)
        {
            return "1%";
        }
        else
        {
            return Math.round(likelihood * 100) + "%";
        }
    }

    private String getWorldTypeString(int worldID)
    {
        World world = plugin.worldMapping.get(worldID);
        EnumSet<WorldType> worldTypes = world.getTypes();
        if (worldTypes.contains(WorldType.PVP) && worldTypes.contains(WorldType.HIGH_RISK))
        {
            return "PvP - High Risk";
        }
        else if (worldTypes.contains(WorldType.PVP))
        {
            return "PvP";
        }
        else if (worldTypes.contains(WorldType.HIGH_RISK))
        {
            return "High Risk";
        }
        else if (worldTypes.contains(WorldType.SKILL_TOTAL))
        {
            return world.getActivity();
        }
        else
        {
            return "-";
        }
    }

    private Color getLikelihoodColor(double likelihood)
    {
        if (likelihood >= 0.8)
        {
            return LIKELIHOOD_COLOR_1;
        }
        else if (likelihood >= 0.6)
        {
            return LIKELIHOOD_COLOR_2;
        }
        else if (likelihood >= 0.4)
        {
            return LIKELIHOOD_COLOR_3;
        }
        else if (likelihood >= 0.2)
        {
            return LIKELIHOOD_COLOR_4;
        }
        else
        {
            return LIKELIHOOD_COLOR_5;
        }
    }

    private Color getWorldTypeColor(int worldID)
    {
        World world = plugin.worldMapping.get(worldID);
        EnumSet<WorldType> worldTypes = world.getTypes();
        for (WorldType dangerousWorldType : dangerousWorldTypes)
        {
            if (worldTypes.contains(dangerousWorldType))
            {
                return DANGEROUS_WORLD_TYPE_COLOR;
            }
        }
        return null;
    }
}
