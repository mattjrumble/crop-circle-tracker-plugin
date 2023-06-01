package cropcircletracker;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.ComboBoxListRenderer;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.*;
import java.util.List;

@Slf4j
public class CropCircleTrackerPanel extends PluginPanel
{
    private static final Color BACKGROUND_COLOR = ColorScheme.DARK_GRAY_COLOR;

    private final CropCircleTrackerPlugin plugin;

    /* Keep track of whether the panel is open or not so that we don't redraw things unnecessarily. */
    public boolean open = false;

    /* A mapping of worlds to likelihoods. */
    public JsonObject likelihoods = null;

    private JComboBox<String> locationDropdownMenu;

    private Table table;

    @Inject
    public CropCircleTrackerPanel(CropCircleTrackerPlugin plugin)
    {
        this.plugin = plugin;
        setBackground(BACKGROUND_COLOR);
        setLayout(new GridBagLayout());
        addLocationLabel();
        addLocationDropdownMenu();
        addTable();
    }

    private void addLocationLabel()
    {
        JLabel label = new JLabel();
        label.setText("Location:");
        add(label, constraints(0, 0, 1));
    }

    private void addLocationDropdownMenu()
    {
        locationDropdownMenu = new JComboBox<>();
        locationDropdownMenu.setRenderer(new ComboBoxListRenderer<>());
        locationDropdownMenu.setFocusable(false);
        locationDropdownMenu.setForeground(Color.WHITE);
        locationDropdownMenu.setMaximumRowCount(CropCircle.values().length);
        ArrayList<String> names = new ArrayList<>();
        for (CropCircle cropCircle: CropCircle.values())
        {
            names.add(cropCircle.getName());
        }
        Collections.sort(names);
        for (String name: names)
        {
            locationDropdownMenu.addItem(name);
        }
        locationDropdownMenu.setSelectedItem(plugin.config.defaultLocation().getName());
        locationDropdownMenu.addItemListener(e ->
        {
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
                updateTable();
            }
        });
        add(locationDropdownMenu, constraints(1, 0, 1));
    }

    private void addTable()
    {
        table = new Table(this.plugin);
        GridBagConstraints constraints = constraints(0, 2, 2);
        constraints.insets = new Insets(10, 0, 0, 0);
        add(table, constraints);
    }

    /*
    Repopulate the table based on the given mapping of worlds to likelihoods, and the selected location from the
    dropdown menu.
    */
    public void updateTable()
    {
        if (this.likelihoods != null)
        {
            String selectedLocationName = String.valueOf(locationDropdownMenu.getSelectedItem());
            CropCircle cropCircle = CropCircle.fromName(selectedLocationName);
            if (cropCircle == null)
            {
                log.error("Invalid location selected");
                return;
            }
            int selectedLocation = cropCircle.getIndex();

            // Get worlds and likelihoods for the selected location.
            List<List<Object>> worldLikelihoodPairs = new ArrayList<>();
            this.likelihoods.keySet().forEach(world ->
            {
                JsonObject likelihoodsForWorld = this.likelihoods.get(world).getAsJsonObject();
                JsonElement likelihoodJsonElement = likelihoodsForWorld.get(String.valueOf(selectedLocation));
                if (likelihoodJsonElement != null)
                {
                    double likelihood = likelihoodJsonElement.getAsDouble();
                    List<Object> pair = new ArrayList<>();
                    pair.add(world);
                    pair.add(likelihood);
                    worldLikelihoodPairs.add(pair);
                }
            });

            // Repopulate table.
            table.update(worldLikelihoodPairs);
        }
    }

    public void clearTable()
    {
        likelihoods = null;
        table.clear();
    }

    public void displayError(String errorMessage)
    {
        likelihoods = null;
        table.displayError(errorMessage);
    }

    public void displayErrors(List<String> errorMessages)
    {
        likelihoods = null;
        table.displayErrors(errorMessages);
    }

    public void onActivate()
    {
        clearTable();
        open = true;
        plugin.getLikelihoods();
    }

    public void onDeactivate()
    {
        open = false;
    }

    /* Convenience method for returning a GridBagConstraints object with some common values. */
    private GridBagConstraints constraints(int gridx, int gridy, int gridwidth)
    {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = gridx;
        c.gridy = gridy;
        c.gridwidth = gridwidth;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        return c;
    }
}
