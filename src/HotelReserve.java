/**
 * Hotel Reservation System to manage reservations by using JDBC.
 @author Milan
 @version 17
 **/

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Scanner;

public class HotelReserve {

    // Database URL in MYSQL
    private static String url = "jdbc:mysql://localhost:3306/hotel";

    // Database username
    private static String user = "root";

    // Database password
    private  static String password = "admin";

    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Load MySQL JDBC Driver
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            Connection connection = DriverManager.getConnection(url,user,password); // Establish database connection
            Statement stmt = connection.createStatement(); // Create statement object for executing SQL commands

            //Running the loop until user exit the system using exit method
            while (true){
                System.out.println();
                System.out.println("Welcome to the Hotel Reservation System");
                Scanner sc = new Scanner(System.in);

                System.out.println("1. Reserve a room");
                System.out.println("2. View Reservations");
                System.out.println("3. Get Room Number");
                System.out.println("4. Update Reservations");
                System.out.println("5. Delete Reservations");
                System.out.println("0. Exit");
                System.out.print("Choose an option: ");

                int option = sc.nextInt();

                switch (option){
                    case 1:
                        roomReservation(sc,stmt);
                        break;
                    case 2:
                        viewReservations(sc,stmt);
                        break;
                    case 3:
                        roomNo(sc,stmt);
                        break;
                    case 4:
                        updateReservation(sc,stmt);
                        break;
                    case 5:
                        deleteReservation(sc,stmt);
                        break;
                    case 0:
                        exit();
                        sc.close();
                       return; //exiting the method
                    default:
                        System.out.println("Invalid Option! Choose again.");
                }
            }
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void roomReservation( Scanner sc, Statement stmt){
        try {
            System.out.print("Enter the name of guest: ");
            String name = sc.next(); // This captures firstname of guest
            sc.nextLine(); //clears the buffer of any leftover characters
            System.out.print("Enter Room Number: ");
            int room = sc.nextInt();
            System.out.print("Enter Phone Number: ");
            String phoneNo = sc.next();

            // Check if room is already full then exit the whole roomReservation method
            if(isRoomFull(room,stmt)){
                System.out.println("Room is already full. Choose another room");
                return; //exit
            }

            String insert_sql ="INSERT INTO reservations(guest_name, room_number,contact_number) VALUES ('" + name + "'," + room + ",'" + phoneNo +"');";
                int rowsAffected = stmt.executeUpdate(insert_sql); // Execute insert query to add reservation
                if(rowsAffected > 0){
                    System.out.println("Reservation successful.");
                }else {
                    System.out.println("Error! Check again.");
                }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static boolean isRoomFull(int roomNo,Statement st){
        try {
            String sql = "SELECT room_number FROM reservations WHERE room_number = " + roomNo;

            ResultSet rs = st.executeQuery(sql);
            return rs.next(); // Returns true if a result exists, indicating the room is occupied
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void viewReservations(Scanner sc,Statement stmt){
        String view_sql = "SELECT * FROM reservations;";

        try {
            ResultSet rs = stmt.executeQuery(view_sql);

            System.out.println("Current Reservations:");
            System.out.println("+----------------+-----------------+---------------+----------------------+-------------------------+");
            System.out.println("| Reservation ID | Guest Name    | Room Number   | Contact Number      | Reservation Date        |");
            System.out.println("+----------------+-----------------+---------------+----------------------+-------------------------+");

            while (rs.next()){
                int id = rs.getInt("reservation_id");
                String name = rs.getString("guest_name");
                int roomNo = rs.getInt("room_number");
                String phoneNo = rs.getString("contact_number");
                String date = rs.getTimestamp("reservation_date").toString();

                //Formatting and displaying the data in table format
                //% indicates a format specifier.
                //- left-aligns the content within the specified width.
                //14 specifies the width (14 characters in this case).
                //d means the value is  int and s is String
                System.out.printf("|%-14d | %-15s | %-13d | %-20s | %-19s     |\n",
                        id,name,roomNo,phoneNo,date);
            }
            System.out.println("+----------------+-----------------+---------------+----------------------+-------------------------+");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void roomNo(Scanner sc,Statement stmt){
        try {
            System.out.print("Enter reservation ID: ");
            int id = sc.nextInt();
            System.out.print("Enter guest Name: ");
            String name = sc.next();

            String sql = "SELECT room_number FROM reservations WHERE reservation_id = " + id + " AND guest_name = '" + name + "';";

            ResultSet rs = stmt.executeQuery(sql);

            if(rs.next()){
                int room_no = rs.getInt("room_number");
                System.out.println("Room number is " + room_no + " for Reservation id " + id + "and the GuestName " + name + ".");
            }else {
                System.out.println("Reservations not found.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void updateReservation(Scanner sc,Statement st){
        try {
            System.out.print("Enter reservation ID: ");
            int id = sc.nextInt();
            sc.nextLine();

            //if there's no reservation then whole method will exit
            if(!reservationExists(id,st)){
                System.out.println("Reservation does not exist");
                return; //exit the method
            }

            System.out.print("Enter New Guest Name: ");
            String newName = sc.nextLine();
            System.out.print("Enter new room no: ");
            int newRoom = sc.nextInt();
            System.out.print("Enter new number: ");
            String newNum = sc.nextLine();

            String update_sql = "UPDATE reservations SET guest_name = '" + newName + "'," +
            "room_number = " + newRoom + ", " +
            "contact_number = '" + newNum + "' " + "WHERE reservation_id = " + id;

            int rowsAffected = st.executeUpdate(update_sql);
            if(rowsAffected > 0){
                System.out.println("Updated successfully");
            }else {
                System.out.println("Update failed");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void deleteReservation(Scanner sc, Statement st){
        try {
            System.out.print("Enter Reservation Id: ");
            int reserveId = sc.nextInt();

            if(!reservationExists(reserveId,st)){
                System.out.println("Reservation not found");
                return; //exit the method
            }

            String delete_sql = "DELETE FROM reservations WHERE reservation_id = " + reserveId;

            int rows = st.executeUpdate(delete_sql);

            if(rows > 0 ){
                System.out.println("Delete successful");
            }else {
                System.out.println("Not deleted");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static boolean reservationExists(int id,Statement st){
        try {
            String sql = "SELECT reservation_id FROM reservations WHERE reservation_id = " + id;

            ResultSet rs = st.executeQuery(sql);
            return rs.next(); // Returns true if a reservation exists
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void exit() throws InterruptedException {
        System.out.print("Exiting System");
        int i = 5;
        while (i!=0){
            System.out.print(".");
            Thread.sleep(200);
            i--;
        }
        System.out.println();
        System.out.println("ThankYou for using the system.");
    }
}
