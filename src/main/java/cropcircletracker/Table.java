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
    private static final List<WorldType> worldTypesToDisplay = Arrays.asList(
            WorldType.BOUNTY,
            WorldType.HIGH_RISK,
            WorldType.LAST_MAN_STANDING,
            WorldType.MEMBERS,
            WorldType.PVP,
            WorldType.SKILL_TOTAL,
            WorldType.SEASONAL
    );

    private final CropCircleTrackerPlugin plugin;

    public Table(CropCircleTrackerPlugin plugin) {
        super();
        this.plugin = plugin;
        setLayout(new GridLayout(0, 1));
    }

    private void addHeadingRow()
    {
        add(new HeadingRow("World", "Likelihood", "World Type", HEADING_COLOR));
    }

    private void addMessageRow(String message)
    {
        add(new MessageRow(message, ROW_COLOR_1));
    }

    private void addEntryRow(int world, double likelihood, Color rowColor)
    {
        add(new EntryRow(world, likelihood, rowColor, this.plugin));
    }

    public void update(List<List<Object>> worldLikelihoodPairs)
    {
        removeAll();
        addHeadingRow();

        // Sort sightings by likelihood.
        Collections.sort(worldLikelihoodPairs, (a, b) -> {
            double likelihoodA = (double) a.get(1);
            double likelihoodB = (double) b.get(1);
            return likelihoodA < likelihoodB ? 1 : -1;
        });

        AtomicInteger rowIndex = new AtomicInteger();
        if (worldLikelihoodPairs.size() > 0)
        {
            for (List<Object> pair : worldLikelihoodPairs)
            {
                int world = Integer.parseInt((String) pair.get(0));
                double likelihood = (double) pair.get(1);
                if (shouldDisplay(world, likelihood))
                {
                    Color rowColor = rowIndex.get() % 2 == 0 ? ROW_COLOR_1 : ROW_COLOR_2;
                    rowIndex.getAndIncrement();
                    addEntryRow(world, likelihood, rowColor);
                }
            }
        }
        else
        {
            addMessageRow("No known crop circles");
        }
        revalidate();
        repaint();
    }

    public void clear()
    {
        removeAll();
        addMessageRow("Loading...");
        revalidate();
        repaint();
    }

    public void displayError(String errorMessage)
    {
        removeAll();
        addMessageRow(errorMessage);
        revalidate();
        repaint();
    }

    public void displayErrors(List<String> errorMessages)
    {
        removeAll();
        for (String errorMessage: errorMessages)
        {
            addMessageRow(errorMessage);
        }
        revalidate();
        repaint();
    }

    /* Decide if we should display a row for the given world and likelihood. */
    private boolean shouldDisplay(int worldID, double likelihood)
    {
        if (likelihood < plugin.config.minimumLikelihood() / 100d)
        {
            return false;
        }
        World world = plugin.worldMapping.get(worldID);
        if (world == null)
        {
            return false;
        }
        EnumSet<WorldType> worldTypes = world.getTypes();
        for (WorldType worldType: worldTypes)
        {
            // If the given world has any world types that are not considered "good" then don't display it. Even if
            // we don't know about the world type (e.g. if a new world type is added after this code has been written),
            // it's safer to not display worlds than it is to display worlds we know nothing about.
            if (!worldTypesToDisplay.contains(worldType))
            {
                return false;
            }
        }
        if (worldTypes.contains(WorldType.PVP) && !plugin.config.showPVPWorlds())
        {
            return false;
        }
        if (worldTypes.contains(WorldType.HIGH_RISK) && !plugin.config.showHighRiskWorlds())
        {
            return false;
        }
        if (worldTypes.contains(WorldType.SEASONAL) && !plugin.config.showSeasonalWorlds())
        {
            return false;
        }
        if (worldTypes.contains(WorldType.SKILL_TOTAL))
        {
            switch (world.getActivity()) {
                case "1250 skill total":
                    if (!plugin.config.show1250TotalWorlds()) {
                        return false;
                    }
                    break;
                case "1500 skill total":
                    if (!plugin.config.show1500TotalWorlds()) {
                        return false;
                    }
                    break;
                case "1750 skill total":
                    if (!plugin.config.show1750TotalWorlds()) {
                        return false;
                    }
                    break;
                case "2000 skill total":
                    if (!plugin.config.show2000TotalWorlds()) {
                        return false;
                    }
                    break;
                case "2200 skill total":
                    if (!plugin.config.show2200TotalWorlds()) {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }
}
