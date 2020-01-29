package mmk.study.launcher.objects;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ContactObject {
    private String id;
    private String displayName;
    private List<String> phoneNumbers=new ArrayList<>();

    public ContactObject() {
    }

    public ContactObject(String id, String displayName, List<String> phoneNumbers) {
        this.id = id;
        this.displayName = displayName;
        this.phoneNumbers = phoneNumbers;
    }

    public ContactObject(String displayName, List<String> phoneNumbers) {
        this.displayName = displayName;
        this.phoneNumbers = phoneNumbers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    public String getDisplayName() {
        return displayName;
    }

    public void addPhoneNumber(String phoneNumber){
        phoneNumbers.add(phoneNumber);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    @NonNull
    @Override
    public String toString() {
        String string="Id: "+this.id+" Name: "+this.displayName+" phoneNumbers: ";

        for (String number: this.phoneNumbers){
            string+=number+",";
        }
        return string;
    }
}
