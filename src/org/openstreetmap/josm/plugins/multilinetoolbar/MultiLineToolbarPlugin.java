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
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JToolBar;

import org.openstreetmap.josm.data.preferences.NamedColorProperty;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.spi.preferences.PreferenceChangedListener;

public class MultiLineToolbarPlugin extends Plugin {  
  private static MultiLineToolbarPlugin instance;
  private LayoutManager original;
  private MultilineToolbarLayout layout;
  
  public MultiLineToolbarPlugin(PluginInformation info) {
    super(info);
    instance = this;
    
    original = MainApplication.getToolbar().control.getLayout();
    
    if(Config.getPref().getBoolean(MultiLineToolbarPref.KEY_ENABLED, true)) {
      layout = new MultilineToolbarLayout();
      MainApplication.getToolbar().control.setLayout(layout);
    }
    
    Config.getPref().addKeyPreferenceChangeListener(MultiLineToolbarPref.KEY_ENABLED, e -> {
      if(Boolean.parseBoolean(e.getNewValue().toString())) {
        layout = new MultilineToolbarLayout();
        MainApplication.getToolbar().control.setLayout(layout);
        MainApplication.getToolbar().control.revalidate();
      }
      else {
        JToolBar bar = MainApplication.getToolbar().control;
        int maxHeight = 0;
        
        for(int i = 0; i < bar.getComponentCount(); i++) {
          maxHeight = Math.max(bar.getComponent(i).getHeight(), maxHeight);
  
          if(bar.getComponent(i) instanceof AbstractButton && ((AbstractButton)bar.getComponent(i)).getIcon() instanceof CompoundIcon) {
            AbstractButton b = (AbstractButton)bar.getComponent(i);
            
            CompoundIcon icon = (CompoundIcon)b.getIcon();
            b.setIcon(icon.icon);
            b.setDisabledIcon(icon.iconDisabled);
          }
        }
        
        bar.setLayout(original);
        
        if(maxHeight != 0) {
          bar.setPreferredSize(new Dimension(bar.getWidth(), maxHeight));
        }
        
        bar.revalidate();
        layout = null;
      }
    });
    
    PreferenceChangedListener l = e -> {
      MainApplication.getToolbar().control.invalidate();
    };
    
    Config.getPref().addKeyPreferenceChangeListener(MultiLineToolbarPref.KEY_COLOR_BORDER, l);
    Config.getPref().addKeyPreferenceChangeListener(MultiLineToolbarPref.KEY_COLOR_FILLING, l);
    Config.getPref().addKeyPreferenceChangeListener(MultiLineToolbarPref.KEY_MENU_INDICATOR_SIZE, l);
    Config.getPref().addKeyPreferenceChangeListener(MultiLineToolbarPref.KEY_MENU_INDICATOR_ENABLED, l);
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
    public void layoutContainer(Container parent) {
      int xPos = 0;
      int yPos = 0;
      
      int lineWidth = parent.getWidth();
      int lineHeight = 0;
      
      for(int i = 0; i < parent.getComponentCount(); i++) {
        lineHeight = Math.max(lineHeight, parent.getComponent(i).getPreferredSize().height);
      }
      
      if(lineHeight != 0) {
        for(int i = 0; i < parent.getComponentCount(); i++) {
          int height = lineHeight;
          int width = parent.getComponent(i).getPreferredSize().width;

          if(parent.getComponent(i) instanceof JToolBar.Separator) {
            width = parent.getComponent(i).getMinimumSize().width;
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
  
          int add = 0;
  
          if(i < parent.getComponentCount()-1) {
              add = parent.getComponent(i+1).getPreferredSize().width;
          }
  
          if(lineWidth != 0 && xPos + add > lineWidth) {
              xPos = 0;
              yPos += lineHeight;
          }
        }
      }
      else {
        lineHeight = 30;
      }
      
      parent.setPreferredSize(new Dimension(xPos, yPos + lineHeight));
      
      width = xPos;
      height = yPos + lineHeight;
    }
  }
  
  private static final class CompoundIcon extends ImageIcon {
    private static final NamedColorProperty COLOR_BORDER = new NamedColorProperty(MultiLineToolbarPref.KEY_COLOR_BORDER, new Color(0, 255, 255));
    private static final NamedColorProperty COLOR_FILLING = new NamedColorProperty(MultiLineToolbarPref.KEY_COLOR_FILLING, new Color(0, 0, 255));
    
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
        
        int xPos = x+getIconWidth()-indicatorSize+3;
        int yPos = getIconHeight()-indicatorSize+3;
        
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
