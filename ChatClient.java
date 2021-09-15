import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatClient {
	 BufferedReader in;
	    PrintWriter out;
	    JFrame frame = new JFrame("Chat Soba!");
	    JTextField textField = new JTextField(40);
	    JTextArea messageArea = new JTextArea(8, 40);
	    
	    Socket m_socket = null;

	    //izradjuje klijenta postavljanjem gui-a tj grafickog korisnickog interfejsa i registracijom
	    public ChatClient() {
	        //pravljenje gui-a
	        textField.setEditable(false);
	        messageArea.setEditable(false);
	        frame.getContentPane().add(textField, "North");
	        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
	        frame.pack();

	        textField.addActionListener(new ActionListener() {
	            //odgovara na pritisak zeljenog tastera u text polju slanjem sadrzaja na server
				//onda je jasno tekstualno podrucje u pripremi za sledecu poruku
	            public void actionPerformed(ActionEvent e) {
	                out.println(textField.getText());
	                textField.setText("");
	            }
	        });
	    }

	    //poziva i vraca ime adrese servera
	    private String getServerAddress() {
	        return JOptionPane.showInputDialog(
	            frame,
	            "Unesite IP adresu servera:",
	            "Dobrodosli!",
	            JOptionPane.QUESTION_MESSAGE,null,null,"127.0.0.1").toString();
	    }

	    //pozovi i vrati zeljeno ime ekrana
	    private String getName() {
	        return JOptionPane.showInputDialog(
	            frame,
	            "Unesite korisnicko ime:",
	            "Korisnicko ime!",
	            JOptionPane.PLAIN_MESSAGE);
	    }

	    //povezuje se na server i ulazi u procesnu petlju
	    private void run() throws IOException {
	        //uspostavlja se veza i inicijalizuju se tokovi
	        String serverAddress = getServerAddress();
	        m_socket = new Socket(serverAddress, 992);
	        in = new BufferedReader(new InputStreamReader(
	        		m_socket.getInputStream()));
	        out = new PrintWriter(m_socket.getOutputStream(), true);
	        //obradjuju se sve poruke sa servera, prema protokolu
	        while (true) {
	            String line = in.readLine();
	            if (line.startsWith("PostaviIme")) {
	                out.println(getName());
	            } else if (line.startsWith("ImeJePrihvaceno")) {
	                textField.setEditable(true);
	               // messageArea.append("ImeJePrihvaceno\n");
	            } else if (line.startsWith("Poruka")) {
	                messageArea.append(line.substring(7) + "\n");
	            }
	        }
	        
	        
	    }

	    //upravlja se klijentom kao aplikacijom sa okvirom koja se moze zatvoriti
	    public static void main(String[] args) throws Exception {
	        ChatClient client = new ChatClient();
	        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        client.frame.setVisible(true);
	        
	        try {
	        	client.run();
	        } catch (IOException e) {
	        	client.m_socket.close();
	        }
	    
	    }
}
