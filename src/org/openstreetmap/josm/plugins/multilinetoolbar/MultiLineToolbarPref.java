package org.openstreetmap.josm.plugins.multilinetoolbar;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;

public class MultiLineToolbarPref extends DefaultTabPreferenceSetting {
  public static final String KEY_ENABLED = MultiLineToolbarPlugin.class.getName()+".enabled";
  
  public static final String KEY_MENU_INDICATOR_ENABLED = MultiLineToolbarPlugin.class.getName()+".menuIndicatorEnabled";
  public static final String KEY_MENU_INDICATOR_SIZE = MultiLineToolbarPlugin.class.getName()+".menuIndicatorSize";
  public static final String KEY_COLOR_BORDER = MultiLineToolbarPlugin.class.getName()+".menuIndicatorBorder";
  public static final String KEY_COLOR_FILLING = MultiLineToolbarPlugin.class.getName()+".menuIndicatorFilling";
  
  private JCheckBox enabled;
  private JCheckBox indicatorEnabled;
  private JSpinner indicatorSize;
  
  public MultiLineToolbarPref() {
    super("main_toolbar", tr("MultiLineToolbar"), tr("Change settings for MultiLineToolbar plugin."));
  }
  
  @Override
  public void addGui(PreferenceTabbedPane gui) {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    enabled = new JCheckBox(tr("Multi line toolbar enabled"), Config.getPref().getBoolean(KEY_ENABLED, true));
    indicatorEnabled = new JCheckBox(tr("Toolbar menu button indicator enabled"), Config.getPref().getBoolean(KEY_MENU_INDICATOR_ENABLED, true));
    indicatorSize = new JSpinner(new SpinnerNumberModel(Config.getPref().getInt(KEY_MENU_INDICATOR_SIZE, 7), 4, 10, 1));
    final JLabel size = new JLabel(tr("Indicator size:"));
    GBC gbc = GBC.std(0, 0).span(3).fill(GBC.HORIZONTAL).anchor(GBC.NORTH).weight(0, 0);
    
    enabled.addItemListener(e -> {
      indicatorEnabled.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      indicatorSize.setEnabled(e.getStateChange() == ItemEvent.SELECTED && indicatorEnabled.isSelected());
      size.setEnabled(indicatorSize.isEnabled());
    });
    
    indicatorEnabled.addItemListener(e -> {
      indicatorSize.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      size.setEnabled(indicatorSize.isEnabled());
    });
    
    indicatorEnabled.setEnabled(enabled.isSelected());
    indicatorSize.setEnabled(enabled.isSelected() && indicatorEnabled.isSelected());
    size.setEnabled(indicatorSize.isEnabled());
    
    panel.add(enabled, gbc);
    panel.add(indicatorEnabled, gbc.insets(20, 0, 0, 0).grid(0, 1));
    panel.add(size, gbc.insets(45, 0, 0, 0).grid(0, 2).span(1).fill(GBC.NONE).anchor(GBC.CENTER));
    panel.add(indicatorSize, gbc.insets(5, 0, 0, 0).grid(1, 2));
    panel.add(Box.createVerticalGlue(), GBC.eol().grid(2, 3).fill(GBC.BOTH));
   // panel.add(new JPanel(), gbc.insets(0).grid(2, 3).fill(GBC.BOTH));
    
    createPreferenceTabWithScrollPane(gui, panel);
  }

  @Override
  public boolean ok() {
    Config.getPref().putBoolean(KEY_ENABLED, enabled.isSelected());
    Config.getPref().putBoolean(KEY_MENU_INDICATOR_ENABLED, indicatorEnabled.isSelected());
    Config.getPref().putInt(KEY_MENU_INDICATOR_SIZE, (int)indicatorSize.getValue());
    
    return false;
  }

}
