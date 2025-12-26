package org.openstreetmap.josm.plugins.multilinetoolbar;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;

import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.gui.preferences.display.ColorPreference;
import org.openstreetmap.josm.gui.widgets.FilterField;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;

public class MultiLineToolbarPref extends DefaultTabPreferenceSetting {
  public static final String KEY_BASE = MultiLineToolbarPlugin.class.getSimpleName();
  
  public static final String KEY_ENABLED = KEY_BASE+".enabled";
  public static final String KEY_BUTTONS_COMPACT = KEY_BASE+".buttonsCompact";
  
  public static final String KEY_MENU_INDICATOR_ENABLED = KEY_BASE+".menuIndicatorEnabled";
  public static final String KEY_MENU_INDICATOR_SIZE = KEY_BASE+".menuIndicatorSize";
  public static final String KEY_COLOR_BORDER = KEY_BASE+".menuIndicatorBorder";
  public static final String KEY_COLOR_FILLING = KEY_BASE+".menuIndicatorFilling";
  
  private JCheckBox enabled;
  private JCheckBox buttonsCompact;
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
    buttonsCompact = new JCheckBox(tr("Compact buttons"), Config.getPref().getBoolean(KEY_BUTTONS_COMPACT, true));
    indicatorEnabled = new JCheckBox(tr("Toolbar menu button indicator enabled"), Config.getPref().getBoolean(KEY_MENU_INDICATOR_ENABLED, true));
    indicatorSize = new JSpinner(new SpinnerNumberModel(Config.getPref().getInt(KEY_MENU_INDICATOR_SIZE, 7), 4, 10, 1));
    final JLabel size = new JLabel(tr("Indicator size:"));
    final JLabel link = new JLabel("<html><a href=\"#link\">"+tr("Change menu indicator colors in color setting")+"</a></html>");
    link.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    link.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if(link.isEnabled()) {
          try {
            gui.selectTabByPref(ColorPreference.class);
            JComponent p = (JComponent)gui.getSelectedComponent();
            
            for(int i = 0; i < p.getComponentCount(); i++) {
              if(p.getComponent(i) instanceof JTabbedPane) {
                JTabbedPane tab = (JTabbedPane)p.getComponent(i);
                
                for(int j = 0; j < tab.getComponentCount(); j++) {
                  if(tab.getComponent(j) instanceof JPanel) {
                    JPanel p2 = (JPanel)tab.getComponent(j);
                    
                    for(int l = 0; l < p2.getComponentCount(); l++) {
                      if(p2.getComponent(l) instanceof FilterField) {
                        ((FilterField)p2.getComponent(l)).setText(KEY_BASE);
                        break;
                      }
                    }
                  }
                }
              }
            }
          }catch(Throwable t) {}
        }
      }
    });
    
    enabled.addItemListener(e -> {
      buttonsCompact.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      indicatorEnabled.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      indicatorSize.setEnabled(e.getStateChange() == ItemEvent.SELECTED && indicatorEnabled.isSelected());
      size.setEnabled(indicatorSize.isEnabled());
      link.setEnabled(indicatorSize.isEnabled());
    });
    
    indicatorEnabled.addItemListener(e -> {
      indicatorSize.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      size.setEnabled(indicatorSize.isEnabled());
      link.setEnabled(indicatorSize.isEnabled());
    });
    
    buttonsCompact.setEnabled(enabled.isSelected());
    indicatorEnabled.setEnabled(enabled.isSelected());
    indicatorSize.setEnabled(enabled.isSelected() && indicatorEnabled.isSelected());
    size.setEnabled(indicatorSize.isEnabled());
    link.setEnabled(indicatorSize.isEnabled());
    
    GBC gbc = GBC.std(0, 0).span(3).fill(GBC.HORIZONTAL).anchor(GBC.NORTH).weight(0, 0);
    
    panel.add(enabled, gbc);
    panel.add(buttonsCompact, gbc.grid(0, 1));
    panel.add(indicatorEnabled, gbc.insets(20, 2, 0, 0).grid(0, 2));
    panel.add(size, gbc.insets(45, 2, 0, 0).grid(0, 3).span(1).fill(GBC.NONE).anchor(GBC.CENTER));
    panel.add(indicatorSize, gbc.insets(5, 2, 0, 0).grid(1, 3));
    panel.add(link, gbc.insets(45, 2, 0, 0).grid(0, 4).span(3).fill(GBC.HORIZONTAL).anchor(GBC.WEST));
    panel.add(Box.createVerticalGlue(), GBC.eol().grid(0, 5).fill(GBC.BOTH));
    
    createPreferenceTabWithScrollPane(gui, panel);
  }

  @Override
  public boolean ok() {
    Config.getPref().putBoolean(KEY_ENABLED, enabled.isSelected());
    Config.getPref().putBoolean(KEY_BUTTONS_COMPACT, buttonsCompact.isSelected());
    Config.getPref().putBoolean(KEY_MENU_INDICATOR_ENABLED, indicatorEnabled.isSelected());
    Config.getPref().putInt(KEY_MENU_INDICATOR_SIZE, (int)indicatorSize.getValue());
    
    return false;
  }

}
