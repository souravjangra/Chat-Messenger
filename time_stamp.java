import java.util.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;

public class time_stamp {

    public static String GetTimeStamp()
    {
      Date date = new Date();
      long time = date.getTime();
      Timestamp ts = new Timestamp(time);

      String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new java.util.Date());
      return timeStamp;        
    }

}
