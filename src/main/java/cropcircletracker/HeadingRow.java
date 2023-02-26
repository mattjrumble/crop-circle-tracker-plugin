package cropcircletracker;

import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;

/*
A component for displaying headings in the panel table.
*/
class HeadingRow extends JPanel
{
	private final static int COLUMN_1_WIDTH = 50;
	private final static int COLUMN_2_WIDTH = 50;
	private final static int COLUMN_3_WIDTH = 100;
	private final static int COLUMN_HEIGHT = 24;

	HeadingRow(String column1, String column2, String column3, Color backgroundColor)
	{
		super();
		setLayout(new GridBagLayout());
		addLabel(column1, COLUMN_1_WIDTH);
		addLabel(column2, COLUMN_2_WIDTH);
		addLabel(column3, COLUMN_3_WIDTH);
		if (backgroundColor != null)
		{
			setBackground(backgroundColor);
		}
	}

	private void addLabel(String text, int width)
	{
		JLabel label = new JLabel(text);
		label.setFont(FontManager.getRunescapeFont());
		label.setPreferredSize(new Dimension(width, COLUMN_HEIGHT));
		label.setMaximumSize(new Dimension(width, COLUMN_HEIGHT));
		label.setMinimumSize(new Dimension(width, COLUMN_HEIGHT));
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
