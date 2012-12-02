package efruchter.tp.gui.panels;

import java.awt.BorderLayout;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jsyntaxpane.DefaultSyntaxKit;

/**
 * Just goofing around with Tabbed panes.
 * 
 * @author toriscope
 * 
 */
@SuppressWarnings("serial")
public class BehaviorEditor extends JPanel {
	
	static {
		DefaultSyntaxKit.initKit();
	}
	
	private ScriptSelectorPanel sPan;
	
	public BehaviorEditor() {
		
		setLayout(new BorderLayout());
		
		sPan = new ScriptSelectorPanel();
		add(sPan, BorderLayout.NORTH);
		
		//Center Editor
		JEditorPane codeEditor = new JEditorPane();
		JScrollPane scrPane = new JScrollPane(codeEditor);
		add(scrPane, BorderLayout.CENTER);
		codeEditor.setContentType("text/python");
		codeEditor.setText("public static void main(String[] args) {\n}");
	}
	
	private class ScriptSelectorPanel extends JPanel {
		public ScriptSelectorPanel() {
			add(new JLabel("Edit selected behavior:"));
		}
	}
}
