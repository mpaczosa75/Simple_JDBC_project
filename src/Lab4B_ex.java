import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.swing.JOptionPane;
import java.lang.reflect.Member;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

@SuppressWarnings("Duplicates")

public class Lab4B_ex
{
    private static String searchMethod = "";
    private static String queryStatement = null;
    private static ResultSet queryRs = null;
    String exit = "";
    public static void main (String[] args)
    {
        String memberID;
        String finished = "";
        int  memberTrue = 0;

        String memberString = "Select COUNT(*) FROM Member Where MemberID = ?;";

        try {
            //TODO convert to prepared statement
            String url =
                    "jdbc:mysql://localhost:56247/mpaczosa?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
            Class.forName("com.mysql.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(url, "mpaczosa", "830862054");

            ) {
                while(!finished.contains("y")) {
                    finished ="";
                    System.out.println("Asking for value");

                    // Get the value
beginning:          memberID = JOptionPane.showInputDialog("Enter your MemberID:");
                    // https://gist.github.com/nickodell/ab4bde0b141374f1714fe71e4ea023ac
                    PreparedStatement prepareMember = conn.prepareStatement(memberString);
                    prepareMember.setString(1, memberID);
                     ResultSet memberRs= prepareMember.executeQuery();
                    memberRs.next();
                    memberTrue = memberRs.getInt(1);

                    if(memberTrue != 1) {
                        String createUser = JOptionPane.showInputDialog(null,
                                memberID + " is not a Library member, would you like to create an account? (Y/N)");
                        switch (createUser.toLowerCase()) {
                            case "y":
                                enterMember(conn, memberID);
                                memberTrue = 1;
                                break;
                            case "n":
                                while (!(finished.contains("y") || finished.contains("n")))
                                    finished = JOptionPane.showInputDialog(null, "Would You like to exit the program (y/n)").toLowerCase();
                                memberTrue = 0;
                                finished = "";
                                break;
                            default:
                                break;
                        }
                        continue;

                    }
                    while(!validSearch()) {
                    setQuery(conn);
                        while(!(finished.contains("y") || finished.contains("n")))
                            finished = JOptionPane.showInputDialog(null,"Would You like to exit the program (y/n)").toLowerCase();
                    }
                   searchMethod ="";
                }
            }
        }
        catch (Exception e) {
            System.err.println(e);
        }



        // Display results
//            JOptionPane.showMessageDialog (null,
//                    "You entered:  " + memberID);


        return;
    }

    private static boolean validSearch(){
        if (searchMethod.contains("isbn") ||
                searchMethod.contains("title") ||
                searchMethod.contains("author"))
            return true;
        return false;
    }

    private static void setQuery(Connection conn) throws SQLException {
        searchMethod = JOptionPane.showInputDialog("Enter Search method (ISBN, Title, Author)").toLowerCase();
        switch (searchMethod.toLowerCase()){
            case "isbn":
                executeISBNquery(conn);
                break;
            case "title":
                executeTitlequery(conn);
                break;
            case "author":
                executeAuthorquery(conn);
                break;
            default:
                JOptionPane.showMessageDialog(null,
                        "Invalid Search, please enter one of the search methods");
        }
    }

    private static void executeISBNquery(Connection conn) throws SQLException {
        while (true) {
            String ISBN = JOptionPane.showInputDialog("Enter ISBN").toLowerCase();
            if (!ISBN.matches(("\\d*-\\d*-\\d*"))) {
                JOptionPane.showMessageDialog (null,
                        "                             Invalid ISBN\n" +
                                "ISBN must be in the form of: [###]-[#####]-[#####]\n" +
                                "Please enter valid ISBN" );
                continue;
            }
            queryStatement = "SELECT TotalCopies, Title, Shelf_Number, LibName from StoredOn JOIN Book on StoredOn.ISBN = Book.ISBN where StoredOn.ISBN = ?;";
            PreparedStatement prepareMember = conn.prepareStatement(queryStatement);
            prepareMember.setString(1, ISBN);

            int totalCopies = -1;
            String title = "";
            ArrayList<String> shelves = new ArrayList<>();
            ArrayList<String> libraries = new ArrayList<>();

            queryRs = prepareMember.executeQuery();
            while (queryRs.next()){
                    totalCopies = queryRs.getInt(1);
                    title = queryRs.getString(2);
                    shelves.add(queryRs.getString(3));
                    libraries.add(queryRs.getString(4));
            }
            printResults(title,ISBN,totalCopies,libraries, shelves);
            break;
        }
    }
    private static void executeAuthorquery(Connection conn)throws SQLException{
            while (true) {
                String author = JOptionPane.showInputDialog("Enter Author").toLowerCase();
                String firstName = author;
                String lastName = author;
                if (author.lastIndexOf(" ") != -1) {
                    firstName = author.replace(author.substring(author.indexOf(" ")), "");
                    lastName = author.replace(author.substring(0, author.indexOf(" ") + 1), "");
                }

                queryStatement = "SELECT Title from Author JOIN WrittenBy on Author.AuthorID = WrittenBy.AuthorID JOIN Book on WrittenBy.ISBN = Book.ISBN where FirstName = ? or LastName = ? or LastName = ? or FirstName =?;";
                PreparedStatement prepareMember = conn.prepareStatement(queryStatement);
                prepareMember.setString(1,  firstName);
                prepareMember.setString(2,  lastName );
                prepareMember.setString(3, lastName);
                prepareMember.setString(4,firstName);

                int totalCopies = -1;
                ArrayList<String> titles = new ArrayList();
                String title = "";
                String ISBN = "";
                ArrayList<String> shelves = new ArrayList<>();
                ArrayList<String> libraries = new ArrayList<>();
                String body = "";

                queryRs = prepareMember.executeQuery();
                while (queryRs.next()) {
                    titles.add(queryRs.getString(1));
                }
                if (titles.size() > 0) {
                    body = "Choose one of the following books by " + author + ": \n";
                    for (int i = 0; i < titles.size(); i++) {
                        body += titles.get(i);
                        if (i < titles.size() - 1) {
                            body += ", ";
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "No books in stock by " + author);
                    break;
                }
                innerLoop:
                while (true) {
                    title = JOptionPane.showInputDialog(null, body).toLowerCase();
                    for (String book : titles)
                        if (title.equals(book.toLowerCase())) break innerLoop;
                }

                queryStatement = "SELECT TotalCopies, Book.ISBN,Shelf_Number, LibName from StoredOn JOIN Book on StoredOn.ISBN = Book.ISBN where Title LIKE ?;";
                prepareMember = conn.prepareStatement(queryStatement);
                prepareMember.setString(1, title);


                queryRs = prepareMember.executeQuery();
                while (queryRs.next()) {
                    totalCopies = queryRs.getInt(1);
                    ISBN = queryRs.getString(2);
                    shelves.add(queryRs.getString(3));
                    libraries.add(queryRs.getString(4));
                }
                printResults(title,ISBN,totalCopies,libraries, shelves);
                break;
            }
    }
    private static void executeTitlequery(Connection conn)throws SQLException{
        while (true) {
            String partialTitle = JOptionPane.showInputDialog("Enter Title").toLowerCase();


            queryStatement = "SELECT Title from Author JOIN WrittenBy on Author.AuthorID = WrittenBy.AuthorID JOIN Book on WrittenBy.ISBN = Book.ISBN where Title LIKE ?;";
            PreparedStatement prepareMember = conn.prepareStatement(queryStatement);
            prepareMember.setString(1, "%" + partialTitle + "%");

            int totalCopies = -1;
            ArrayList<String> titles = new ArrayList();
            String title = "";
            String ISBN = "";
            ArrayList<String> shelves = new ArrayList<>();
            ArrayList<String> libraries = new ArrayList<>();
            String body = "";

            queryRs = prepareMember.executeQuery();
            while (queryRs.next()) {
                titles.add(queryRs.getString(1));
            }
            if (titles.size() > 0) {
                body = "Choose one of the following books" + ": \n";
                for (int i = 0; i < titles.size(); i++) {
                    body += titles.get(i);
                    if (i < titles.size() - 1) {
                        body += ", ";
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "No books in stock by under " + partialTitle);
                break;
            }
            innerLoop:
            while (true) {
                title = JOptionPane.showInputDialog(null, body).toLowerCase();
                for (String book : titles)
                    if (title.equals(book.toLowerCase())) break innerLoop;
            }

            queryStatement = "SELECT TotalCopies, Book.ISBN,Shelf_Number, LibName from StoredOn JOIN Book on StoredOn.ISBN = Book.ISBN where Title LIKE ?;";
            prepareMember = conn.prepareStatement(queryStatement);
            prepareMember.setString(1, title);


            queryRs = prepareMember.executeQuery();
            while (queryRs.next()) {
                totalCopies = queryRs.getInt(1);
                ISBN = queryRs.getString(2);
                shelves.add(queryRs.getString(3));
                libraries.add(queryRs.getString(4));
            }
            printResults(title,ISBN,totalCopies,libraries, shelves);
            break;
        }
    }
    private static void printResults(String title, String ISBN, int totalCopies, ArrayList list, ArrayList shelves ){
            if (totalCopies > 0){
                String body = "Book: " + title + ", ISBN : " + ISBN + " is available at ";
                for (int i = 0; i < list.size(); i++){
                    body += "Library: " + list.get(i) + ", Shelf " + shelves.get(i);
                    if (i < list.size()-1){
                        body += " and ";
                    }
                }
                JOptionPane.showMessageDialog(null,
                        body);
                return;
            }
            if (totalCopies == 0) {
                JOptionPane.showMessageDialog(null,
                        "All copies of Book: " + title + ", ISBN : " + ISBN + "are checked out.");
                return;
            }
            JOptionPane.showMessageDialog(null,
                    "Title : " + title + " is not in stock.");
            return;
        }
    private static void enterMember(Connection conn, String memberID) throws SQLException {
        String firstName = JOptionPane.showInputDialog("Enter your First Name");
        String lastName = JOptionPane.showInputDialog("Enter your Last Name");
        String gender = JOptionPane.showInputDialog("Enter Your Gender: (M|F) or leave blank").toUpperCase();
        String DOB = JOptionPane.showInputDialog("Enter your Date of Birth (yyyy-mm-dd)");
        queryStatement = "INSERT INTO Member Values (?,?,?,?,?);";
        PreparedStatement update = conn.prepareStatement(queryStatement);
        update.setString(1,memberID);
        update.setString(2,firstName);
        update.setString(3,lastName);
        update.setString(4,gender);
        update.setString(5, DOB);
        update.executeUpdate();
    }

} // end

