import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.*;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;

public class ClientGUI extends Thread{

  final JTextPane jtextFilDiscu = new JTextPane();
  final JTextPane jtextListUsers = new JTextPane();
  final JTextField jtextInputChat = new JTextField();
  private String oldMsg = "";
  private Thread read;
  private String serverName;
  private int PORT;
  private String name;
  BufferedReader input;
  PrintWriter output;
  Socket server;

  public ClientGUI() {
    this.serverName = "localhost";
    this.PORT = 8080;
    this.name = "";

    String fontfamily = "Arial, sans-serif";
    Font font = new Font(fontfamily, Font.PLAIN, 15);

    final JFrame jfr = new JFrame("Chat");
    jfr.getContentPane().setLayout(null);
    jfr.setSize(700, 500);
    jfr.setResizable(false);
    jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    jtextFilDiscu.setBounds(25, 25, 490, 320);
    jtextFilDiscu.setFont(font);
    jtextFilDiscu.setMargin(new Insets(6, 6, 6, 6));
    jtextFilDiscu.setEditable(false);
    JScrollPane jtextFilDiscuSP = new JScrollPane(jtextFilDiscu);
    jtextFilDiscuSP.setBounds(25, 25, 490, 320);

    jtextFilDiscu.setContentType("text/html");
    jtextFilDiscu.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

    jtextListUsers.setBounds(520, 25, 156, 320);
    jtextListUsers.setEditable(true);
    jtextListUsers.setFont(font);
    jtextListUsers.setMargin(new Insets(6, 6, 6, 6));
    jtextListUsers.setEditable(false);
    JScrollPane jsplistuser = new JScrollPane(jtextListUsers);
    jsplistuser.setBounds(520, 25, 156, 320);

    jtextListUsers.setContentType("text/html");
    jtextListUsers.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

    // user input field
    jtextInputChat.setBounds(0, 350, 400, 50);
    jtextInputChat.setFont(font);
    jtextInputChat.setMargin(new Insets(6, 6, 6, 6));
    final JScrollPane jtextInputChatSP = new JScrollPane(jtextInputChat);
    jtextInputChatSP.setBounds(25, 350, 650, 50);

    // send button
    final JButton jsbtn = new JButton("Send");
    jsbtn.setFont(font);
    jsbtn.setBounds(575, 410, 100, 35);
    
    // Disconnect button
    final JButton jsbtndeco = new JButton("Disconnect");
    jsbtndeco.setFont(font);
    jsbtndeco.setBounds(25, 410, 130, 35);

    // send chat record to the user's email address

    final JButton jsbtn2 = new JButton("Email Chat");
    jsbtn2.setFont(font);
    jsbtn2.setBounds(180,410,130,35);


    jtextInputChat.addKeyListener(new KeyAdapter() {
      // send message on Enter
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          sendMessage();
        }

        // Get last message typed
        if (e.getKeyCode() == KeyEvent.VK_UP) {
          String currentMessage = jtextInputChat.getText().trim();
          jtextInputChat.setText(oldMsg);
          oldMsg = currentMessage;
        }

        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
          String currentMessage = jtextInputChat.getText().trim();
          jtextInputChat.setText(oldMsg);
          oldMsg = currentMessage;
        }
      }
    });

    // send button action
    jsbtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        sendMessage();
      }
    });

    jsbtn2.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent ae){
            JavaRunCommand ctt = new JavaRunCommand();
            ctt.convertToText();
            JavaRunCommand jrc = new JavaRunCommand();
            jrc.RunCmd();

          
        }
    });

    

    

    // Connection view
    final JTextField jtfName = new JTextField(this.name);
    final JTextField jtfport = new JTextField(Integer.toString(this.PORT));
    final JTextField jtfAddr = new JTextField(this.serverName);
    final JButton jcbtn = new JButton("Connect");

    // To check if those fields are not empty
    jtfName.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));
    jtfport.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));
    jtfAddr.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));

    // position of the modules
    jcbtn.setFont(font);
    jtfAddr.setBounds(25, 380, 135, 40);
    jtfName.setBounds(375, 380, 135, 40);
    jtfport.setBounds(200, 380, 135, 40);
    jcbtn.setBounds(575, 380, 100, 40);

    jtextFilDiscu.setBackground(Color.LIGHT_GRAY);
    jtextListUsers.setBackground(Color.LIGHT_GRAY);

    // elements
    jfr.add(jcbtn);
    jfr.add(jtextFilDiscuSP);
    jfr.add(jsplistuser);
    jfr.add(jtfName);
    jfr.add(jtfport);
    jfr.add(jtfAddr);
    jfr.setVisible(true);


    // some basic information during chat!
    appendToPane(jtextFilDiscu, "<h4>Hey wassup guys!Welcome to our chat engine!:</h4>"
        +"<ul>"
        +"<li><b>@nickname</b> This can be used as your 'nickname'</li>"
        +"<li><b>;)</b> Implementation of smileys</li>"
        +"</ul><br/>");

    // During connection
    jcbtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        try {
          name = jtfName.getText();
          String port = jtfport.getText();
          serverName = jtfAddr.getText();
          PORT = Integer.parseInt(port);

          appendToPane(jtextFilDiscu, "<span>Connecting to " + serverName + " on port " + PORT + "...</span>");
          server = new Socket(serverName, PORT);

          appendToPane(jtextFilDiscu, "<span>Connected to " +
              server.getRemoteSocketAddress()+"</span>");

          input = new BufferedReader(new InputStreamReader(server.getInputStream()));
          output = new PrintWriter(server.getOutputStream(), true);

          // sending nickname to the server
          output.println(name);

          // creating new read thread
          read = new Read();
          read.start();
          jfr.remove(jtfName);
          jfr.remove(jtfport);
          jfr.remove(jtfAddr);
          jfr.remove(jcbtn);
          jfr.add(jsbtn);
          jfr.add(jsbtn2);
          jfr.add(jtextInputChatSP);
          jfr.add(jsbtndeco);
          jfr.revalidate();
          jfr.repaint();
          jtextFilDiscu.setBackground(Color.WHITE);
          jtextListUsers.setBackground(Color.WHITE);
        } catch (Exception ex) {
          appendToPane(jtextFilDiscu, "<span>Could not connect to the Server!</span>");
          JOptionPane.showMessageDialog(jfr, ex.getMessage());
        }
      }

    });

    // on disconnection
    jsbtndeco.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent ae) {
        jfr.add(jtfName);
        jfr.add(jtfport);
        jfr.add(jtfAddr);
        jfr.add(jcbtn);
        jfr.remove(jsbtn);
        jfr.remove(jtextInputChatSP);
        jfr.remove(jsbtndeco);
        jfr.revalidate();
        jfr.repaint();
        read.interrupt();
        jtextListUsers.setText(null);
        jtextFilDiscu.setBackground(Color.LIGHT_GRAY);
        jtextListUsers.setBackground(Color.LIGHT_GRAY);
        appendToPane(jtextFilDiscu, "<span>Connection closed.</span>");
        output.close();
      }
    });

  }

  // For checking if all the fields are not empty
  public class TextListener implements DocumentListener{
    JTextField jtf1;
    JTextField jtf2;
    JTextField jtf3;
    JButton jcbtn;

    public TextListener(JTextField jtf1, JTextField jtf2, JTextField jtf3, JButton jcbtn){
      this.jtf1 = jtf1;
      this.jtf2 = jtf2;
      this.jtf3 = jtf3;
      this.jcbtn = jcbtn;
    }

    public void changedUpdate(DocumentEvent e) {}

    public void removeUpdate(DocumentEvent e) {
      if(jtf1.getText().trim().equals("") ||
          jtf2.getText().trim().equals("") ||
          jtf3.getText().trim().equals("")
          ){
        jcbtn.setEnabled(false);
      }else{
        jcbtn.setEnabled(true);
      }
    }
    public void insertUpdate(DocumentEvent e) {
      if(jtf1.getText().trim().equals("") ||
          jtf2.getText().trim().equals("") ||
          jtf3.getText().trim().equals("")
          ){
        jcbtn.setEnabled(false);
      }else{
        jcbtn.setEnabled(true);
      }
    }

  }

  // sending messages
  public void sendMessage() {
    try {
      String message = jtextInputChat.getText().trim();
      if (message.equals("")) {
        return;
      }
      this.oldMsg = message;
      output.println(message);
      jtextInputChat.requestFocus();
      jtextInputChat.setText(null);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(null, ex.getMessage());
      System.exit(0);
    }
  }

  public static void main(String[] args) throws Exception {
    ClientGUI client = new ClientGUI();
  }

  // For reading new incoming messages
  class Read extends Thread {
    public void run() {
      String message;
      while(!Thread.currentThread().isInterrupted()){
        try {
          message = input.readLine();
          if(message != null){
            if (message.charAt(0) == '[') {
              message = message.substring(1, message.length()-1);
              ArrayList<String> ListUser = new ArrayList<String>(
                  Arrays.asList(message.split(", "))
                  );
              jtextListUsers.setText(null);
              for (String user : ListUser) {
                appendToPane(jtextListUsers, "@" + user);
              }
            }else{
              appendToPane(jtextFilDiscu, message);
            }
          }
        }
        catch (IOException ex) {
          System.err.println("Failed to parse incoming message");
        }
      }
    }
  }

  // sending html to the chat window
  private void appendToPane(JTextPane tp, String msg){
    HTMLDocument doc = (HTMLDocument)tp.getDocument();
    HTMLEditorKit editorKit = (HTMLEditorKit)tp.getEditorKit();
    try {
      editorKit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
      tp.setCaretPosition(doc.getLength());
    } catch(Exception e){
      e.printStackTrace();
    }
  }
}

class JavaRunCommand {

  public void convertToText()
  {
      try
      {
          // Reading the object from a file 
          FileInputStream file = new FileInputStream("messages.ser"); 
          ObjectInputStream in = new ObjectInputStream(file);
          // Method for deserialization of object 
          ArrayList<message> object1= new ArrayList<>();
          object1 = (ArrayList)in.readObject(); // type casting it 
          in.close(); 
          file.close(); 
          //System.out.println("Object has been deserialized "); 
          //System.out.println(object1.chatList);
          //--------------------Writing in file --------------------//
          FileWriter fileWriter =new FileWriter("messages.txt");
          // Always wrap FileWriter in BufferedWriter.
          BufferedWriter bufferedWriter =new BufferedWriter(fileWriter);
          // Note that write() does not automatically
          // append a newline character.
          /*bufferedWriter.write("Hello there,");
          bufferedWriter.write(" here is some text.");
          bufferedWriter.newLine();
          bufferedWriter.write("We are writing");
          bufferedWriter.write(" the text to the file.");
          */
          // Always close files.
          // bufferedWriter.write(i.user+" : "+i.messageStr);
          

          Date date = new Date();
          long time = date.getTime();
          Timestamp ts = new Timestamp(time);

          String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new java.util.Date());

          
          bufferedWriter.write(timeStamp);
          bufferedWriter.newLine();

          for(message i :object1)
          {
            bufferedWriter.write(i.user+" : "+i.messageStr);
            bufferedWriter.newLine();
          }
          bufferedWriter.close();
      } 
        
      catch(IOException ex) 
      { 
          System.out.println("IOException is caught"); 
      } 
        
      catch(ClassNotFoundException ex) 
      { 
          System.out.println("ClassNotFoundException is caught"); 
      }
       
  }
    //   public static String GetTimeStamp()
    // {
    //   // Date date= new Date();
    //   Date date = new Date();
    //   long time = date.getTime();
    //   // System.out.println("Time in Milliseconds: " + time);

    //   Timestamp ts = new Timestamp(time);

    //   date.setTime(ts.getTime());
    //   String formattedDate = new SimpleDateFormat("yyyyMMdd").format(date);
    //   return formattedDate;
    //   // System.out.println("CurrGetTimeStampent Time Stamp: " + ts);
        
    // }
    // public static String timestampAsString(Timestamp timestamp) {
    //   return DateTimeFormat.forPattern("yyyyMMdd").print(timestamp.getTime());
    // }

    public void RunCmd() {

        String s = null;

        try {
            
	    // run the Unix commands to execute
            // using the Runtime exec method to:
            Process p = Runtime.getRuntime().exec("python3 email2.py");
            
            BufferedReader stdInput = new BufferedReader(new 
                 InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new 
                 InputStreamReader(p.getErrorStream()));

            // read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }
            
            // read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }
            
            // System.exit(0);
        }
        catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            // System.exit(-1);
        }
    }

    
}
