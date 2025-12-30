package org.openstreetmap.josm.plugins.multilinetoolbar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Point;
import java.util.Objects;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.data.preferences.NamedColorProperty;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.spi.preferences.PreferenceChangedListener;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.ImageProvider.ImageSizes;

public class MultiLineToolbarPlugin extends Plugin {
  private static final int BORDER_COMPACT = 3;
  private static final int BORDER_NORMAL = 5;
  
  private static int BORDER_HORIZONTAL = Config.getPref().getBoolean(MultiLineToolbarPref.KEY_BUTTONS_COMPACT, true) ? BORDER_COMPACT : BORDER_NORMAL;
  
  private static final NamedColorProperty COLOR_BORDER = new NamedColorProperty(MultiLineToolbarPref.KEY_COLOR_BORDER, new Color(0, 255, 255));
  private static final NamedColorProperty COLOR_FILLING = new NamedColorProperty(MultiLineToolbarPref.KEY_COLOR_FILLING, new Color(0, 0, 255));

  private static MultiLineToolbarPlugin instance;
  private LayoutManager original;
  private MultilineToolbarLayout layout;
  private JMenuItem configure;
  
  public MultiLineToolbarPlugin(PluginInformation info) {
    super(info);
    instance = this;
    configure = new JMenuItem(org.openstreetmap.josm.tools.I18n.tr("Configure MultiLineToolbar"));
    configure.setIcon(ImageProvider.get("preferences/main_toolbar", ImageSizes.MENU));
    configure.addActionListener(e -> {
      final PreferenceDialog p = new PreferenceDialog(MainApplication.getMainFrame());
      SwingUtilities.invokeLater(() -> p.selectPreferencesTabByClass(MultiLineToolbarPref.class));
      p.setVisible(true);
    });
    
    if(MainApplication.getToolbar() != null) {
      original = MainApplication.getToolbar().control.getLayout();
      addPopupEntry();
    }
    
    if(Config.getPref().getBoolean(MultiLineToolbarPref.KEY_ENABLED, true) && MainApplication.getToolbar() != null) {
      layout = new MultilineToolbarLayout();
      MainApplication.getToolbar().control.setLayout(layout);
    }
    
    Config.getPref().addKeyPreferenceChangeListener(MultiLineToolbarPref.KEY_ENABLED, e -> {
      if(MainApplication.getToolbar() != null) {
        if(Boolean.parseBoolean(e.getNewValue().toString())) {
          layout = new MultilineToolbarLayout();
          MainApplication.getToolbar().control.setLayout(layout);
          MainApplication.getToolbar().control.doLayout();
        }
        else {
          JToolBar bar = MainApplication.getToolbar().control;
          int maxHeight = 0;
          
          for(int i = bar.getComponentCount()-1; i >= 0 ; i--) {
            maxHeight = Math.max(bar.getComponent(i).getHeight(), maxHeight);
    
            if(bar.getComponent(i) instanceof AbstractButton && ((AbstractButton)bar.getComponent(i)).getIcon() instanceof CompoundIcon) {
              AbstractButton b = (AbstractButton)bar.getComponent(i);
              
              CompoundIcon icon = (CompoundIcon)b.getIcon();
              b.setIcon(icon.icon);
              b.setDisabledIcon(icon.iconDisabled);
            }
            else if(bar.getComponent(i) instanceof JSeparator && ((JSeparator)bar.getComponent(i)).getOrientation() == JSeparator.HORIZONTAL) {
              bar.remove(i);
            }
          }
          
          bar.setLayout(original);
          
          if(maxHeight != 0) {
            bar.setPreferredSize(new Dimension(bar.getWidth(), maxHeight));
          }
          
          bar.revalidate();
          layout = null;
        }
      }
    });
    
    PreferenceChangedListener l = e -> {
      if(layout != null && MainApplication.getToolbar() != null) {
        MainApplication.getToolbar().control.invalidate();
      }
    };
    
    Config.getPref().addKeyPreferenceChangeListener(MultiLineToolbarPref.KEY_BUTTONS_COMPACT, e -> {
      if(layout != null && MainApplication.getToolbar() != null) {
        BORDER_HORIZONTAL = Config.getPref().getBoolean(MultiLineToolbarPref.KEY_BUTTONS_COMPACT, true) ? BORDER_COMPACT : BORDER_NORMAL;
        MainApplication.getToolbar().control.doLayout();
      }
    });
    Config.getPref().addKeyPreferenceChangeListener(MultiLineToolbarPref.KEY_LINE_SEPARATOR, e -> {
      if(layout != null && MainApplication.getToolbar() != null) {
        MainApplication.getToolbar().control.doLayout();
      }
    });
    Config.getPref().addKeyPreferenceChangeListener("toolbar", e -> {
      if(layout != null && MainApplication.getToolbar() != null) {
        MainApplication.getToolbar().control.getParent().revalidate();
      }
    });
    Config.getPref().addKeyPreferenceChangeListener(MultiLineToolbarPref.KEY_COLOR_BORDER, l);
    Config.getPref().addKeyPreferenceChangeListener(MultiLineToolbarPref.KEY_COLOR_FILLING, l);
    Config.getPref().addKeyPreferenceChangeListener(MultiLineToolbarPref.KEY_MENU_INDICATOR_SIZE, l);
    Config.getPref().addKeyPreferenceChangeListener(MultiLineToolbarPref.KEY_MENU_INDICATOR_ENABLED, l);
  }
  
  private void addPopupEntry() {
    JPopupMenu popup = MainApplication.getToolbar().control.getComponentPopupMenu();
    
    if(popup != null) {
      popup.addSeparator();
      popup.add(configure);
    }
  }
  
  public static MultiLineToolbarPlugin getInstance() {
    return instance;
  }
  
  @Override
  public PreferenceSetting getPreferenceSetting() {
    return new MultiLineToolbarPref();
  }
  
  private static final class MultilineToolbarLayout implements LayoutManager {
    private int width = 0;
    private int height = 0;
    
    @Override
    public void addLayoutComponent(String name, Component comp) {}

    @Override
    public void removeLayoutComponent(Component comp) {}

    @Override
    public synchronized Dimension preferredLayoutSize(Container parent) {
        if(width == 0 && height == 0) {
            layoutContainer(parent);
        }

        return new Dimension(width,height);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(0,0);
    }

    @Override
    public synchronized void layoutContainer(Container parent) {
      int oldWidth = width;
      int oldHeight = height;
      
      int xPos = parent.getInsets().top;
      int yPos = parent.getInsets().left;
      
      int lineWidth = parent.getWidth();
      
      int buttonWidth = ImageProvider.ImageSizes.LARGEICON.getAdjustedWidth() + BORDER_HORIZONTAL*2;
      int lineHeight = ImageProvider.ImageSizes.LARGEICON.getAdjustedHeight() + 6;
      
      for(int i = parent.getComponentCount()-1; i >= 0; i--) {
        if(parent.getComponent(i) instanceof AbstractButton) {
          ((AbstractButton)parent.getComponent(i)).setBorder(BorderFactory.createEmptyBorder(2,BORDER_HORIZONTAL,2,BORDER_HORIZONTAL));
        }
        else if(parent.getComponent(i) instanceof JSeparator && ((JSeparator)parent.getComponent(i)).getOrientation() == JSeparator.HORIZONTAL) {
          parent.remove(i);
          continue;
        }
      }
            
      if(lineHeight != 0) {
        for(int i = 0; i < parent.getComponentCount(); i++) {
          int height = lineHeight;
          int width = Math.max(buttonWidth, parent.getComponent(i).getPreferredSize().width);

          if(parent.getComponent(i) instanceof JToolBar.Separator) {
            width = parent.getComponent(i).getMinimumSize().width;
            height = lineHeight;
          }
          else if(parent.getComponent(i) instanceof AbstractButton && Objects.equals("org.openstreetmap.josm.gui.tagging.presets.TaggingPresetMenu", ((AbstractButton)parent.getComponent(i)).getAction().getClass().getCanonicalName()) && !(((AbstractButton)parent.getComponent(i)).getIcon() instanceof CompoundIcon)) {
            CompoundIcon icon = new CompoundIcon(((AbstractButton)parent.getComponent(i)).getIcon(), ((AbstractButton)parent.getComponent(i)).getDisabledIcon(), lineHeight);
            
            if(icon.icon != null) {
              ((AbstractButton)parent.getComponent(i)).setIcon(icon);
              ((AbstractButton)parent.getComponent(i)).setDisabledIcon(icon);
            }
          }
          
          parent.getComponent(i).setLocation(new Point(xPos,yPos+lineHeight/2-height/2));
          parent.getComponent(i).setSize(width, height);
          xPos += width;
  
          int add = xPos + parent.getInsets().right;
          
          if(i < parent.getComponentCount()-1) {
            add += parent.getComponent(i+1) instanceof JSeparator ? parent.getComponent(i+1).getMinimumSize().width : Math.max(buttonWidth, parent.getComponent(i+1).getPreferredSize().width);
            
            if(lineWidth != 0 && add > lineWidth && parent.getComponent(i+1).getPreferredSize().width / 2 > buttonWidth) {
              add = lineWidth;
            }
          }
          
          if(lineWidth != 0 && add > lineWidth && i < parent.getComponentCount()-1) {
            xPos = parent.getInsets().left;
            yPos += lineHeight + parent.getInsets().bottom;
            
            if(Config.getPref().getBoolean(MultiLineToolbarPref.KEY_LINE_SEPARATOR, true)) {
              JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
              sep.setSize(lineWidth-xPos-parent.getInsets().right, sep.getPreferredSize().height);
              sep.setLocation(new Point(xPos,yPos));
              
              parent.add(sep,++i);            
              
              yPos += sep.getPreferredSize().height;
            }
            
            yPos+=parent.getInsets().top;
          }
        }
      }
      else {
        lineHeight = 30;
      }
      
      width = lineWidth;
      height = yPos + lineHeight + parent.getInsets().bottom;
      
      parent.setSize(new Dimension(width, height));
      
      //force update of parent container of toolbar and main panel
      if(oldWidth != width || oldHeight != height) {
        parent.getParent().doLayout();
        
        JPanel p = MainApplication.getMainPanel();
        p.doLayout();
        
        for(int i = 0; i < p.getComponentCount(); i++) {
          p.getComponent(i).doLayout();
          
          if(p.getComponent(i) instanceof MapFrame) {
            ((MapFrame)p.getComponent(i)).getComponent(0).doLayout();
          }
        }
        
        parent.getParent().revalidate();
      }
    }
  }
  
  private static final class CompoundIcon extends ImageIcon {
    private ImageIcon icon;
    private ImageIcon iconDisabled;
    private int lineHeight;
    
    public CompoundIcon(Icon icon, Icon iconDisabled, int lineHeight) {
      if(icon instanceof ImageIcon && iconDisabled instanceof ImageIcon) {
        this.icon = (ImageIcon)icon;
        this.lineHeight = lineHeight;
        this.iconDisabled = (ImageIcon)iconDisabled;
      }
    }
    
    @Override
    public int getIconHeight() {
      return lineHeight;
    }
    
    @Override
    public int getIconWidth() {
      return icon.getIconWidth();
    }
    
    @Override
    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
      if(c.isEnabled()) {
        icon.paintIcon(c, g, x, y+getIconHeight()/2-icon.getIconHeight()/2);
      }
      else {
        iconDisabled.paintIcon(c, g, x, y+getIconHeight()/2-icon.getIconHeight()/2);
      }
      
      if(Config.getPref().getBoolean(MultiLineToolbarPref.KEY_MENU_INDICATOR_ENABLED, true)) {
        int indicatorSize = Config.getPref().getInt(MultiLineToolbarPref.KEY_MENU_INDICATOR_SIZE, 7);
        
        Color color = g.getColor();
        
        int xPos = x+getIconWidth()-indicatorSize;
        int yPos = getIconHeight()-indicatorSize-1;
        
        if(c.isEnabled()) {
          g.setColor(COLOR_FILLING.get());
        }
        else {
          g.setColor(Color.lightGray);
        }
        g.fillArc(xPos, yPos, indicatorSize, indicatorSize, 0, 360);
        
        if(c.isEnabled()) {
          g.setColor(COLOR_BORDER.get());
        }
        else {
          g.setColor(Color.lightGray.brighter());
        }
        g.drawArc(xPos, yPos, indicatorSize, indicatorSize, 0, 360);
        
        g.setColor(color);
      }
    }
  }
}
