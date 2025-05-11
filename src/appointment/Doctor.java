package appointment;

public class Doctor {
    private int id;
    private String name;
    private String specialty;
    private boolean available;

    public Doctor(int id, String name, String specialty, boolean available) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.available = available;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}

