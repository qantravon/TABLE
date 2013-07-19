package processing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Main {
	public static void main(String[] args) throws IOException{
		final Stream testStream = new Stream();
		JLabel name = new JLabel("Enter File Name");
		name.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		final JTextField nameField = new JTextField();
		JButton confirm = new JButton("Confirm");
		confirm.setAlignmentX(JButton.LEFT_ALIGNMENT);
		final JLabel completeLabel = new JLabel(" ");
		completeLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		
		JFrame interfaceFrame = new JFrame("ImageProcessor");
		JPanel interfacePanel = new JPanel();
		
		confirm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				try {
					String imageFile = nameField.getText();
					System.out.println(imageFile);
					testStream.stream(imageFile); 	//Stream call
					completeLabel.setText("Completed!");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		BoxLayout b1 = new BoxLayout(interfacePanel, BoxLayout.Y_AXIS);
		interfacePanel.setLayout(b1);
		interfacePanel.add(name);
		interfacePanel.add(nameField); 
		interfacePanel.add(confirm); 
		interfacePanel.add(completeLabel);
		
		interfaceFrame.setSize(300, 150);
		interfaceFrame.getContentPane().add(interfacePanel, BorderLayout.CENTER);
		interfaceFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		interfaceFrame.setVisible(true);
	}
}
