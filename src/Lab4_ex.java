
import java.io.IOException;
import java.sql.*;

import javax.xml.parsers.ParserConfigurationException;


import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;




public class Lab4_ex {
    private  int MemberID;
    private  String ISBN;
    private  String Checkout_Date;
    private  String Checkin_Date;
    private  String type;

    public Lab4_ex(int MemberID, String ISBN, String Checkout_Date, String Checkin_Date, String type){
        this.MemberID = MemberID;
        this.ISBN = ISBN;
        this.Checkout_Date = Checkout_Date;
        this.Checkin_Date = Checkin_Date;
        this.type = type;
    }

    public static void main(String args[]){

        Connection con = null;

        try (FileWriter writer = new FileWriter(args[1]);
        BufferedWriter filewriter = new BufferedWriter(writer)){


                Statement stmt;
            ResultSet rs;

            // Register the JDBC driver for MySQL.
            Class.forName("com.mysql.jdbc.Driver");

            // Define URL of database server for
            // database named 'user' on the faure.
            String url =
                    "jdbc:mysql://localhost:56247/mpaczosa?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
            // Get a connection to the database for a
            // user named 'user' with the password
            // 123456789.
            con = DriverManager.getConnection(
                    url,"mpaczosa", "830862054");

            // Get a Statement object
            stmt = con.createStatement();
            ArrayList<Lab4_ex> transactions  = readXml(args);

            for(Lab4_ex transaction:transactions ) {

//                System.out.println("MemberID: " + transaction.MemberID);
//                System.out.println("ISBN: " + transaction.ISBN);
//                System.out.println("Checkout_Date: " + transaction.Checkout_Date);
//                System.out.println("Checkin_Date: " + transaction.Checkin_Date);
//                System.out.println("type: " + transaction.type + "\n");
                try  {


                        ResultSet stmtRs;
                    stmtRs= stmt.executeQuery("SELECT LibName, TotalCopies FROM StoredOn WHERE ISBN = '" + transaction.ISBN + "' and  TotalCopies > 0;");
                    int copies = -1;
                    String library ="";
                    while (stmtRs.next()){
                        if(stmtRs.getInt("TotalCopies") > copies){
                            copies = stmtRs.getInt("TotalCopies");
                            library = stmtRs.getString("Libname");
                        }
                    }
                    if(transaction.type.equals("checkout")){

                        if(copies > 0) {
                            stmt.executeUpdate("INSERT IGNORE INTO `BorrowedBy` (`MemberID`,`ISBN`,`CheckoutDate`,`CheckinDate`)  VALUES ("
                                    + transaction.MemberID + "," + "'" + transaction.ISBN + "'" + ","
                                    + "str_to_date(" + transaction.Checkout_Date + ",'%Y-%m-%d'),"
                                    + "str_to_date(" + transaction.Checkin_Date + ",'%Y-%m-%d'));");
                            stmt.executeUpdate("UPDATE StoredOn SET TotalCopies = TotalCopies -1 WHERE LibName LIKE '"+library+"';");
                            filewriter.write("Successfully checked out Book number " + transaction.ISBN + " Total Copies in "+library+" library is "+copies--+".\n");
                            continue;
                        }
                            filewriter.write("Tried to checkout a Book that isn't in a Library.\n");


                    }
                    else{
                        stmtRs = stmt.executeQuery(" SELECT MemberID From `BorrowedBy` WHERE CheckinDate IS NULL and "
                                +"CheckOutDate IS NOT NULL and MemberID = " + transaction.MemberID
                                +" and ISBN like '"+ transaction.ISBN+"';");
                        stmtRs.next();
                        stmtRs.getString("MemberID");

                        stmt.executeUpdate("UPDATE `BorrowedBy` SET `CheckinDate` = (str_to_date("
                                + transaction.Checkin_Date + ",'%Y-%m-%d')) WHERE CheckinDate IS NULL and "
                                + "CheckOutDate IS NOT NULL and MemberID = " + transaction.MemberID
                                + " and ISBN like \"" + transaction.ISBN + "\";");

                        stmt.executeUpdate("UPDATE StoredOn SET TotalCopies = TotalCopies + 1 WHERE LibName LIKE '"+library+"';");

                        filewriter.write("Successfully checked in Book number " + transaction.ISBN+" Total Copies in "+library+" library is "+copies--+".\n");

                    }
//
                    stmtRs.close();
                } catch (Exception e) {
                    System.out.println(e);
                    if(transaction.type.equals("checkout")) {
                        System.out.println(
                            "Tried to checkout a Book that isn't in a Library.");
                    }
                    else System.out.println(
                            "Tried to checkin book with no checkout record"
                    );
                }//end catch
            }
            System.out.println();
     try {
         rs = stmt.executeQuery("SELECT * FROM BorrowedBy;");
         while (rs.next()) {

             filewriter.write("MemberID: " +  rs.getString("MemberID")+"\n");
             filewriter.write("ISBN: " + rs.getString("ISBN")+"\n");
             filewriter.write("Checkout_Date: " + rs.getString("CheckoutDate")+"\n");
             filewriter.write("Checkin_Date: " + rs.getString("CheckinDate") + "\n\n");

         }
         rs = stmt.executeQuery("Select Last_Name,First_Name,m.MemberID,Title From Member as m join BorrowedBy as br " +
                 "on m.MemberID = br.MemberID join Book as b on br.ISBN = b.ISBN Where " +
                 "CheckoutDate IS NOT NULL ORDER BY m.MemberID;\n");
         String memberID ="";
         while (rs.next()){
          String instance = rs.getString("MemberID");
             if(!instance.equals(memberID)){
                 filewriter.write("\n");
                 filewriter.write(rs.getString("First_Name")+" "+rs.getString("Last_Name")
                 +"  has Checked out: '"+ rs.getString("Title")+"' ");
             }
             else {
                 filewriter.write("'"+rs.getString("Title")+"' ");
             }
             memberID = instance;
         }
     }
     catch (Exception e){
         e.printStackTrace();
     }

            con.close();
        }catch( Exception e ) {
            e.printStackTrace();

        }//end catch


    }//end main
    public static ArrayList <Lab4_ex> readXml(String [] args) throws IOException,SAXException, ParseException{
//
        ArrayList<Lab4_ex> transactions = new ArrayList<>();
        try {

            File file = new File(args[0]);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nodeLst = doc.getElementsByTagName("Borrowed_by");

            for (int s = 0; s < nodeLst.getLength(); s++) {

                Node fstNode = nodeLst.item(s);

                if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element sectionNode = (Element) fstNode;

                    NodeList memberIdElementList = sectionNode.getElementsByTagName("MemberID");
                    Element memberIdElmnt = (Element) memberIdElementList.item(0);
                    NodeList memberIdNodeList = memberIdElmnt.getChildNodes();
                    //System.out.println("MemberID : " + ((Node) memberIdNodeList.item(0)).getNodeValue().trim());
                    int memberID = Integer.parseInt(((Node) memberIdNodeList.item(0)).getNodeValue().trim());


                    NodeList secnoElementList = sectionNode.getElementsByTagName("ISBN");
                    Element secnoElmnt = (Element) secnoElementList.item(0);
                    NodeList secno = secnoElmnt.getChildNodes();
                    //System.out.println("ISBN : " + ((Node) secno.item(0)).getNodeValue().trim());

                    String isbn =((Node) secno.item(0)).getNodeValue().trim();

                    String type = null;

                    NodeList codateElementList = sectionNode.getElementsByTagName("Checkout_date");
                    Element codElmnt = (Element) codateElementList.item(0);
                    NodeList cod = codElmnt.getChildNodes();
                    //System.out.println("Checkout_date : " + ((Node) cod.item(0)).getNodeValue().trim());

                    //https://stackoverflow.com/questions/20235692/how-to-convert-string-to-datetime-in-java
                    String checkout_date = null ;
                    String temp = ((Node) cod.item(0)).getNodeValue().trim();
                    temp = temp.replace("/", "-");

                    if(temp.equals("N-A")) {
                        checkout_date = null;
                        type = "checkin";
                    }
                    else {type = "checkin";
                        checkout_date =parseSqlDate(temp);
                    }


                  String checkin_date = null ;
                    NodeList cidateElementList = sectionNode.getElementsByTagName("Checkin_date");
                    Element cidElmnt = (Element) cidateElementList.item(0);
                    NodeList cid = cidElmnt.getChildNodes();

                    temp = ((Node) cid.item(0)).getNodeValue().trim();
                    temp = temp.replace("/", "-");
                    if(temp.equals("N-A")) {
                         checkin_date =null;
                         type = "checkout";
                    }
                    else {
                        checkin_date = parseSqlDate(temp);
                    }

                    Lab4_ex transaction = new Lab4_ex(memberID, isbn, checkout_date, checkin_date, type );
                    transactions.add(transactions.size(),transaction);

                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return transactions;
    }
    private static String parseSqlDate(String s) throws ParseException{
        String newDate= s.substring(s.length()-4);

        return newDate + "-" + s.substring(0,s.length()-5);
    }
}