package mmk.study.launcher.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import mmk.study.launcher.objects.ContactObject;
import mmk.study.launcher.repository.ContactsRepository;

public class ContactsViewModel extends AndroidViewModel {

    private ContactsRepository contactsRepository;

    public ContactsViewModel(@NonNull Application application) {
        super(application);
        contactsRepository=new ContactsRepository(application);
    }





    public LiveData<List<ContactObject>> getAllContacts(){
        return contactsRepository.getAllContacts();
    }

    public LiveData<List<ContactObject>> getContactsByString(String searchString){
        return contactsRepository.getContactsByString(searchString);
    }

    public void deleteContact(String id){
        contactsRepository.deleteContact(id);
    }
    public void updateContact(ContactObject contactObject){contactsRepository.updateContact(contactObject);}
    public void insertContact(ContactObject contactObject){contactsRepository.insertContact(contactObject);}




}
