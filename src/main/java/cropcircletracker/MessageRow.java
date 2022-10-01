package cropcircletracker;

import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;

/*
A component for displaying a message in the panel table (e.g. "Loading..." or an error message).
*/
class MessageRow extends JPanel
{
    MessageRow(String message, Color backgroundColor)
    {
        super();
        setLayout(new GridBagLayout());
        addLabel(message);
        if (backgroundColor != null)
        {
            setBackground(backgroundColor);
        }
    }

    private void addLabel(String message)
    {
        JLabel label = new JLabel(message);
        label.setFont(FontManager.getRunescapeFont());
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label, constraints(1, GridBagConstraints.BOTH));
    }

    /* Convenience method for returning a GridBagConstraints object with some common values. */
    private GridBagConstraints constraints(int weightX, int fill)
    {
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = weightX;
        c.fill = fill;
        return c;
    }
}
