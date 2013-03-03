package efruchter.tp.gui;

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
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Console {

	private static JFrame frame;
	private static JTextArea textArea;
	private static JCheckBox displayCheckBox;
	private static long count;
	
	public static void init() {
		OutputStream out = new OutputStream() {
			@Override
			public void write(int arg0) throws IOException {
				if (displayCheckBox.isSelected())
					textArea.append(String.valueOf((char) arg0));
			}
		};

		System.setErr(new PrintStream(out, true));
		System.setOut(new PrintStream(out, true));
		
		show();
	}
	
	public static void setTitle(final String title) {
		if (frame != null) {
			frame.setTitle(title);
		}
	}

	private static void show() {
		if (frame == null) {
			frame = new JFrame("Console");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLayout(new BorderLayout());
			JScrollPane pane = new JScrollPane(textArea = new JTextArea());
			pane.setPreferredSize(new Dimension(400, 400));
			textArea.setEditable(false);
			frame.add(BorderLayout.CENTER, pane);

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

			frame.add(BorderLayout.SOUTH, p);
			frame.pack();
			frame.setVisible(true);

			count = 0;
		}
	}

	public static void println(final String output) {
		show();
		if (displayCheckBox.isSelected()) {
			textArea.append(count++ + ":\t");
			textArea.append(output);
			textArea.append("\n\n");
		}
	}

	public static void println(final Exception output) {
		StringWriter se = new StringWriter();
		output.printStackTrace(new PrintWriter(se));
		println(se.toString());
	}
}
