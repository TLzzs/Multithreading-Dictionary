package GUI;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Dimension;

public class ToggleButton extends JButton {
    private static final Color SELECTED_COLOR = new Color(230, 230, 250);
    private static final Color DESELECTED_COLOR = Color.WHITE;

    public ToggleButton(String text) {
        super(text);
        setPreferredSize(new Dimension(100, 40));
        setBackground(DESELECTED_COLOR);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(true);
    }

    public void setSelected(boolean selected) {
        setBackground(selected ? SELECTED_COLOR : DESELECTED_COLOR);
    }
}
