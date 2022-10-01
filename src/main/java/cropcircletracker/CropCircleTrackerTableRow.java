package cropcircletracker;

import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;

class CropCircleTrackerTableRow extends JPanel
{
	private final static int COLUMN_1_WIDTH = 50;
	private final static int COLUMN_2_WIDTH = 50;
	private final static int COLUMN_3_WIDTH = 100;
	private final static int COLUMN_HEIGHT = 24;

	CropCircleTrackerTableRow(
			String column1, String column2, String column3,
			Color textColor1, Color textColor2, Color textColor3, Color backgroundColor
	)
	{
		setLayout(new GridBagLayout());
		addLabel(column1, textColor1, COLUMN_1_WIDTH);
		addLabel(column2, textColor2, COLUMN_2_WIDTH);
		addLabel(column3, textColor3, COLUMN_3_WIDTH);
		if (backgroundColor != null)
		{
			setBackground(backgroundColor);
		}
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
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		add(label, c);
	}
}
