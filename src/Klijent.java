import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Base64;

public class Klijent extends JFrame{

    private JPanel PanelMain;
    private JTextField textField1;
    private JButton posaljiButton;
    private JTextArea textArea1;
    private JLabel label1;

    private final static String HOST = "localhost";
    private final static int PORT_NAME = 13;

    public static void stampaj_poruku(Klijent klijent, String sadrzaj){
        klijent.textArea1.append(" Server:   " + sadrzaj + "\n" + "\n");
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

    public static void sacuvaj_sliku(Klijent klijent, String naziv, String slika_string) throws IOException {


        String niz[] = naziv.split("-velicina-");
        int size = Integer.parseInt(niz[1]);
        byte[] slika_byte = new byte[size];
        slika_byte = Base64.getDecoder().decode(slika_string);

        String novi_fajl = "C:\\Users\\Darko\\IdeaProjects\\GUITest\\src\\Klijent-Slike\\" + niz[0];

        ByteArrayInputStream bais = new ByteArrayInputStream(slika_byte);
        BufferedImage image = ImageIO.read(bais);

        File outputfile = new File(novi_fajl);
        ImageIO.write(image, tip_slike(naziv), outputfile);

        klijent.textArea1.append(" Server:   " + niz[0] + "\n" + "\n");
    }

    public Klijent(){
        add(PanelMain);
        setSize(500,500);
        PanelMain.setBorder(new EmptyBorder(10, 10, 10, 10));
        setTitle("Klijent");
        setLocation(800,100);
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
        label1.setFont(f1);

    }

    public static void main(String[] args){

        Klijent klijent = new Klijent();
        klijent.setVisible(true);

        Socket s = null;

        InputStream in = null;
        InputStreamReader isr = null;
        BufferedReader bfr = null;

        OutputStream out = null;
        OutputStreamWriter osw = null;
        BufferedWriter bfw = null;

        try{

            s = new Socket(HOST, PORT_NAME);

            in = s.getInputStream();
            isr = new InputStreamReader(in, "UTF-8");
            bfr = new BufferedReader(isr);

            out = s.getOutputStream();
            osw = new OutputStreamWriter(out, "UTF-8");
            bfw = new BufferedWriter(osw);


            while(true){

                klijent.posaljiButton.setEnabled(true);
                klijent.textField1.setEditable(true);

                String poruka = klijent.textField1.getText();
                while(!klijent.posaljiButton.getModel().isPressed()) {
                    poruka = klijent.textField1.getText();
                }

                if(poruka.endsWith(".jpg") || poruka.endsWith(".png") ||
                        poruka.endsWith(".jpg\"") || poruka.endsWith(".png\"")) {
                    poruka = ukloni_navodnike(poruka);
                    posalji_sliku(poruka, bfw);
                }
                else if(poruka.equals("quit")) {
                    // Zatvaranje soketa
                    bfw.write("quit" + '\n' + '\n');

                    klijent.textArea1.append(" Soket je zatvoren.");
                    klijent.textField1.setText("");

                    s.close();
                    bfw.close();
                    osw.close();
                    out.close();

                    bfr.close();
                    isr.close();
                    in.close();
                }
                else {
                    posalji_poruku(bfw, poruka);
                }

                klijent.posaljiButton.setEnabled(false);
                klijent.textField1.setEditable(false);

                klijent.textArea1.append(" Klijent:   " + poruka + '\n' + '\n');
                klijent.textField1.setText("");

                String dolazni_tip = bfr.readLine();
                String dolazna_lokacija = bfr.readLine();
                String dolazni_sadrzaj = bfr.readLine();

                if(dolazni_tip.equals("TEXT"))
                    stampaj_poruku(klijent, dolazni_sadrzaj);
                else if(dolazni_tip.equals("IMAGE")) {
                    sacuvaj_sliku(klijent, dolazna_lokacija, dolazni_sadrzaj);
                }

            }

        } catch(SocketException z){
            System.err.println("Soket je zatvoren.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if (s != null)
                    s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }



    }

}
