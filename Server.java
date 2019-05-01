import java.io.IOException;
import java.net.Inet4Address; 
import java.net.InetAddress; 
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.awt.Color;
import java.io.*;
import java.util.*;
import java.io.File;

class message implements Serializable
{
  public String user;
  public String messageStr;
  message(String user,String messageStr)
    {
        this.user=user;
        this.messageStr=messageStr;
    }
    public String toString() {
        return this.user + " : " + this.messageStr ;
    }
}
class serialisation implements Serializable
{
    message mObj;
    String fileName;
    serialisation(String fileName)
    {
      this.fileName=fileName;
    }
    ArrayList<message> chatList = new ArrayList<>();
    public boolean addMessage(String User,String tempChat)
    {
        mObj= new message(User,tempChat);
        try
        {
          // if the .ser already exist take the data from it and append the data to it, otherwise make new .ser object
            if(doesExist(fileName))
            {
              ArrayList<message> node = new ArrayList<message>();
              try
              {
                FileInputStream file = new FileInputStream(fileName); 
                ObjectInputStream in = new ObjectInputStream(file);

                // Method for deserialization of object 
                node = (ArrayList)in.readObject(); // type casting it
                node.add(mObj);
                in.close();
                file.close();
              }
              catch(Exception e)
              {
                System.out.println("error at reading .ser");
              }
              try
              {
                FileOutputStream fs = new FileOutputStream(fileName);
                ObjectOutputStream os = new ObjectOutputStream(fs);
                os.writeObject(node);
                os.close(); 
              }
              catch(Exception e)
              {
                System.out.println("error at writing .ser");
              }
            }
            else
            {
              FileOutputStream fs = new FileOutputStream(fileName);
              ObjectOutputStream os = new ObjectOutputStream(fs);

              chatList.add(mObj);
              os.writeObject(chatList);
              os.close();
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            return false;
        }     
      return true;
    }
    public static boolean doesExist(String fileName)
    {
      String currPath=System.getProperty("user.dir");
        System.out.println("Working Directory = " +currPath);
        boolean check = new File(currPath,fileName).exists();
      if(check)
        return true;
      else
        return false;
    }


}

public class Server 
{
  private int port;
  private List<User> clients;
  private ServerSocket server;
  private serialisation serObject= new serialisation("messages.ser");
  public static void main(String[] args) throws IOException 
  {
    new Server(8080).run();
  }

  public Server(int port) 
  {
    this.port = port;
    this.clients = new ArrayList<User>();
  }
  public void LoadMessages(String SerFileName,User signedInUser)
  {
   try
    {
        // Reading the object from a file 
        FileInputStream file = new FileInputStream(SerFileName); 
        ObjectInputStream in = new ObjectInputStream(file);
        // Method for deserialization of object 
        ArrayList<message> object1= new ArrayList<>();
        object1 = (ArrayList)in.readObject(); // type casting it 
        in.close(); 
        file.close(); 
        for(message i :object1)
        {
          if(i.user.equals(signedInUser.getNickname()))
            {
              signedInUser.getOutStream().println("\n<p class=\"tab\" align=\"right\" >"+i.messageStr+"</p>");
              //System.out.println("message send was "+i.user+" and current user is "+signedInUser.getNickname());
            }
          else
            {
              //System.out.println("message send was "+i.user+" and current user is "+signedInUser.getNickname());
              signedInUser.getOutStream().println("\n<p class=\"tab\" align=\"left\" >"+i.messageStr+"    <sub>"+i.user+"</sub>"+"</p>");
            }
        }
        //bufferedWriter.close();
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
  public void run() throws IOException 
  {
    InetAddress ip =  Inet4Address.getByName("127.0.0.1"); //10.184.0.28/Chat-Messenger/ //127.0.0.1
    server = new ServerSocket(port,3,ip) 
    {
      protected void finalize() throws IOException 
      {
        this.close();
      }
    };
    System.out.println("Port "+port+" is now open.");
    
    

    while (true)
    {
      // accepts a new client
      Socket client = server.accept();
      // get nickname of newUser
      String nickname = (new Scanner ( client.getInputStream() )).nextLine();
      //String nickname="loneWolf";
      nickname = nickname.replace(",", ""); //  ',' use for serialisation
      nickname = nickname.replace(" ", "_");
      System.out.println("New Client: \"" + nickname + "\"\n\t     Host:" + client.getInetAddress().getHostAddress());

      // create new User
      User newUser = new User(client, nickname);
      // add newUser to list
      this.clients.add(newUser);
      LoadMessages("messages.ser",newUser);
      // Welcome msg
      newUser.getOutStream().println(
          "<img src='https://www.kizoa.fr/img/e8nZC.gif' height='42' width='42'>"
          + "<b>Welcome</b> " + newUser.toString() +
          "<img src='https://www.kizoa.fr/img/e8nZC.gif' height='42' width='42'>"
          );
      
      // create a new thread for newUser handling
      new Thread(new UserHandler(this, newUser)).start();
    }
  }

  // delete a user from the list
  public void removeUser(User user){
    this.clients.remove(user);
  }

  // send incoming msg to all Users
  public void broadcastMessages(String msg, User userSender) {
    try{
        serObject.addMessage(userSender.getNickname(),msg);
        System.out.println("call at 202 ");
      }
      catch (Exception ex)
      {
        System.out.println("something went wrong");
      }
    for (User client : this.clients) {
  if(userSender.getNickname()==client.getNickname())
      client.getOutStream().println("<p class=\"tab\" align=\"right\" >"+msg+"</p>");
      else
      client.getOutStream().println("<p class=\"tab\" align=\"left\" >"+msg+"    <sub>"+userSender.toString()+"</sub>"+"</p>");    
    }
  }

  // send list of clients to all Users
  public void broadcastAllUsers(){
    for (User client : this.clients) {
      client.getOutStream().println(this.clients);
    }
  }

  // send message to a User (String)
  public void sendMessageToUser(String msg, User userSender, String user){
    boolean find = false;
    for (User client : this.clients) {
      if (client.getNickname().equals(user) && client != userSender) {
        find = true;
        userSender.getOutStream().println(userSender.toString() + " -> " + client.toString() +": " + msg);
        client.getOutStream().println(
            "(<b>Private</b>)" + userSender.toString() + "<span>: " + msg+"</span>");
      }
    }
    if (!find) {
      userSender.getOutStream().println(userSender.toString() + " -> (<b>no one!</b>): " + msg);
    }
  }
}

class UserHandler implements Runnable {

  private Server server;
  private User user;

  public UserHandler(Server server, User user) {
    this.server = server;
    this.user = user;
    this.server.broadcastAllUsers();
  }

  public void run() {
    String message;

    // when there is a new message, broadcast to all
    Scanner sc = new Scanner(this.user.getInputStream());
    while (sc.hasNextLine()) {
      message = sc.nextLine();
      // smiley
      message = message.replace(":)", "<img src='http://4.bp.blogspot.com/-ZgtYQpXq0Yo/UZEDl_PJLhI/AAAAAAAADnk/2pgkDG-nlGs/s1600/facebook-smiley-face-for-comments.png'>");
      message = message.replace(":D", "<img src='http://2.bp.blogspot.com/-OsnLCK0vg6Y/UZD8pZha0NI/AAAAAAAADnY/sViYKsYof-w/s1600/big-smile-emoticon-for-facebook.png'>");
      message = message.replace(":d", "<img src='http://2.bp.blogspot.com/-OsnLCK0vg6Y/UZD8pZha0NI/AAAAAAAADnY/sViYKsYof-w/s1600/big-smile-emoticon-for-facebook.png'>");
      message = message.replace(":(", "<img src='http://2.bp.blogspot.com/-rnfZUujszZI/UZEFYJ269-I/AAAAAAAADnw/BbB-v_QWo1w/s1600/facebook-frown-emoticon.png'>");
      message = message.replace("-_-", "<img src='http://3.bp.blogspot.com/-wn2wPLAukW8/U1vy7Ol5aEI/AAAAAAAAGq0/f7C6-otIDY0/s1600/squinting-emoticon.png'>");
      message = message.replace(";)", "<img src='http://1.bp.blogspot.com/-lX5leyrnSb4/Tv5TjIVEKfI/AAAAAAAAAi0/GR6QxObL5kM/s400/wink%2Bemoticon.png'>");
      message = message.replace(":P", "<img src='http://4.bp.blogspot.com/-bTF2qiAqvi0/UZCuIO7xbOI/AAAAAAAADnI/GVx0hhhmM40/s1600/facebook-tongue-out-emoticon.png'>");
      message = message.replace(":p", "<img src='http://4.bp.blogspot.com/-bTF2qiAqvi0/UZCuIO7xbOI/AAAAAAAADnI/GVx0hhhmM40/s1600/facebook-tongue-out-emoticon.png'>");
      message = message.replace(":o", "<img src='http://1.bp.blogspot.com/-MB8OSM9zcmM/TvitChHcRRI/AAAAAAAAAiE/kdA6RbnbzFU/s400/surprised%2Bemoticon.png'>");
      message = message.replace(":O", "<img src='http://1.bp.blogspot.com/-MB8OSM9zcmM/TvitChHcRRI/AAAAAAAAAiE/kdA6RbnbzFU/s400/surprised%2Bemoticon.png'>");

      // detect if message was privately sent
      if (message.charAt(0) == '@'){
        if(message.contains(" ")){
          System.out.println("private msg : " + message);
          int firstSpace = message.indexOf(" ");
          String userPrivate= message.substring(1, firstSpace);
          server.sendMessageToUser(
              message.substring(
                firstSpace+1, message.length()
                ), user, userPrivate
              );
        }

        // Change manage
      }
      else if (message.charAt(0) == '#'){
        user.changeColor(message);
        // update color for all other users
        this.server.broadcastAllUsers();
      }
      else
      {
        if(message.length()==0)
          ;
        else
          server.broadcastMessages(message, user);
      }
    }
    // end of Thread
    server.removeUser(user);
    this.server.broadcastAllUsers();
    sc.close();
  }
}

class User {
  private static int nbUser = 0;
  private int userId;
  private PrintStream streamOut;
  private InputStream streamIn;
  private String nickname;
  private Socket client;
  private String color;

  // constructor
  public User(Socket client, String name) throws IOException {
    this.streamOut = new PrintStream(client.getOutputStream());
    this.streamIn = client.getInputStream();
    this.client = client;
    this.nickname = name;
    this.userId = nbUser;
    this.color = ColorInt.getColor(this.userId);
    nbUser += 1;
  }

  // change color user
  public void changeColor(String hexColor){
    // check if it's a valid hexColor
    Pattern colorPattern = Pattern.compile("#([0-9a-f]{3}|[0-9a-f]{6}|[0-9a-f]{8})");
    Matcher m = colorPattern.matcher(hexColor);
    if (m.matches()){
      Color c = Color.decode(hexColor);
      // if the Color is too Bright don't change
      double luma = 0.2126 * c.getRed() + 0.7152 * c.getGreen() + 0.0722 * c.getBlue(); // per ITU-R BT.709
      if (luma > 160) {
        this.getOutStream().println("<b>Color Too Bright</b>");
        return;
      }
      this.color = hexColor;
      this.getOutStream().println("<b>Color changed successfully</b> " + this.toString());
      return;
    }
    this.getOutStream().println("<b>Failed to change color</b>");
  }

  public PrintStream getOutStream(){
    return this.streamOut;
  }

  public InputStream getInputStream(){
    return this.streamIn;
  }

  public String getNickname(){
    return this.nickname;
  }

  // print user with his color
  public String toString(){

    return "<u><span style='color:"+ this.color
      +"'>" + this.getNickname() + "</span></u>";

  }
}

class ColorInt {
    public static String[] mColors = {
            "#3079ab", // dark blue
            "#e15258", // red
            "#f9845b", // orange
            "#7d669e", // purple
            "#53bbb4", // aqua
            "#51b46d", // green
            "#e0ab18", // mustard
            "#f092b0", // pink
            "#e8d174", // yellow
            "#e39e54", // orange
            "#d64d4d", // red
            "#4d7358", // green
    };

    public static String getColor(int i) {
        return mColors[i % mColors.length];
    }
}
