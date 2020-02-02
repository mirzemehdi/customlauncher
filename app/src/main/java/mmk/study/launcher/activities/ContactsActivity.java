package mmk.study.launcher.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;
import mmk.study.launcher.Common.Common;
import mmk.study.launcher.R;
import mmk.study.launcher.adapters.ContactsAdapter;
import mmk.study.launcher.contentProviders.ContactsProvider;
import mmk.study.launcher.interfaces.ContactItemClicked;
import mmk.study.launcher.objects.ContactObject;
import mmk.study.launcher.repository.ContactsRepository;
import mmk.study.launcher.viewmodel.ContactsViewModel;

public class ContactsActivity extends AppCompatActivity {
    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    public static final int CALL_REQUEST_CODE=2;
    private ContactsViewModel contactsViewModel;
    private RecyclerView contactsRecylerView;
    private ContactsAdapter contactsAdapter;
    private List<ContactObject> contactObjectList = new ArrayList<>();
    private FloatingActionButton addContactButton;
    private AlertDialog addContactDialog;
    private String selectedPhoneNumber="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);


        getPermissions();
        contactsViewModel = ViewModelProviders.of(this).get(ContactsViewModel.class);

        init();
        getAllContactsByString("");
        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dialogAddBtnTxt = getResources().getString(R.string.dialogAddBtnTxt);
                createAlertDialog(dialogAddBtnTxt, Common.TYPE_INSERT, null, null, null);

            }
        });

        List<String> numbers = new ArrayList<>();
        numbers.add("0554765655");
        ContactObject contactObject = new ContactObject("1712", "Coyhuuun(Sintezator)", numbers);
//        insertContact(contactObject);
//        updateContact(contactObject );
        //deleteContact("1712");


//        readContacts();
    }

    private void init() {
        contactsRecylerView = findViewById(R.id.contactsRecylerView);
        contactsRecylerView.setHasFixedSize(true);
        contactsRecylerView.setLayoutManager(new LinearLayoutManager(this));
        contactsAdapter = new ContactsAdapter(this, new ContactItemClicked() {
            @Override
            public void onClick(ContactObject contact) {
                editContact(contact);
            }
        });
        contactsRecylerView.setAdapter(contactsAdapter);
        addSwipeFunctionToRecyclerView(contactsRecylerView);
        addContactButton = findViewById(R.id.addContactFAB);


    }

    private void addSwipeFunctionToRecyclerView(RecyclerView contactsRecylerView) {


        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {
                    ContactObject contactObject = contactObjectList.get(viewHolder.getAdapterPosition());
                    deleteContact(contactObject, viewHolder.getAdapterPosition());
                } else if (direction == ItemTouchHelper.RIGHT) {
                    ContactObject contactObject = contactObjectList.get(viewHolder.getAdapterPosition());
                    callContact(contactObject.getPhoneNumbers().get(0));
                    contactsAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeRightBackgroundColor(getResources().getColor(android.R.color.holo_green_light, null))
                        .addSwipeRightActionIcon(R.drawable.ic_call_black)
                        .addSwipeLeftBackgroundColor(getResources().getColor(android.R.color.holo_red_light, null))
                        .addSwipeLeftActionIcon(R.drawable.ic_delete_white)
                        .create().decorate();

            }
        };


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(contactsRecylerView);
    }

    private void callContact(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
        selectedPhoneNumber=phoneNumber;
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE},CALL_REQUEST_CODE);
            return;
        }
        startActivity(intent);
    }

    private void editContact(ContactObject contact) {
        String dialogBtnTxt=getResources().getString(R.string.dialogUpdateBtnTxt);

        createAlertDialog(dialogBtnTxt,Common.TYPE_UPDATE,contact.getId()
                ,contact.getDisplayName(),contact.getPhoneNumbers().get(0));
    }


    private void createAlertDialog(String positiveBtnTxt,String type,String contactId,String displayName,String phone){

        AlertDialog.Builder builder=new AlertDialog.Builder(ContactsActivity.this);
        final View view=getLayoutInflater().inflate(R.layout.contact_dialog_view,null);

        TextView titleTextView=view.findViewById(R.id.contact_dialog_title_tv);
        if (type.equals(Common.TYPE_INSERT)) titleTextView.setText(getResources().getString(R.string.dialogTitleInsert));
        else if (type.equals(Common.TYPE_UPDATE)) titleTextView.setText(getResources().getString(R.string.dialogTitleUpdate));

        Button cancelBtn=view.findViewById(R.id.contact_dialog_cancel_btn);
        Button addBtn=view.findViewById(R.id.contact_dialog_add_btn);
        addBtn.setText(positiveBtnTxt);
        final EditText nameEditText=view.findViewById(R.id.contact_dialog_name_editText);
        final EditText phoneEditText=view.findViewById(R.id.contact_dialog_phone_editText);
        if (displayName!=null) nameEditText.setText(displayName);
        if (phone!=null) phoneEditText.setText(phone);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type.equals(Common.TYPE_INSERT))
                    insertContact(nameEditText.getText().toString(),phoneEditText.getText().toString(),view);
                else if (type.equals(Common.TYPE_UPDATE))
                    updateContact(contactId,nameEditText.getText().toString(),phoneEditText.getText().toString(),view);
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContactDialog.dismiss();
            }
        });

        builder.setView(view);
        addContactDialog=builder.create();
        addContactDialog.setCancelable(false);
        addContactDialog.show();

    }

    private void updateContact(String id,String name,String phone,View dialogView) {

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(phone)){

            if (!getPermissions())
                return ;
            List<String> numbers=new ArrayList<>();
            numbers.add(phone);
            ContactObject contactObject=new ContactObject(id,name,numbers);
            contactsViewModel.updateContact(contactObject);
            Toast.makeText(this, getResources().getString(R.string.updateContactSuccess), Toast.LENGTH_SHORT).show();
            addContactDialog.dismiss();

        }
        else
            Snackbar.make(dialogView,getResources().getString(R.string.allFieldsNeed),Snackbar.LENGTH_SHORT).show();
    }

    private void insertContact(String name,String phone,View dialogView) {

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(phone)){

            if (!getPermissions())
                return ;
            List<String> numbers=new ArrayList<>();
            numbers.add(phone);
            ContactObject contactObject=new ContactObject(name,numbers);
            contactsViewModel.insertContact(contactObject);
            Toast.makeText(this, getResources().getString(R.string.addContactSuccess), Toast.LENGTH_SHORT).show();
            addContactDialog.dismiss();

        }
        else
            Snackbar.make(dialogView,getResources().getString(R.string.allFieldsNeed),Snackbar.LENGTH_SHORT).show();


    }

    private void deleteContact(ContactObject contactObject,int position) {
        if (!getPermissions())
            return ;
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.dialogTitleDelete));
        String message=getResources().getString(R.string.dialogDeleteMsgPart1)+" "+contactObject.getDisplayName()
                +" " +getResources().getString(R.string.dialogDeleteMsgPart2);
        builder.setMessage(message);

        builder.setPositiveButton(getResources().getString(R.string.dialogDeleteBtnTxt), (dialog, which) -> {
            contactsViewModel.deleteContact(contactObject.getId());
            contactsAdapter.notifyItemChanged(position);

        });
        builder.setNegativeButton(getResources().getString(R.string.dialogCancelBtnTxt), (dialog, which) -> {
            addContactDialog.dismiss();
            contactsAdapter.notifyItemChanged(position);
        });
        addContactDialog=builder.create();
        addContactDialog.show();


    }

    private void getAllContactsByString(String searchString) {

        if (!getPermissions())
            return ;


        contactsViewModel.getContactsByString(searchString).observe(this, new Observer<List<ContactObject>>() {
            @Override
            public void onChanged(List<ContactObject> contactObjects) {
                contactsAdapter.setContactObjectList(contactObjects);
                contactObjectList=contactObjects;
                Log.d("onChangeContacts","GetAllContacts Activity Called");
                Log.d("MyContacts: ","onChanged");
                for (ContactObject contactObject: contactObjects){
                    Log.d("MyContact: ",contactObject.toString());
                }

            }
        });


    }

    private boolean getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                &&checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
            return false;

        }
        return true;

    }


   /* public void readContacts(){


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            return ;

        }


        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur!=null&&cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    System.out.println("name : " + name + ", ID : " + id);

                    // get the phone number
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phone = pCur.getString(
                                pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        System.out.println("phone" + phone);
                    }
                    pCur.close();


                    // get email and type

                    Cursor emailCur = cr.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (emailCur.moveToNext()) {
                        // This would allow you get several email addresses
                        // if the email addresses were stored in an array
                        String email = emailCur.getString(
                                emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        String emailType = emailCur.getString(
                                emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));

                        System.out.println("Email " + email + " Email Type : " + emailType);
                    }
                    emailCur.close();

                    // Get note.......
                    String noteWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                    String[] noteWhereParams = new String[]{id,
                            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};
                    Cursor noteCur = cr.query(ContactsContract.Data.CONTENT_URI, null, noteWhere, noteWhereParams, null);
                    if (noteCur.moveToFirst()) {
                        String note = noteCur.getString(noteCur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
                        System.out.println("Note " + note);
                    }
                    noteCur.close();

                    //Get Postal Address....

                    String addrWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                    String[] addrWhereParams = new String[]{id,
                            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};
                    Cursor addrCur = cr.query(ContactsContract.Data.CONTENT_URI,
                            null, null, null, null);
                    while(addrCur.moveToNext()) {
                        String poBox = addrCur.getString(
                                addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));
                        String street = addrCur.getString(
                                addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
                        String city = addrCur.getString(
                                addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
                        String state = addrCur.getString(
                                addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
                        String postalCode = addrCur.getString(
                                addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
                        String country = addrCur.getString(
                                addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
                        String type = addrCur.getString(
                                addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));

                        // Do something with these....

                    }
                    addrCur.close();

                    // Get Instant Messenger.........
                    String imWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                    String[] imWhereParams = new String[]{id,
                            ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE};
                    Cursor imCur = cr.query(ContactsContract.Data.CONTENT_URI,
                            null, imWhere, imWhereParams, null);
                    if (imCur.moveToFirst()) {
                        String imName = imCur.getString(
                                imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));
                        String imType;
                        imType = imCur.getString(
                                imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.TYPE));
                    }
                    imCur.close();

                    // Get Organizations.........

                    String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                    String[] orgWhereParams = new String[]{id,
                            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};
                    Cursor orgCur = cr.query(ContactsContract.Data.CONTENT_URI,
                            null, orgWhere, orgWhereParams, null);
                    if (orgCur.moveToFirst()) {
                        String orgName = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
                        String title = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
                    }
                    orgCur.close();
                }
            }
            cur.close();
        }
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==PERMISSIONS_REQUEST_READ_CONTACTS){
            if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED
                    &&grantResults[1]==PackageManager.PERMISSION_GRANTED){
                getAllContactsByString("");
            }
            else
                Toast.makeText(this, getResources().getString(R.string.readingContactsPermissionDenied), Toast.LENGTH_SHORT).show();
        }
        else if (requestCode==CALL_REQUEST_CODE){
            if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                callContact(selectedPhoneNumber);
            }
            else
                Toast.makeText(this, getResources().getString(R.string.readingContactsPermissionDenied), Toast.LENGTH_SHORT).show();
        }
    }
}
