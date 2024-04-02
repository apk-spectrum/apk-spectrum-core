package com.apkspectrum.plugin.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

import com.apkspectrum.plugin.UpdateChecker;
import com.apkspectrum.plugin.PlugInConfig;
import com.apkspectrum.plugin.PlugInManager;
import com.apkspectrum.resource._RStr;
import com.apkspectrum.swing.KeyStrokeAction;
import com.apkspectrum.swing.WindowSizeMemorizer;

public class UpdateNotificationWindow extends JFrame implements ActionListener {
    private static final long serialVersionUID = 8847497098166369293L;

    private static final String ACT_CMD_CLOSE = "ACT_CMD_CLOSE";

    private static UpdateNotificationWindow frame;
    private static UpdateNotificationPanel mainPanel;

    private JCheckBox ckbNaver;

    private UpdateNotificationWindow(Component parent) {
        setTitle(_RStr.TITLE_UPDATE_LIST.get());
        setIconImage(PlugInManager.getAppIcon());
        setResizable(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        add(mainPanel = new UpdateNotificationPanel());

        JPanel ctrPanel = new JPanel();
        BoxLayout ctrLayout = new BoxLayout(ctrPanel, BoxLayout.Y_AXIS);
        ctrPanel.setLayout(ctrLayout);
        ctrPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        ckbNaver = new JCheckBox(_RStr.LABEL_DO_NOT_LOOK_AGAIN.get());
        ctrPanel.add(ckbNaver);

        JButton btnClose = new JButton(_RStr.BTN_CLOSE.get());
        btnClose.setActionCommand(ACT_CMD_CLOSE);
        btnClose.addActionListener(this);

        JPanel btnCtrPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)) {
            private static final long serialVersionUID = -7930379247697419237L;

            @Override
            public Dimension getMaximumSize() {
                Dimension max = super.getMaximumSize();
                max.height = getPreferredSize().height;
                return max;
            }
        };
        btnCtrPanel.setAlignmentX(0.0f);
        btnCtrPanel.add(btnClose);

        ctrPanel.add(btnCtrPanel);

        add(ctrPanel, BorderLayout.SOUTH);

        pack();
        WindowSizeMemorizer.apply(this);
        setLocationRelativeTo(parent);

        KeyStrokeAction.registerKeyStrokeAction(getRootPane(), JComponent.WHEN_IN_FOCUSED_WINDOW,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ACT_CMD_CLOSE, this);
    }

    public static void show(Component parent, UpdateChecker[] list) {
        if (frame == null) frame = new UpdateNotificationWindow(parent);
        mainPanel.addUpdateList(list);
        if (!frame.isShowing()) {
            frame.setVisible(true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        String actCmd = evt.getActionCommand();
        if (actCmd == null) return;

        switch (actCmd) {
            case ACT_CMD_CLOSE:
                if (ckbNaver.isSelected()) {
                    PlugInConfig.setGlobalConfiguration(PlugInConfig.CONFIG_NO_LOOK_UPDATE_POPUP,
                            "true");
                    PlugInManager.saveProperty();
                }
                frame.dispose();
                break;
        }
    }
}
