package com.apkscanner.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import com.apkscanner.resource.Resource;


public class ToolBar extends JToolBar
{
	private static final long serialVersionUID = 894134416480807167L;

	private HashMap<ButtonSet, JButton> buttonMap;
	private HashMap<MenuItemSet, JMenuItem> menuItemMap;
	
	public enum MenuItemSet
	{
		NEW_WINDOW		(null, null, null, '\0', true),
		NEW_EMPTY		(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK, false), 'N'),
		NEW_APK			(null, Resource.IMG_TOOLBAR_OPEN.getImageIcon(16,16), null, '\0'),
		NEW_PACKAGE		(null, null, null, '\0'),
		OPEN_APK		(null, Resource.IMG_TOOLBAR_OPEN.getImageIcon(16,16), KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK, false), 'O'),
		OPEN_PACKAGE	(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK, false), 'P'),
		INSTALL_APK		(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK, false), 'I'),
		INSTALLED_CHECK	(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK, false), 'T');
		
    	private String text = null;
    	private ImageIcon icon = null;
    	private String actionCommand = null;
    	private KeyStroke keyStroke = null;
    	private char mnemonic = '\0';
    	private boolean extend = false;

    	
    	MenuItemSet(String text, ImageIcon icon, KeyStroke keyStroke, char mnemonic) {
    		this(text, icon, keyStroke, mnemonic, false);
    	}
    	
    	MenuItemSet(String text, ImageIcon icon, KeyStroke keyStroke, char mnemonic, boolean extend) {
    		this.text = text;
    		this.icon = icon;
    		this.keyStroke = keyStroke;
    		this.mnemonic = mnemonic;
    		this.extend = extend;
    		this.actionCommand = this.getClass().getName()+"."+this.toString();
    	}
    	
    	public boolean matchActionEvent(ActionEvent e)
    	{
    		return actionCommand.equals(e.getActionCommand());
    	}
    	
    	private JMenuItem getMenuItem(ActionListener listener)
    	{
    		JMenuItem menuItem = null;
    		if(!extend) {
    			menuItem = new JMenuItem(text);
    			menuItem.setAccelerator(keyStroke);
    		} else {
    			menuItem = new JMenu(text);
    		}
            menuItem.addActionListener(listener);
            menuItem.setActionCommand(actionCommand);
            menuItem.setIcon(icon);
            
            menuItem.setMnemonic(mnemonic);
            return menuItem;
    	}
    	
    	static private HashMap<MenuItemSet, JMenuItem> getButtonMap(ActionListener listener)
    	{
    		HashMap<MenuItemSet, JMenuItem> menuItemMap = new HashMap<MenuItemSet, JMenuItem>();
            for(MenuItemSet bs: values()) {
            	menuItemMap.put(bs, bs.getMenuItem(listener));
            }
            return menuItemMap;
    	}
	}
			
	
    public enum ButtonSet
    {
    	OPEN			(Type.HOVER, null, Resource.IMG_TOOLBAR_OPEN.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
    	OPEN_EXTEND		(Type.EXTEND, null, Resource.IMG_TOOLBAR_OPEN_ARROW.getImageIcon(10,10)),
    	MANIFEST		(Type.HOVER, null, Resource.IMG_TOOLBAR_MANIFEST.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
    	EXPLORER		(Type.HOVER, null, Resource.IMG_TOOLBAR_EXPLORER.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
    	INSTALL			(Type.HOVER, null, Resource.IMG_TOOLBAR_INSTALL.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
    	INSTALL_EXTEND	(Type.EXTEND, null, Resource.IMG_TOOLBAR_OPEN_ARROW.getImageIcon(10,10)),
    	SETTING			(Type.HOVER, null, Resource.IMG_TOOLBAR_SETTING.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
    	ABOUT			(Type.HOVER, null, Resource.IMG_TOOLBAR_ABOUT.getImageIcon(ButtonSet.IconSize, ButtonSet.IconSize)),
    	ALL				(Type.NONE, null, null),
    	NEED_TARGET_APK	(Type.NONE, null, null);

    	private enum Type {
    		NONE, NORMAL, HOVER, EXTEND
    	}
    	
    	static private final int IconSize = 40;

    	private Type type = null;
    	private String text = null;
    	private ImageIcon icon = null;
    	private ImageIcon hoverIcon = null;
    	private String actionCommand = null;
    	
    	ButtonSet(Type type, String text, ImageIcon icon)
    	{
    		this(type, text, icon, icon);
    	}
    	
    	ButtonSet(Type type, String text, ImageIcon icon, ImageIcon hoverIcon)
    	{
    		this.type = type;
    		this.text = text;
    		this.icon = icon;
    		this.hoverIcon = hoverIcon;
    		this.actionCommand = this.getClass().getName()+"."+this.toString();
    	}
    	
    	public boolean matchActionEvent(ActionEvent e)
    	{
    		return actionCommand.equals(e.getActionCommand());
    	}

    	private JButton getButton(ActionListener listener)
    	{
    		JButton button = null;
    		switch(type) {
    		case NORMAL:
    			button = new JButton(text, icon);
    			button.addActionListener(listener);
    			button.setVerticalTextPosition(JLabel.BOTTOM);
    			button.setHorizontalTextPosition(JLabel.CENTER);
    			button.setBorderPainted(false);
    			button.setOpaque(false);
    			button.setFocusable(false);
    			button.setPreferredSize(new Dimension(63,65));
    			break;
    		case HOVER:
    			button = new ToolBarButton(text, icon, hoverIcon, listener);
    			button.setVerticalTextPosition(JLabel.BOTTOM);
    			button.setHorizontalTextPosition(JLabel.CENTER);
    			button.setBorderPainted(false);
    			button.setOpaque(false);
    			button.setFocusable(false);
    			button.setPreferredSize(new Dimension(63,65));
    			break;
    		case EXTEND:
    			button = new JButton(text, icon);
    			button.setMargin(new Insets(27,0,27,0));
    			button.setBorderPainted(false);
    			button.setOpaque(false);
    			button.setFocusable(false);
    			break;
    		default:
    			return null;
    		}
			button.setActionCommand(actionCommand);
    		
    		return button;
    	}
    	
    	static private HashMap<ButtonSet, JButton> getButtonMap(ActionListener listener)
    	{
    		HashMap<ButtonSet, JButton> buttonMap = new HashMap<ButtonSet, JButton>();
            for(ButtonSet bs: values()) {
            	buttonMap.put(bs, bs.getButton(listener));
            }
            return buttonMap;
    	}
    }
	
    static private class ToolBarButton extends JButton
    {
    	private static final long serialVersionUID = -6788392217820751244L;
    	public ImageIcon  mHoverIcon, mIcon;

    	public ToolBarButton(String text, ImageIcon icon, ImageIcon hoverIcon, ActionListener listener)
    	{
    		super(text, icon);
    		this.mHoverIcon = hoverIcon;
    		this.mIcon = icon;
    		this.addActionListener(listener);

    		this.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent evt) {
                	setIcon(mHoverIcon);
                }
                public void mouseExited(MouseEvent evt) {
                	setIcon(mIcon);
                }
            });
    	}
    }
    
    public ToolBar(ActionListener listener)
    {
        initUI(listener);
    }

    public final void initUI(ActionListener listener)
    {
        buttonMap = ButtonSet.getButtonMap(listener);
        menuItemMap = MenuItemSet.getButtonMap(listener);
        
    	final JPopupMenu openPopupMenu = new JPopupMenu();
    	JMenuItem SubMenu = openPopupMenu.add((JMenu)menuItemMap.get(MenuItemSet.NEW_WINDOW));
    	SubMenu.add(menuItemMap.get(MenuItemSet.NEW_EMPTY));
    	SubMenu.add(menuItemMap.get(MenuItemSet.NEW_APK));
    	SubMenu.add(menuItemMap.get(MenuItemSet.NEW_PACKAGE));
        openPopupMenu.add(menuItemMap.get(MenuItemSet.OPEN_APK));
        openPopupMenu.add(menuItemMap.get(MenuItemSet.OPEN_PACKAGE));

        final JPopupMenu installPopupMenu = new JPopupMenu();
        installPopupMenu.add(menuItemMap.get(MenuItemSet.INSTALL_APK));
        installPopupMenu.add(menuItemMap.get(MenuItemSet.INSTALLED_CHECK));
        
        Dimension sepSize = new Dimension(2,65);
              
        add(buttonMap.get(ButtonSet.OPEN));
        add(buttonMap.get(ButtonSet.OPEN_EXTEND));
        
        add(getNewSeparator(JSeparator.VERTICAL, sepSize));
                
        add(buttonMap.get(ButtonSet.MANIFEST));
        add(buttonMap.get(ButtonSet.EXPLORER));
        
        add(getNewSeparator(JSeparator.VERTICAL, sepSize));

        add(buttonMap.get(ButtonSet.INSTALL));
        add(buttonMap.get(ButtonSet.INSTALL_EXTEND));
        
        add(getNewSeparator(JSeparator.VERTICAL, sepSize));

        add(buttonMap.get(ButtonSet.SETTING));
        add(getNewSeparator(JSeparator.VERTICAL, sepSize));
        
        add(buttonMap.get(ButtonSet.ABOUT));
        
        buttonMap.get(ButtonSet.OPEN_EXTEND).addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	JButton btn = buttonMap.get(ButtonSet.OPEN_EXTEND);
            	openPopupMenu.show(btn, btn.getWidth()/2, btn.getHeight());
            }
        });
        
        buttonMap.get(ButtonSet.INSTALL_EXTEND).addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            	JButton btn = buttonMap.get(ButtonSet.INSTALL_EXTEND);
            	installPopupMenu.show(btn, btn.getWidth()/2, btn.getHeight());
            }
        });
        
        reloadResource();

        setAlignmentX(0);
        setFloatable(false);
        setOpaque(false);

        setLayout(new FlowLayout(FlowLayout.LEFT));
    }

    private JSeparator getNewSeparator(int orientation, Dimension size)
    {
        JSeparator separator = new JSeparator(orientation);
        separator.setBackground(Color.gray);
        separator.setForeground(Color.gray);
        separator.setPreferredSize(size);
    	return separator;
    }
    
    private void setButtonText(ButtonSet buttonSet, String text, String tipText)
    {
    	buttonMap.get(buttonSet).setText(text);
    	buttonMap.get(buttonSet).setToolTipText(tipText);
    }
    
    private void setMenuItemText(MenuItemSet menuItemSet, String text, String tipText)
    {
    	menuItemMap.get(menuItemSet).setText(text);
    	menuItemMap.get(menuItemSet).setToolTipText(tipText);
    }

    public void reloadResource()
    {
    	setButtonText(ButtonSet.OPEN, Resource.STR_BTN_OPEN.getString(), Resource.STR_BTN_OPEN_LAB.getString());
    	setButtonText(ButtonSet.MANIFEST, Resource.STR_BTN_MANIFEST.getString(), Resource.STR_BTN_MANIFEST_LAB.getString());
    	setButtonText(ButtonSet.EXPLORER, Resource.STR_BTN_EXPLORER.getString(), Resource.STR_BTN_EXPLORER_LAB.getString());
    	setButtonText(ButtonSet.INSTALL, Resource.STR_BTN_INSTALL.getString(), Resource.STR_BTN_INSTALL_LAB.getString());
    	setButtonText(ButtonSet.SETTING, Resource.STR_BTN_SETTING.getString(), Resource.STR_BTN_SETTING_LAB.getString());
    	setButtonText(ButtonSet.ABOUT, Resource.STR_BTN_ABOUT.getString(), Resource.STR_BTN_ABOUT_LAB.getString());
    	
    	setMenuItemText(MenuItemSet.NEW_WINDOW, Resource.STR_MENU_NEW.getString(), null);
    	setMenuItemText(MenuItemSet.NEW_EMPTY, Resource.STR_MENU_NEW_WINDOW.getString(), null);
    	setMenuItemText(MenuItemSet.NEW_APK, Resource.STR_MENU_NEW_APK_FILE.getString(), null);
    	setMenuItemText(MenuItemSet.NEW_PACKAGE, Resource.STR_MENU_NEW_PACKAGE.getString(), null);
    	setMenuItemText(MenuItemSet.OPEN_APK, Resource.STR_MENU_APK_FILE.getString(), null);
    	setMenuItemText(MenuItemSet.OPEN_PACKAGE, Resource.STR_MENU_PACKAGE.getString(), null);
    	setMenuItemText(MenuItemSet.INSTALL_APK, Resource.STR_MENU_INSTALL.getString(), null);
    	setMenuItemText(MenuItemSet.INSTALLED_CHECK, Resource.STR_MENU_CHECK_INSTALLED.getString(), null);
    }

    public void setEnabledAt(ButtonSet buttonId, boolean enabled)
    {
    	switch(buttonId) {
    	case ALL:
    	case OPEN:
    		buttonMap.get(ButtonSet.OPEN).setEnabled(enabled);
    		buttonMap.get(ButtonSet.OPEN_EXTEND).setEnabled(enabled);
    		if(buttonId != ButtonSet.ALL) break;
    	case ABOUT:
    		buttonMap.get(ButtonSet.ABOUT).setEnabled(enabled);
    		if(buttonId != ButtonSet.ALL) break;
    	case NEED_TARGET_APK:
    		buttonId = ButtonSet.ALL;
    	case MANIFEST:
    		buttonMap.get(ButtonSet.MANIFEST).setEnabled(enabled);
    		if(buttonId != ButtonSet.ALL) break;
    	case EXPLORER:
    		buttonMap.get(ButtonSet.EXPLORER).setEnabled(enabled);
    		if(buttonId != ButtonSet.ALL) break;
    	case INSTALL:
    		buttonMap.get(ButtonSet.INSTALL).setEnabled(enabled);
    		buttonMap.get(ButtonSet.INSTALL_EXTEND).setEnabled(enabled);
    		if(buttonId != ButtonSet.ALL) break;
    	default:
    		break;
    	}
    }
}