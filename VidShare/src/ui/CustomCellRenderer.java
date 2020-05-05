package ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.JPanel;

public class CustomCellRenderer implements ListCellRenderer {

	protected static Border border = new LineBorder(new Color(0xdddddd), 1);
	static Color disabledForeground = Color.DARK_GRAY;
	static Color disabledBackground = Color.LIGHT_GRAY;
	static Color noFocusBackground = Color.WHITE;
	static Color focusBackground = new Color(0xa3e0ff);
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		((JPanel) value).setBorder(border);
		((JPanel) value).setBackground(((JPanel) value).isEnabled()? (isSelected? focusBackground:noFocusBackground) : disabledBackground);
		return (Component) value;
	}
	
}
