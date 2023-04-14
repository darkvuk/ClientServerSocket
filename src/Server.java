import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;

public class Server extends JFrame{

    private JPanel PanelMain;
    private JTextField textField1;
    private JButton posaljiButton;
    private JTextArea textArea1;
    private JLabel Label1;

    private final static String HOST = "localhost";
    private final static int PORT_NAME = 13;


    public static void stampaj_poruku(Server server, String sadrzaj){
        server.textArea1.append(" Klijent:   " + sadrzaj + "\n" + "\n");
    }

    public static String naziv_slike(String lokacija){

        if(lokacija.contains("\\"))
            lokacija = lokacija.replace("\\","/");
        if (lokacija.contains("\""))
            lokacija = lokacija.replace("\"","");
        String[] parts = lokacija.split("/");
        return parts[parts.length-1];
    }

    public static String ukloni_navodnike(String lokacija){
        return lokacija.replace("\"","");
    }

    public static String tip_slike(String naziv){
        if(naziv.endsWith("jpg") || naziv.endsWith("jpg\""))
            return "jpg";

        return "png";
    }

    public static void posalji_sliku(String poruka, BufferedWriter bfw){

        try {
            File myFile = new File (poruka);
            byte [] niz_bajta  = new byte [(int)myFile.length()];
            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(niz_bajta,0,niz_bajta.length);

            String bajtistring = Base64.getEncoder().encodeToString(niz_bajta);
            String naziv_slike = naziv_slike(poruka);
            String br_bajta = Integer.toString(niz_bajta.length);

            bfw.write("IMAGE" + '\n' + naziv_slike + "-velicina-" + br_bajta + '\n' + bajtistring);
            bfw.newLine();
            bfw.flush();

            bis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void posalji_poruku(BufferedWriter bfw, String poruka){
        try {
            bfw.write("TEXT" + '\n' + '\n' + poruka);
            bfw.newLine();
            bfw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sacuvaj_sliku(Server server, String naziv, String slika_string) throws IOException {
        String niz[] = naziv.split("-velicina-");
        int size = Integer.parseInt(niz[1]);
        byte[] slika_byte = new byte[size];
        slika_byte = Base64.getDecoder().decode(slika_string);

        String novi_fajl = "C:\\Users\\Darko\\IdeaProjects\\GUITest\\src\\Server-Slike\\" + niz[0];

        ByteArrayInputStream bais = new ByteArrayInputStream(slika_byte);
        BufferedImage image = ImageIO.read(bais);

        File outputfile = new File(novi_fajl);
        ImageIO.write(image, tip_slike(naziv), outputfile);

        server.textArea1.append(" Klijent:   " + niz[0] + "\n" + "\n");
    }

    public Server(){
        add(PanelMain);
        setSize(500,500);
        PanelMain.setBorder(new EmptyBorder(10, 10, 10, 10));
        setTitle("Server");
        setLocation(200,100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textArea1.setEditable(false);

        textField1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                posaljiButton.doClick();
                posaljiButton.setEnabled(false);
                textField1.setEditable(false);
            }
        });

        Font f1 = new Font("Arial", Font.PLAIN, 14);
        textArea1.setFont(f1);
        posaljiButton.setFont(f1);
        textField1.setFont(f1);
        Label1.setFont(f1);


    }

    public static void main(String[] args){
        Server server = new Server();
        server.setVisible(true);

        ServerSocket ss = null;

        InputStream in = null;
        InputStreamReader isr = null;
        BufferedReader bfr = null;

        OutputStream out = null;
        OutputStreamWriter osw = null;
        BufferedWriter bfw = null;

        try {
            ss = new ServerSocket(PORT_NAME);

            while(true){

                Socket s = ss.accept();

                in = s.getInputStream();
                isr = new InputStreamReader(in, "UTF-8");
                bfr = new BufferedReader(isr);

                out = s.getOutputStream();
                osw = new OutputStreamWriter(out, "UTF-8");
                bfw = new BufferedWriter(osw);

                while(true){

                    String dolazni_tip = bfr.readLine();
                    String dolazna_lokacija = bfr.readLine();
                    String dolazni_sadrzaj = bfr.readLine();

                    if(dolazni_tip.equals("TEXT"))
                        stampaj_poruku(server, dolazni_sadrzaj);
                    else if(dolazni_tip.equals("quit")){
                        s.close();
                        ss.close();
                    }
                    else if(dolazni_tip.equals("IMAGE")) {
                        sacuvaj_sliku(server, dolazna_lokacija, dolazni_sadrzaj);
                    }

                    server.posaljiButton.setEnabled(true);
                    server.textField1.setEditable(true);

                    String poruka = server.textField1.getText();
                    while(!server.posaljiButton.getModel().isPressed()){
                        poruka = server.textField1.getText();
                    }

                    if(poruka.endsWith(".jpg") || poruka.endsWith(".png") ||
                            poruka.endsWith(".jpg\"") || poruka.endsWith(".png\"")) {
                        poruka = ukloni_navodnike(poruka);
                        posalji_sliku(poruka, bfw);
                    }
                    else {
                        posalji_poruku(bfw, poruka);
                    }

                    server.posaljiButton.setEnabled(false);
                    server.textField1.setEditable(false);

                    server.textArea1.append(" Server:   " + poruka + '\n' + '\n');
                    server.textField1.setText("");
                }
            }
        } catch(NullPointerException n){
            System.err.println("Soket je zatvoren.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                server.textArea1.append(" Soket je zatvoren.");
                server.textField1.setEditable(false);

                JOptionPane.showMessageDialog(server, "Soket je zatvoren",
                        "Upozorenje", JOptionPane.CLOSED_OPTION);

                bfw.close();
                osw.close();
                out.close();

                bfr.close();
                isr.close();
                in.close();

                if (ss != null)
                    ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }




    }

}
