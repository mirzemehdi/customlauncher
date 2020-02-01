package mmk.study.launcher.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import mmk.study.launcher.R;
import mmk.study.launcher.interfaces.ContactItemClicked;
import mmk.study.launcher.objects.ContactObject;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private Context context;
    private List<ContactObject> contactObjectList=new ArrayList<>();
    private ContactItemClicked IContactItemClicked;

    public ContactsAdapter(Context context,ContactItemClicked contactItemClicked) {
        this.context = context;
        this.IContactItemClicked=contactItemClicked;

    }

    @NonNull
    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_contact,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsAdapter.ViewHolder holder, int position) {
        ContactObject contactObject=contactObjectList.get(position);
        holder.contactName.setText(contactObject.getDisplayName());
    }

    @Override
    public int getItemCount() {
        return contactObjectList.size();
    }

    public void setContactObjectList(List<ContactObject>contactObjectList){
        this.contactObjectList=contactObjectList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView contactName;
        private LinearLayout contactView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            contactName=itemView.findViewById(R.id.contactNameTxtView);
            contactView=itemView.findViewById(R.id.contactView);
            contactView.setOnClickListener(v -> {
                ContactObject contactObject=contactObjectList.get(getAdapterPosition());
                IContactItemClicked.onClick(contactObject);
           });
        }

    }
}
