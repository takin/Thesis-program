package SemanticQA.userinterface;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;


@SuppressWarnings("serial")
public class Mainpage extends JFrame {

	JButton b;
	JTextArea textArea;
	
	public Mainpage() {
		
		setLayout(new FlowLayout());
		
		b = new JButton("testing");
		textArea = new JTextArea(10, 20);
		
		add(b);
		add(textArea);
		
		setSize(400, 300);
		
		
		setVisible(true);
		
	}
	
	public static void main(String args[]) {
		
		new Mainpage();
		
	}
	
}
