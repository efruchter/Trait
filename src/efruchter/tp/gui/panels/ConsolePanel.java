package efruchter.tp.gui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ConsolePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final JTextArea textArea;
	private final JCheckBox displayCheckBox;
	private long count;

	public ConsolePanel() {

		this.setLayout(new BorderLayout());
		JScrollPane pane = new JScrollPane(textArea = new JTextArea());
		pane.setPreferredSize(new Dimension(400, 400));
		textArea.setEditable(false);
		this.add(BorderLayout.CENTER, pane);

		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				count = 0;
				textArea.setText("");
			}
		});

		displayCheckBox = new JCheckBox("Show Output", true);

		JPanel p = new JPanel();
		p.add(clearButton);
		p.add(displayCheckBox);

		this.add(BorderLayout.SOUTH, p);

		count = 0;

		OutputStream out = new OutputStream() {
			@Override
			public void write(int arg0) throws IOException {
				if (displayCheckBox.isSelected())
					textArea.append(String.valueOf((char) arg0));
			}
		};
		
		PrintStream pri = new PrintStream(out, true);

		System.setOut(pri);
		System.setErr(pri);
	}

	public void println(final String output) {
		if (displayCheckBox.isSelected()) {
			textArea.append(count++ + ":\t");
			textArea.append(output);
			textArea.append("\n\n");
		}
	}

	public static void println(final Exception output) {
		StringWriter se = new StringWriter();
		output.printStackTrace(new PrintWriter(se));
	}
}
