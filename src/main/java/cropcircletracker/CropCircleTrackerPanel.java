package cropcircletracker;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.ComboBoxListRenderer;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class CropCircleTrackerPanel extends PluginPanel
{
    private GridBagConstraints constraints(int gridx, int gridy)
    {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = gridx;
        c.gridy = gridy;
        c.weightx = 1;
        c.weighty = 0;
        c.gridwidth = 1;
        return c;
    }

    private void addLocationLabel()
    {
        JLabel label = new JLabel();
        label.setText("Location:");
        add(label, constraints(0, 0));
    }

    private void addLocationComboBox()
    {
        JComboBox<String> box = new JComboBox<>();
        box.setRenderer(new ComboBoxListRenderer<>());
        box.setFocusable(false);
        box.setForeground(Color.WHITE);
        box.setMaximumRowCount(CropCircle.values().length);
        ArrayList<String> names = new ArrayList<>();
        for (CropCircle cropCircle: CropCircle.values())
        {
            names.add(cropCircle.getName());
        }
        Collections.sort(names);
        for (String name: names)
        {
            box.addItem(name);
        }
        add(box, constraints(1, 0));
    }

    @Inject
    public CropCircleTrackerPanel()
    {
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new GridBagLayout());
        addLocationLabel();
        addLocationComboBox();
    }
}
