package cropcircletracker;

import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/*
A component for displaying an entry in the panel table (i.e. for displaying the world/likelihood/world type).
*/
class EntryRow extends JPanel
{
	private final static int COLUMN_1_WIDTH = 50;
	private final static int COLUMN_2_WIDTH = 50;
	private final static int COLUMN_3_WIDTH = 100;
	private final static int COLUMN_HEIGHT = 24;

	private Color lastBackground;

	EntryRow(
			String column1, String column2, String column3,
			Color textColor1, Color textColor2, Color textColor3, Color backgroundColor
	)
	{
		super();
		setLayout(new GridBagLayout());
		addLabel(column1, textColor1, COLUMN_1_WIDTH);
		addLabel(column2, textColor2, COLUMN_2_WIDTH);
		addLabel(column3, textColor3, COLUMN_3_WIDTH);
		if (backgroundColor != null)
		{
			setBackground(backgroundColor);
		}

		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				EntryRow.this.lastBackground = getBackground();
				setBackground(getBackground().brighter());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				setBackground(lastBackground);
			}
		});
	}

	private void addLabel(String text, Color textColor, int width)
	{
		JLabel label = new JLabel(text);
		label.setFont(FontManager.getRunescapeFont());
		if (textColor != null)
		{
			label.setForeground(textColor);
		}
		label.setPreferredSize(new Dimension(width, COLUMN_HEIGHT));
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
