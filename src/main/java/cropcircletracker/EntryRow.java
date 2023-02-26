package cropcircletracker;

import net.runelite.client.ui.FontManager;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/*
A component for displaying an entry in the panel table (i.e. for displaying the world/likelihood/world type).
*/
class EntryRow extends JPanel
{
	private static final int COLUMN_1_WIDTH = 50;
	private static final int COLUMN_2_WIDTH = 50;
	private static final int COLUMN_3_WIDTH = 100;
	private static final int COLUMN_HEIGHT = 24;
	private static final Color CURRENT_WORLD_COLOR = new Color(0, 255, 0);
	private static final Color LIKELIHOOD_COLOR_1 = new Color(0, 255, 0);
	private static final Color LIKELIHOOD_COLOR_2 = new Color(128, 255, 0);
	private static final Color LIKELIHOOD_COLOR_3 = new Color(255, 255, 0);
	private static final Color LIKELIHOOD_COLOR_4 = new Color(255, 128, 0);
	private static final Color LIKELIHOOD_COLOR_5 = new Color(255, 0, 0);
	private static final Color DANGEROUS_WORLD_TYPE_COLOR = new Color(255, 0, 0);
	private static final List<WorldType> dangerousWorldTypes = Arrays.asList(
			WorldType.BOUNTY,
			WorldType.HIGH_RISK,
			WorldType.PVP
	);
	private final CropCircleTrackerPlugin plugin;
	private Color lastBackground;

	EntryRow(int world, double likelihood, Color backgroundColor, CropCircleTrackerPlugin plugin)
	{
		super();
		this.plugin = plugin;
		setLayout(new GridBagLayout());
		addLabel(String.valueOf(world), getWorldColor(world), COLUMN_1_WIDTH);
		addLabel(getLikelihoodString(likelihood), getLikelihoodColor(likelihood), COLUMN_2_WIDTH);
		addLabel(getWorldTypeString(world), getWorldTypeColor(world), COLUMN_3_WIDTH);
		if (backgroundColor != null)
		{
			setBackground(backgroundColor);
		}

		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent mouseEvent)
			{
				if (mouseEvent.getClickCount() == 2)
				{
					plugin.worldHopper.scheduleHop(world);
				}
			}
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
		else if (worldTypes.contains(WorldType.BOUNTY))
		{
			return "Target World";
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

	private Color getWorldColor(int worldID)
	{
		if (worldID == plugin.client.getWorld())
		{
			return CURRENT_WORLD_COLOR;
		}
		return null;
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

	private void addLabel(String text, Color textColor, int width)
	{
		JLabel label = new JLabel(text);
		label.setFont(FontManager.getRunescapeFont());
		if (textColor != null)
		{
			label.setForeground(textColor);
		}
		label.setPreferredSize(new Dimension(width, COLUMN_HEIGHT));
		label.setMinimumSize(new Dimension(width, COLUMN_HEIGHT));
		label.setMaximumSize(new Dimension(width, COLUMN_HEIGHT));
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
