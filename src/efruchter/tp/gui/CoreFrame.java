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
import efruchter.tp.gui.panels.StatisticsPanel;
import efruchter.tp.gui.panels.generegistery.VectorViewPanel;

public class CoreFrame {
	
	//private final LevelViewPanel levelView;
	private final VectorViewPanel vectorView;
	private final StatisticsPanel sPanel;
	//private final BehaviorEditor bEditor;
	//private final OptionPanel oPanel;
	
	private final JButton resetButton;
	private final TraitProject project;
	
	/**
	 * Core GUI control panel.
	 * 
	 * @param project
	 *            project controller
	 */
	public CoreFrame(final TraitProject project) {
		
		this.project = project;
		
		//Tab view
        final JTabbedPane tabbedPane = new JTabbedPane();
		//Level
		//tabbedPane.addTab("Level", levelView = new LevelViewPanel(new Level()));
		//Gene Reg.
		tabbedPane.addTab("Vector", vectorView = new VectorViewPanel(new Level()));
		//Statistics
		tabbedPane.addTab("Statistics", sPanel = new StatisticsPanel());
		//Editor
		//tabbedPane.addTab("Script", bEditor = new BehaviorEditor());
		//Options
		//tabbedPane.addTab("Options", oPanel = new OptionPanel());
		
		//deactivate unused
		//tabbedPane.setEnabledAt(2, false);
		//tabbedPane.setEnabledAt(3, false);
		
		//build Frame
        final JFrame frame = new JFrame("Control Panel");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Global Controls
        final JPanel cont = new JPanel();
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
	
	private static JPanel wrapInPanel(final Component s) {
		JPanel p = new JPanel();
		p.add(s);
		return p;
	}
	
	public void setLevel(final Level level) {
		//levelView.setLevel(level);
		vectorView.setLevel(level);
	}
	
	public StatisticsPanel getStatisticsPanel() {
		return sPanel;
	}
}
