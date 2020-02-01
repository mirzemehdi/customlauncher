package mmk.study.launcher.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;

import mmk.study.launcher.contentProviders.ContactsProvider;
import mmk.study.launcher.objects.ContactObject;

public class ContactsRepository {

    private ContactsProvider contactsProvider;

    public ContactsRepository (Application application){
        contactsProvider=new ContactsProvider(application);
    }


    public LiveData<List<ContactObject>> getAllContacts(){
        return  contactsProvider.getAllContacts();
    }

    public LiveData<List<ContactObject>> getContactsByString(String searchString){
        Log.d("onChangeContacts","GetAllContacts Repo Called");
        return contactsProvider.getContactsByString(searchString);
    }

    public void deleteContact(String id){
        contactsProvider.deleteContact(id);
    }
    public void updateContact(ContactObject contactObject){contactsProvider.updateContact(contactObject);}
    public void insertContact(ContactObject contactObject){contactsProvider.insertContact(contactObject);}
}
