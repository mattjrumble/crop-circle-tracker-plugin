package cropcircletracker;

import net.runelite.client.ui.FontManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

class CropCircleTrackerTableRow extends JPanel
{
	CropCircleTrackerTableRow(
			String column1, String column2, String column3,
			Color textColor1, Color textColor2, Color textColor3, Color backgroundColor
	) {
		setLayout(new GridLayout());
		setBorder(new EmptyBorder(0, 0, 0, 0));
		addCell(column1, textColor1);
		addCell(column2, textColor2);
		addCell(column3, textColor3);
		if (backgroundColor != null) {
			setBackground(backgroundColor);
		}
	}

	private void addCell(String text, Color textColor) {
		JPanel cell = new JPanel(new GridBagLayout());
		cell.setBorder(new EmptyBorder(3, 3, 3, 3));
		JLabel label = new JLabel(text);
		if (textColor != null) {
			label.setForeground(textColor);
		}
		label.setFont(FontManager.getRunescapeFont());
		cell.add(label);
		cell.setOpaque(false);
		add(cell);
	}
}
