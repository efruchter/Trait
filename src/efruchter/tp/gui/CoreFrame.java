package efruchter.tp.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import efruchter.tp.TraitProject;
import efruchter.tp.entity.Level;
import efruchter.tp.gui.panels.BehaviorEditor;
import efruchter.tp.gui.panels.OptionPanel;
import efruchter.tp.gui.panels.StatisticsPanel;
import efruchter.tp.gui.panels.level.LevelViewPanel;

public class CoreFrame {
	
	private LevelViewPanel levelView;
	private StatisticsPanel sPanel;
	private BehaviorEditor bEditor;
	private OptionPanel oPanel;
	
	private JButton resetButton;
	private TraitProject project;
	
	/**
	 * Core GUI control panel.
	 * 
	 * @param project
	 *            project controller
	 */
	public CoreFrame(TraitProject project) {
		
		this.project = project;
		
		//Tab view
		JTabbedPane tabbedPane = new JTabbedPane();
		//Level
		tabbedPane.addTab("Level", levelView = new LevelViewPanel(new Level()));
		//Statistics
		tabbedPane.addTab("Statistics", sPanel = new StatisticsPanel());
		//Editor
		//tabbedPane.addTab("Script", bEditor = new BehaviorEditor());
		//Options
		//tabbedPane.addTab("Options", oPanel = new OptionPanel());
		
		//build Frame
		JFrame frame = new JFrame("Control Panel");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Global Controls
		JPanel cont = new JPanel();
		frame.add(cont, BorderLayout.NORTH);
		cont.setLayout(new GridLayout(1, 3));
		//comps
		cont.add(wrapInPanel(resetButton = new JButton("Quick Reset")));
		
		frame.add(tabbedPane, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
		
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				CoreFrame.this.project.resetSim();
			}
		});
	}
	
	public LevelViewPanel getLevelView() {
		return levelView;
	}
	
	public StatisticsPanel getStatisticsPanel() {
		return sPanel;
	}
	
	public BehaviorEditor getBehaviorEditor() {
		return bEditor;
	}
	
	public OptionPanel getOptionsPanel() {
		return oPanel;
	}
	
	private static JPanel wrapInPanel(Component s) {
		JPanel p = new JPanel();
		p.add(s);
		return p;
	}
}
