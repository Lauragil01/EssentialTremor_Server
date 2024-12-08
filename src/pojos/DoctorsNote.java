package pojos;

public class DoctorsNote {

    private String doctorName;
    private String doctorSurname;
    private String notes;

    public DoctorsNote(String notes) {
        this.notes = notes;
    }

    public DoctorsNote(String doctorName, String doctorSurname, String notes) {
        this.doctorName = doctorName;
        this.doctorSurname = doctorSurname;
        this.notes = notes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
