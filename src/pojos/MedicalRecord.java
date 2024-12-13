package pojos;



import signals.ACC;
import signals.EMG;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecord {

    private String patientName;
    private String patientSurname;
    private int age;
    private double weight;
    private int height;
    private List<String> symptoms;
    private ACC acceleration;
    private EMG emg;
    private Boolean genetic_background;
    private List<DoctorsNote> doctorsNotes;



    public List<DoctorsNote> getDoctorsNotes() {
        return doctorsNotes;
    }
    public void setDoctorsNotes(List<DoctorsNote> doctorsNotes) {
        this.doctorsNotes = doctorsNotes;
    }

    public Boolean getGenetic_background() {
        return genetic_background;
    }

    public void setGenetic_background(Boolean genetic_background) {
        this.genetic_background = genetic_background;
    }

    public String getPatientName() {
        return patientName;
    }


    public String getPatientSurname() {
        return patientSurname;
    }


    public ACC getAcc(){
        return acceleration;
    }
    public void setAcceleration(ACC acceleration) {
        this.acceleration = acceleration;
    }

    public EMG getEmg() {
        return emg;
    }

    public void setEmg(EMG emg) {
        this.emg = emg;
    }

    public int getAge() {
        return age;
    }

    public double getWeight() {
        return weight;
    }

    public int getHeight() {
        return height;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }


    public MedicalRecord(int age, double weight, int height, List<String> symptoms,ACC accSignal, EMG emgSignal, boolean geneticBackground, String patientName, String patientSurname) {
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.symptoms = symptoms;
        this.acceleration=accSignal;
        this.emg=emgSignal;
        this.genetic_background=geneticBackground;
        this.patientName=patientName;
        this.patientSurname=patientSurname;
        this.doctorsNotes = new ArrayList<>();

    }

    public MedicalRecord(String patientName, String patientSurname, int age, double weight, int height, List<String> symptoms, Boolean genetic_background) {
        this.patientName = patientName;
        this.patientSurname = patientSurname;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.symptoms = symptoms;
        this.genetic_background = genetic_background;
        this.doctorsNotes = new ArrayList<>();
    }
    public MedicalRecord(String patientName, String patientSurname, int age, double weight, int height,Boolean geneticBackground, List<String> symptoms) {
        this.patientName = patientName;
        this.patientSurname = patientSurname;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.genetic_background = geneticBackground;
        this.symptoms = symptoms;
        this.doctorsNotes = new ArrayList<>(); //list vacía

    }

    @Override
    public String toString() {
        return "MedicalRecord{" +
                "patientName='" + patientName + '\'' +
                ", surname= '"+ patientSurname + '\''+
                ", age=" + age +
                ", weight=" + weight +
                ", height=" + height +
                ", symptoms=" + symptoms +
                ", genetic_background=" + genetic_background +
                '}';
    }

    public void showAcc() {
        this.acceleration.plotSignal();
    }

    public void showEMG(){
        this.emg.plotSignal();
    }
}
