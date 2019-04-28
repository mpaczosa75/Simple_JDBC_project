
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class Lab4_ex {
    public static void main(String args[]){

        if(args.length != 1) {
            System.err.append("input file missing");
        }
        Connection con = null;

        try {
            Statement stmt;
            ResultSet rs;

            // Register the JDBC driver for MySQL.
            Class.forName("com.mysql.jdbc.Driver");

            // Define URL of database server for
            // database named 'user' on the faure.
            String url =
                    "jdbc:mysql://faure:3306/mpaczosa?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
            // Get a connection to the database for a
            // user named 'user' with the password
            // 123456789.
            con = DriverManager.getConnection(
                    url,"mpaczosa", "830862054");

            // Display URL and connection information
            System.out.println("URL: " + url);
            System.out.println("Connection: " + con);

            // Get a Statement object
            stmt = con.createStatement();

            try{
                rs = stmt.executeQuery("SELECT * FROM Author");
                while (rs.next()) {
                    System.out.println (rs.getString("AuthorID"));
                }
                parse(args);
            }catch(Exception e){
                System.out.print(e);
                System.out.println(
                        "No Author table to query");
            }//end catch

            con.close();
        }catch( Exception e ) {
            e.printStackTrace();

        }//end catch

    }//end main
    public static void parse(String [] args) throws XMLStreamException, IOException {
        try (FileInputStream fis = new FileInputStream(args[0])) {
            XMLInputFactory xmlInFact = XMLInputFactory.newInstance();
            XMLStreamReader reader = xmlInFact.createXMLStreamReader(fis);
            while(reader.hasNext()) {
                reader.next(); // do something here

            }
        }
    }
}