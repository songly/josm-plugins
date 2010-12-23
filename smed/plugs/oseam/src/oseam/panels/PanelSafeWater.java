package oseam.panels;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Font;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.Shp;

import java.awt.Cursor;
import java.awt.event.ActionListener;

public class PanelSafeWater extends JPanel {

	private OSeaMAction dlg;
	private ButtonGroup shapeButtons = null;
	private JRadioButton pillarButton = null;
	private JRadioButton sparButton = null;
	private JRadioButton sphereButton = null;
	private JRadioButton barrelButton = null;
	private JRadioButton floatButton = null;
	private ActionListener alShape = null;

	public PanelSafeWater(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		this.add(getPillarButton(), null);
		this.add(getSparButton(), null);
		this.add(getSphereButton(), null);
		this.add(getBarrelButton(), null);
		this.add(getFloatButton(), null);

		shapeButtons = new ButtonGroup();
		shapeButtons.add(pillarButton);
		shapeButtons.add(sparButton);
		shapeButtons.add(sphereButton);
		shapeButtons.add(barrelButton);
		shapeButtons.add(floatButton);
		alShape = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (pillarButton.isSelected()) {
					dlg.mark.setShape(Shp.PILLAR);
					pillarButton.setBorderPainted(true);
				} else {
					pillarButton.setBorderPainted(false);
				}
				if (sparButton.isSelected()) {
					dlg.mark.setShape(Shp.SPAR);
					sparButton.setBorderPainted(true);
				} else {
					sparButton.setBorderPainted(false);
				}
				if (sphereButton.isSelected()) {
					dlg.mark.setShape(Shp.SPHERE);
					sphereButton.setBorderPainted(true);
				} else {
					sphereButton.setBorderPainted(false);
				}
				if (barrelButton.isSelected()) {
					dlg.mark.setShape(Shp.BARREL);
					barrelButton.setBorderPainted(true);
				} else {
					barrelButton.setBorderPainted(false);
				}
				if (floatButton.isSelected()) {
					dlg.mark.setShape(Shp.FLOAT);
					floatButton.setBorderPainted(true);
				} else {
					floatButton.setBorderPainted(false);
				}
				if (dlg.mark != null)
					dlg.mark.paintSign();
			}
		};
		pillarButton.addActionListener(alShape);
		sparButton.addActionListener(alShape);
		sphereButton.addActionListener(alShape);
		barrelButton.addActionListener(alShape);
		floatButton.addActionListener(alShape);
	}

	public void clearSelections() {
		shapeButtons.clearSelection();
		alShape.actionPerformed(null);
	}

	private JRadioButton getPillarButton() {
		if (pillarButton == null) {
			pillarButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PillarButton.png")));
			pillarButton.setBounds(new Rectangle(0, 0, 34, 32));
			pillarButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			pillarButton.setToolTipText(Messages.getString("PillarTip"));
		}
		return pillarButton;
	}

	private JRadioButton getSparButton() {
		if (sparButton == null) {
			sparButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SparButton.png")));
			sparButton.setBounds(new Rectangle(0, 32, 34, 32));
			sparButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			sparButton.setToolTipText(Messages.getString("SparTip"));
		}
		return sparButton;
	}

	private JRadioButton getSphereButton() {
		if (sphereButton == null) {
			sphereButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SphereButton.png")));
			sphereButton.setBounds(new Rectangle(0, 64, 34, 32));
			sphereButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			sphereButton.setToolTipText(Messages.getString("SphereTip"));
		}
		return sphereButton;
	}

	private JRadioButton getBarrelButton() {
		if (barrelButton == null) {
			barrelButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BarrelButton.png")));
			barrelButton.setBounds(new Rectangle(0, 96, 34, 32));
			barrelButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			barrelButton.setToolTipText(Messages.getString("BarrelTip"));
		}
		return barrelButton;
	}

	private JRadioButton getFloatButton() {
		if (floatButton == null) {
			floatButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FloatButton.png")));
			floatButton.setBounds(new Rectangle(0, 128, 34, 32));
			floatButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			floatButton.setToolTipText(Messages.getString("FloatTip"));
		}
		return floatButton;
	}

}