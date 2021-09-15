import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

//Jelena Jovanovic, Kristina Savic, Barbara Krstic
/* Za socket potrebna nam je IP adresa i port
 *  Nakon pokretanjem servera u konzoli dobijamo poruku da je servrer spreman za klijente. Kada se klijent poveze,
 *  server salje zahtev klijentu da unese svoj nadimak za chat.
 *  Ali da bi se klijent povezao pre toga on mora uneti IP adresu na kome se nalazi server.
 *  U ovom slucaju to je '127.0.0.1' posto se server nalazi na istom racunaru odnosno na localhost.
 */

public class ChatServer {
	// Dodajemo port na koji server odgovara. Ovaj port mora biti isti i kod klijenta da bi mogao da se poveze na ovaj server.     
    private static final int PORT = 992;
  

    // Pamtimo imena korisnika.
    private static HashSet<String> names = new HashSet<String>();//vreme dodavanja, brisanja O(1)

    // Ovde cemo da pamtimo izlazne tokove korisnika.
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    /*
     * U main f-ji pravimo socket za server koji sadrzi isti port kao kod klijenta kako bi znao s kim treba da se poveze,
     * a zatim handler koji ce da prihvata zahteve od klijenta.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Chat server je pokrenut! Mozete se pridruziti chat sobi...");
        ServerSocket listener = new ServerSocket(PORT){
        	@Override
        	protected void finalize() throws IOException{// pri skupljanju od 
        		// strane garbage kolektora, zove se ova metoda
        		// i zatvara se prikljucnica
        		this.close();
        	}
        	
        };
       
            while (true) {
                new Handler(listener.accept()).start();
            }
         
    }

    
    // Pravimo klasu Handler koja sluzi za komunikaciju sa klijentom i ispisivanje njegovih poruka.
    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        
        // F-ja koja prihvata ime korisnika a zatim prikazuje chat sobu gde ce korisnik unositi poruke.       
        public void run() {
            try {

                // Kreira strim karaktera za soket.
                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Trazimo unos korisnickog imena.
                while (true) {//sve dok ime nije jedinstveno vrti petlju
                    out.println("PostaviIme");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {//zakljucava obj names
                        if (!names.contains(name)) {
                            names.add(name);
                            break;
                        }
                    }
                }

                // Cuvamo korisnicko ime kako bi kasnije mogao da primi poruke.
                out.println("ImeJePrihvaceno");
                writers.add(out);

                // Prihvatamo poruke od korisnika i prikazujemo ih.
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    for (PrintWriter writer : writers) {
                        writer.println("Poruka " + name + ": " + input);
                    }
                }
            } catch (IOException e) {
                System.out.println("Klijent " + name + " je napustio chat sobu ");
            } finally {
                // Ukoliko je klijent izasao iz chat sobe brisemo njegovo ime i poruke koje smo pamtili.
            	// (Poruke ostaju vidljive u cetu, ovde ih brisemo iz promenljive koja ih pamtila)
                if (name != null) {
                    names.remove(name);
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    } 
}
