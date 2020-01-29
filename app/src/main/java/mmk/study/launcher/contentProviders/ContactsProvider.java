package mmk.study.launcher.contentProviders;

import android.Manifest;
import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.github.tamir7.contacts.Contact;
import com.github.tamir7.contacts.Contacts;
import com.github.tamir7.contacts.PhoneNumber;
import com.github.tamir7.contacts.Query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import mmk.study.launcher.objects.ContactObject;

public class ContactsProvider {

    private ContentResolver cr;

    public ContactsProvider(Application context) {
        this.cr = context.getContentResolver();
        Contacts.initialize(context);
    }

    public LiveData<List<ContactObject>> getAllContacts() {

        MutableLiveData<List<ContactObject>> mutableContactList = new MutableLiveData<>();
        List<ContactObject> contactObjectList = new ArrayList<>();
        List<Contact>contacts= Contacts.getQuery().hasPhoneNumber().find();

        for (Contact contact:contacts) {
            ContactObject contactObject=new ContactObject();
            contactObject.setId(String.valueOf(contact.getId()));
            contactObject.setDisplayName(contact.getDisplayName());
            List<PhoneNumber>phoneNumbers=contact.getPhoneNumbers();
            for (PhoneNumber phoneNumber:phoneNumbers){
                contactObject.addPhoneNumber(phoneNumber.getNormalizedNumber());
            }
            contactObjectList.add(contactObject);
        }

        mutableContactList.setValue(contactObjectList);


        return mutableContactList;
    }

    public LiveData<List<ContactObject>> getContactsByString(String searchString) {


        MutableLiveData<List<ContactObject>> mutableContactList = new MutableLiveData<>();
        List<ContactObject> contactObjectList = new ArrayList<>();

        Query mainQuery = Contacts.getQuery().hasPhoneNumber();
        Query q1 = Contacts.getQuery().hasPhoneNumber();
        q1.whereStartsWith(Contact.Field.DisplayName, searchString);
        Query q2 = Contacts.getQuery().hasPhoneNumber();
        q2.whereStartsWith(Contact.Field.PhoneNormalizedNumber, searchString);
        List<Query> qs = new ArrayList<>();
        qs.add(q1);
        qs.add(q2);
        mainQuery.or(qs);
        List<Contact> contacts = mainQuery.find();

        for (Contact contact:contacts) {
            ContactObject contactObject=new ContactObject();
            contactObject.setId(String.valueOf(contact.getId()));
            contactObject.setDisplayName(contact.getDisplayName());
            List<PhoneNumber>phoneNumbers=contact.getPhoneNumbers();
            for (PhoneNumber phoneNumber:phoneNumbers){
                contactObject.addPhoneNumber(phoneNumber.getNormalizedNumber());
            }
            contactObjectList.add(contactObject);
        }

        mutableContactList.setValue(contactObjectList);


        return mutableContactList;

//        MutableLiveData<List<ContactObject>> contactList = new MutableLiveData<>();
//
//        String SELECTION = ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?";
//
//        String[] selectionArgs = {searchString + "%"};
//
//        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
//                null, SELECTION, selectionArgs, ContactsContract.Contacts.DISPLAY_NAME + " ASC");
//
//        List<ContactObject> contactObjectList = new ArrayList<>();
//
//        if (cur != null && cur.getCount() > 0) {
//
//            while (cur.moveToNext()) {
//                ContactObject contactObject = new ContactObject();
//                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
//                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//
//                contactObject.setId(id);
//                contactObject.setDisplayName(name);
//                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
//                    // get the phone number
//                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
//                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
//                            new String[]{id}, null);
//                    if (pCur != null && pCur.getCount() > 0) {
//
//
//                        while (pCur.moveToNext()) {
//
//                            String phone = pCur.getString(
//                                    pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                            contactObject.addPhoneNumber(phone);
//                        }
//                        pCur.close();
//                    }
//
//
//                }
//                contactObjectList.add(contactObject);
//            }
//            cur.close();
//        }
//
//        contactList.setValue(contactObjectList);
//        return contactList;
    }

    public void deleteContact(String id) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        String[] args = new String[]{id};

        ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(ContactsContract.RawContacts.CONTACT_ID + "=?", args).build());
        try {
            cr.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public void updateContact(ContactObject contactObject) {



        String where = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND "
                + ContactsContract.Data.MIMETYPE + " = ?";

        String[] nameParams = new String[]{contactObject.getId(),
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
        String[] numberParams = new String[]{contactObject.getId(),
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};

        final ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        if (!contactObject.getDisplayName().isEmpty()) {
            ops.add(android.content.ContentProviderOperation.newUpdate(
                    android.provider.ContactsContract.Data.CONTENT_URI)
                    .withSelection(where, nameParams)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            contactObject.getDisplayName()).build());
        }
        if (!contactObject.getPhoneNumbers().get(0).isEmpty()) {
            ops.add(android.content.ContentProviderOperation.newUpdate(
                    android.provider.ContactsContract.Data.CONTENT_URI)
                    .withSelection(where, numberParams)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contactObject.getPhoneNumbers().get(0))
                    .build());
        }
        try {
            cr.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }


//
    }

    public void insertContact(ContactObject contactObject) {
        ArrayList<ContentProviderOperation> contentProviderOperations = new ArrayList();
        //insert raw contact using RawContacts.CONTENT_URI
        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null).withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());
        //insert contact display name using Data.CONTENT_URI
        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contactObject.getDisplayName()).build());
        //insert mobile number using Data.CONTENT_URI
        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contactObject.getPhoneNumbers().get(0)).withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());
        try {
            cr.applyBatch(ContactsContract.AUTHORITY, contentProviderOperations);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }
}
