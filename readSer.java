import java.io.*;
import java.util.*;
import java.io.File;

public class readSer
{
	public void readSerFile()
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
            System.out.println("Object has been deserialized "); 
            //System.out.println(object1.chatList);
            for(message i :object1)
            {
              System.out.println(i);
            }
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

	public static void main(String[] args)
	{
		readSer ob= new readSer();
		ob.readSerFile();
	}
}