package gameClient;
import api.DWGraph_Algo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class wow extends JFrame implements ActionListener {
    private static JFrame frame;
    private static JPanel panel;
    private static JLabel user_label, password_label,aware;
    private static JTextField userName_text;
    private static JPasswordField password_text;
    private static JButton submit;
    private int Level;
    private String ID;


    public int getLevel() {
        return Level;
    }

    public String getID() {
        return ID;
    }

    public static api.dw_graph_algorithms buildGraph(String JsonGraph, api.game_service game){
        api.DWGraph_DS_Deserializer deserializer = new api.DWGraph_DS_Deserializer();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(api.directed_weighted_graph.class, deserializer);
        Gson gson = gsonBuilder.create();
        api.directed_weighted_graph graph = gson.fromJson(game.getGraph(), api.directed_weighted_graph.class);
        api.dw_graph_algorithms graphAlgo = new DWGraph_Algo();
        graphAlgo.init(graph);
        return graphAlgo;
    }

    public static void man() throws IOException {
        // Username Label
        frame = new JFrame();
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int width = dimension.width;
        int height = dimension.height;

        frame.setSize(width - 100, height - 100);
        user_label = new JLabel("Level Number :", SwingConstants.CENTER);
//        user_label.setText("User Name :");
        userName_text = new JTextField();
        // Password Label
        password_label = new JLabel("Password :",SwingConstants.CENTER);
//        password_label.setText();
        password_text = new JPasswordField();
        // Submit
        submit = new JButton("SUBMIT");
        //lucky
        aware = new JLabel("stick between [0->23]",SwingConstants.CENTER);
//        aware.setAlignmentX(400);


        panel = new JPanel(new GridLayout(3, 2));
        panel.add(user_label);
        panel.add(userName_text);
        panel.add(password_label);
        panel.add(password_text);
        panel.add(aware);
        panel.add(submit);


        JPanel poo = new JPanel();
        BufferedImage image = ImageIO.read(new File("pokemongo.jpg"));
        JLabel label = new JLabel(new ImageIcon(image));
        poo.add(label);
        //***************************************
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Adding the listeners to components..
        submit.addActionListener(new wow());
        frame.add(panel, BorderLayout.NORTH);
        frame.add(poo, BorderLayout.CENTER);
        frame.setTitle("Please Login Here !");
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        String level = userName_text.getText();
        String password = password_text.getText();
        if(level.equals(""))
            level+="-3";
        int LevelNumber = Integer.parseInt(level);

        if (LevelNumber < 24 && LevelNumber > -1){
            System.out.println(LevelNumber + " --------- " + password);
            Level = LevelNumber;
            ID = password;
            frame.setVisible(false); //you can't see me!
            frame.dispose();
        }
        else{
            JDialog d = new JDialog(frame, "Unvalid Option");

            // create a label
            JLabel l = new JLabel("Please Enter number between [0-23]", SwingConstants.CENTER);

            d.add(l);

            // setsize of dialog
            d.setBounds(frame.getWidth()/2, frame.getHeight()/2,350,200);

            // set visibility of dialog
            d.setVisible(true);
        }
    }

    public static void main(String[] args) throws IOException {
        wow a = new wow();
        a.man();
    }
}

