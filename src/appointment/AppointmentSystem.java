package appointment;

import java.sql.*;
import java.util.*;

public class AppointmentSystem {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        System.out.println("Welcome to the Patient Appointment System!");

        do {
            System.out.println("\n1. View Doctors\n2. Book Appointment\n3. View Appointments\n4. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    viewDoctors();
                    break;
                case 2:
                    bookAppointment(scanner);
                    break;
                case 3:
                    viewAppointments();
                    break;
                case 4:
                    System.out.println("Exiting system...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 4);

        scanner.close();
    }

    // View doctors
    private static void viewDoctors() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM doctors";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            System.out.println("\nDoctors List:");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") +
                        ", Name: " + rs.getString("name") +
                        ", Specialty: " + rs.getString("specialty") +
                        ", Available: " + rs.getString("available_time"));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving doctors.");
            e.printStackTrace();
        }
    }

    // Book appointment
    private static void bookAppointment(Scanner scanner) {
        System.out.print("Enter Doctor ID: ");
        int doctorId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        System.out.print("Enter Patient Name: ");
        String patientName = scanner.nextLine();

        System.out.print("Enter Patient Contact: ");
        String patientContact = scanner.nextLine();

        System.out.print("Enter Appointment Time (HH:mm): ");
        String time = scanner.nextLine();

        try (Connection conn = DBConnection.getConnection()) {
            // Insert patient record
            String insertPatientQuery = "INSERT INTO patients (doctor_id, patient_name, patient_contact, appointment_time) VALUES (?, ?, ?, ?)";
            PreparedStatement patientStmt = conn.prepareStatement(insertPatientQuery, Statement.RETURN_GENERATED_KEYS);
            patientStmt.setInt(1, doctorId);
            patientStmt.setString(2, patientName);
            patientStmt.setString(3, patientContact);
            patientStmt.setString(4, time);
            patientStmt.executeUpdate();

            ResultSet patientRs = patientStmt.getGeneratedKeys();
            if (patientRs.next()) {
                int patientId = patientRs.getInt(1);

                // Insert appointment
                String insertAppointmentQuery = "INSERT INTO appointments (doctor_id, patient_id, date, time) VALUES (?, ?, CURDATE(), ?)";
                PreparedStatement appointmentStmt = conn.prepareStatement(insertAppointmentQuery);
                appointmentStmt.setInt(1, doctorId);
                appointmentStmt.setInt(2, patientId);
                appointmentStmt.setString(3, time);
                appointmentStmt.executeUpdate();

                System.out.println("Appointment booked successfully!");
            }
        } catch (SQLException e) {
            System.out.println("Error booking appointment.");
            e.printStackTrace();
        }
    }

    // View appointments
    private static void viewAppointments() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT a.id, a.date, a.time, d.name AS doctor_name, p.patient_name AS patient_name " +
                    "FROM appointments a " +
                    "JOIN doctors d ON a.doctor_id = d.id " +
                    "JOIN patients p ON a.patient_id = p.id";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            System.out.println("\nAppointments:");
            while (rs.next()) {
                System.out.println("Appointment ID: " + rs.getInt("id") +
                        ", Date: " + rs.getDate("date") +
                        ", Time: " + rs.getString("time") +
                        ", Doctor: " + rs.getString("doctor_name") +
                        ", Patient: " + rs.getString("patient_name"));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving appointments.");
            e.printStackTrace();
        }
    }
}
