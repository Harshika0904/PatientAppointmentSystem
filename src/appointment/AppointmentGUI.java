package appointment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

public class AppointmentGUI extends JFrame {

    private JTextArea displayArea;

    public AppointmentGUI() {
        setTitle("Patient Appointment System");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        displayArea = new JTextArea();
        displayArea.setEditable(false);

        JPanel buttonPanel = new JPanel();

        JButton viewDoctorsBtn = new JButton("View Doctors");
        JButton bookAppointmentBtn = new JButton("Book Appointment");
        JButton viewTodayBtn = new JButton("Today's Appointments");

        viewDoctorsBtn.addActionListener(e -> viewDoctors());
        bookAppointmentBtn.addActionListener(e -> bookAppointment());
        viewTodayBtn.addActionListener(e -> showTodaysAppointments());

        buttonPanel.add(viewDoctorsBtn);
        buttonPanel.add(bookAppointmentBtn);
        buttonPanel.add(viewTodayBtn);

        add(buttonPanel, BorderLayout.NORTH);
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        setVisible(true);
    }

    private void viewDoctors() {
        displayArea.setText("Doctors List:\n");
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM doctors";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                displayArea.append("ID: " + rs.getInt("id") + ", Name: " + rs.getString("name") +
                        ", Specialty: " + rs.getString("specialty") +
                        ", Available: " + rs.getString("available_time") + "\n");
            }
        } catch (SQLException e) {
            displayArea.setText("Error loading doctors.\n" + e.getMessage());
        }
    }

    private void bookAppointment() {
        JTextField doctorIdField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField contactField = new JTextField();
        JTextField timeField = new JTextField();

        Object[] inputs = {
                "Doctor ID:", doctorIdField,
                "Patient Name:", nameField,
                "Contact:", contactField,
                "Time (HH:mm):", timeField
        };

        int result = JOptionPane.showConfirmDialog(null, inputs, "Book Appointment", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String timeInput = timeField.getText().trim();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime requestedTime;

            try {
                requestedTime = LocalTime.parse(timeInput, timeFormatter);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid time format. Please use HH:mm (e.g., 09:30).", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                int doctorId = Integer.parseInt(doctorIdField.getText().trim());

                String checkQuery = "SELECT appointment_time FROM patients WHERE doctor_id = ? AND appointment_date = CURDATE()";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setInt(1, doctorId);
                ResultSet rs = checkStmt.executeQuery();

                while (rs.next()) {
                    String existingTimeStr = rs.getString("appointment_time");
                    LocalTime existingTime = LocalTime.parse(existingTimeStr, timeFormatter);

                    if (existingTime.equals(requestedTime)) {
                        JOptionPane.showMessageDialog(this,
                                "This time slot is already booked.",
                                "Booking Failed", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    long diffMinutes = Math.abs(Duration.between(existingTime, requestedTime).toMinutes());
                    if (diffMinutes < 15) {
                        JOptionPane.showMessageDialog(this,
                                "This time slot is too close to an existing appointment.\nPlease maintain at least 15 minutes gap.",
                                "Booking Too Close", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }

                String insert = "INSERT INTO patients (doctor_id, patient_name, patient_contact, appointment_time, appointment_date) VALUES (?, ?, ?, ?, CURDATE())";
                PreparedStatement stmt = conn.prepareStatement(insert);
                stmt.setInt(1, doctorId);
                stmt.setString(2, nameField.getText());
                stmt.setString(3, contactField.getText());
                stmt.setString(4, timeInput);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Appointment booked successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error booking appointment:\n" + ex.getMessage());
            }
        }
    }

    private void showTodaysAppointments() {
        displayArea.setText("Today's Appointments:\n");
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT p.id, p.patient_name, p.patient_contact, p.appointment_time, d.name AS doctor_name " +
                    "FROM patients p JOIN doctors d ON p.doctor_id = d.id WHERE appointment_date = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                displayArea.append("Patient ID: " + rs.getInt("id") +
                        ", Name: " + rs.getString("patient_name") +
                        ", Contact: " + rs.getString("patient_contact") +
                        ", Time: " + rs.getString("appointment_time") +
                        ", Doctor: " + rs.getString("doctor_name") + "\n");
            }
        } catch (SQLException e) {
            displayArea.setText("Error loading today's appointments.\n" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new AppointmentGUI();
    }
}
