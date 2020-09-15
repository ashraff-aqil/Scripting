import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import system.DateTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TestDate {
    public static void main(String args[]) {
        String dateReceived = "01 NOVEMBER 2018 ";
        String dateReceived2 = "October 8 2019";
        String dateReceived3 = "October 072020";
        dateReceived = dateReceived.trim();
        dateReceived = dateReceived.replace("st","");
        try{
            System.out.println("Cleaned: "+dateReceived);
            Date finalDate = new SimpleDateFormat("dd MMMM yyyy").parse(dateReceived);
            System.out.println("Final: "+finalDate.toString());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String convertedDate = sdf.format(finalDate);
            System.out.println("Converted Date: "+convertedDate);
        }
        catch(Exception e){
            System.out.println("Date received was not in a good format");
        }
        try{
            Date finalDate = new SimpleDateFormat("MMMM dd yyyy").parse(dateReceived2);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String convertedDate = sdf.format(finalDate);
            System.out.println("Converted Date: "+convertedDate);
        }
        catch (Exception e){
            System.out.println("Okay la");
        }
        try{
            Date finalDate = new SimpleDateFormat("MMMM ddyyyy").parse(dateReceived3);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String convertedDate = sdf.format(finalDate);
            System.out.println("Converted Date: "+convertedDate);

            Calendar c = Calendar.getInstance();
            try{
                c.setTime(sdf.parse(convertedDate));
                c.add(Calendar.DAY_OF_MONTH,7);
                String newDate = sdf.format(c.getTime());
                System.out.println("Okay: "+newDate);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        catch (Exception e){
            System.out.println("Okay la");
        }
    }
}
