package efruchter.tp.gui.panels.generegistery;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;

import efruchter.tp.TraitProjectClient;
import efruchter.tp.entity.Level;
import efruchter.tp.gui.panels.level.GeneralEditor;
import efruchter.tp.learning.GeneVector;
import efruchter.tp.learning.GeneVectorIO;

/**
 * INCOMING
 * 
 * @author toriscope
 * 
 */
@SuppressWarnings("serial")
public class VectorViewPanel extends JPanel implements TreeSelectionListener {

	private final JTree tree;
	private final GeneralEditor genEditor;

	public VectorViewPanel(final Level level) {

		setLayout(new BorderLayout());
		// Make tree
		tree = new JTree();
		tree.setExpandsSelectedPaths(true);
		tree.setEditable(false);
		tree.setModel(new ExplorationVectorTreeModel(new GeneVector()));

		// Add to panel
		final JScrollPane pane = new JScrollPane(tree);
		pane.setBorder(BorderFactory.createTitledBorder("Genes"));
		pane.setPreferredSize(new Dimension(500, 500));
		add(pane, BorderLayout.CENTER);

		// General editor
		add(genEditor = new GeneralEditor(tree), BorderLayout.SOUTH);

		tree.addTreeSelectionListener(this);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Username editor
		add(new JPanel() {
			{
				setBorder(BorderFactory.createTitledBorder("Username"));
				final JLabel a = new JLabel(TraitProjectClient.PREFERENCES.get("username", "No Username set!"));
				add(a);
				final JButton b = new JButton("Set");
				add(b);
				b.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						String username = JOptionPane
						        .showInputDialog("Please set a username. This username will be used\nwhen sending your data to the server.");
						if (username != null && !username.isEmpty()) {
							TraitProjectClient.PREFERENCES.put("username", username);
							a.setText(TraitProjectClient.PREFERENCES.get("username", "No Username set!"));
						}
					}
				});
			}
		}, BorderLayout.NORTH);
	}

	public void setLevel(final Level level) {
		tree.setModel(new ExplorationVectorTreeModel(GeneVectorIO.getExplorationVector()));
		genEditor.setEditing(null);
	}

	@Override
	public void valueChanged(final TreeSelectionEvent arg0) {
		if (tree.getSelectionCount() > 0) {
			genEditor.setEditing(tree.getSelectionPath());
		}

	}
}
