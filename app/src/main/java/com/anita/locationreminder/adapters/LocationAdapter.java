package com.anita.locationreminder.adapters;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.anita.locationreminder.R;
import com.anita.locationreminder.activities.LocationlistActivity;
import com.anita.locationreminder.activities.MapsActivity;
import com.anita.locationreminder.models.LongLat;
import com.anita.locationreminder.utils.FirebaseUtils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.security.Key;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LocationAdapter extends FirebaseRecyclerAdapter<LongLat,LocationAdapter.LocationViewHolder> {
    private List<LongLat> longLatList;
    private Context context;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public LocationAdapter(@NonNull FirebaseRecyclerOptions<LongLat> options,Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull LocationViewHolder locationViewHolder, final int i, @NonNull final LongLat longLat) {
   locationViewHolder.tvName.setText(longLat.getNames());
   locationViewHolder.iGmap.setOnClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View view) {

       }
   });
   locationViewHolder.iEdit.setOnClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View view) {
           final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.layout_add_alarm);
                final EditText names = dialog.findViewById(R.id.etName);
                final EditText task = dialog.findViewById(R.id.etTask);
                TextView tvLatitude = dialog.findViewById(R.id.tvLatitude);
                TextView tvLongitude = dialog.findViewById(R.id.tvLongitude);
                Button btnAdd = dialog.findViewById(R.id.btnAdd);
                Button btnCancel = dialog.findViewById(R.id.btnCancel);
                names.setText(longLat.getNames());
                task.setText(longLat.getTask());
                tvLatitude.setText("Latitude : " + longLat.getLatitude());
                tvLongitude.setText("Longitude : " + longLat.getLongitude());
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Map<String,Object> map = new HashMap<>();
                        map.put("names",names.getText().toString());
                        map.put("task",task.getText().toString());

                        FirebaseDatabase.getInstance().getReference()
                                .child("LongLat")
                                .child(getRef(i).getKey())
                                .updateChildren(map)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(context, "Alarm updated Successfully", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                });

                    }
                });
                dialog.show();
       }
   });
   locationViewHolder.iDelete.setOnClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View view) {
           FirebaseDatabase.getInstance().getReference()
                   .child("LongLat")
                   .child(getRef(i).getKey())
                   .removeValue()
//                   .setValue(null)
                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {
                           Toast.makeText(context, "Alarm Deleted Successfully", Toast.LENGTH_SHORT).show();
                       }
                   });

       }
   });
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_lonlat, parent, false);
        return new LocationViewHolder(view);
    }

    public class LocationViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private ImageView iGmap,iEdit,iDelete;
        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            iGmap = itemView.findViewById(R.id.iGmap);
            iEdit = itemView.findViewById(R.id.iEdit);
            iDelete = itemView.findViewById(R.id.iDelete);

        }
    }
}

//public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.Holder> {
//    private Context context;
//    private List<LongLat> longLatList;
//    private long maxid = 0;
//
//    public LocationAdapter(Context context, List<LongLat> longLatList) {
//        this.context = context;
//        this.longLatList = longLatList;
//    }
//
//    @NonNull
//    @Override
//    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
//        View v = LayoutInflater.from(context).inflate(R.layout.layout_lonlat, viewGroup, false);
//        return new Holder(v);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull final Holder holder, final int i) {
//        final LongLat longLat = longLatList.get(i);
//
//
//        holder.tvName.setText(longLat.getNames());
//        holder.iEdit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final Dialog dialog = new Dialog(context);
//                dialog.setContentView(R.layout.layout_add_alarm);
//                final EditText etName = dialog.findViewById(R.id.etName);
//                final EditText etTask = dialog.findViewById(R.id.etTask);
//                TextView tvLatitude = dialog.findViewById(R.id.tvLatitude);
//                TextView tvLongitude = dialog.findViewById(R.id.tvLongitude);
//                Button btnAdd = dialog.findViewById(R.id.btnAdd);
//                Button btnCancel = dialog.findViewById(R.id.btnCancel);
//                tvLatitude.setText("Latitude : " + longLat.getLatitude());
//                tvLongitude.setText("Longitude : " + longLat.getLongitude());
//
//                etName.setText(longLat.getNames());
//                etTask.setText(longLat.getTask());
//
//                btnCancel.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        dialog.dismiss();
//                    }
//                });
//                btnAdd.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Map<String,Object> map = new HashMap<>();
//                        map.put("etName",etName.getText().toString());
//                        map.put("etTask",etTask.getText().toString());
//
//                        FirebaseDatabase.getInstance().getReference()
//                                .child("LongLat")
//                                .child(String.valueOf(maxid+1))
////                                .child(getRef(i).getKey())
//                                .updateChildren(map)
//                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        dialog.dismiss();
//                                    }
//                                });
//
////                        FirebaseUtils.database.child("LongLat")
////                                .child("key")
////                                .updateChildren(map)
////                                .addOnCompleteListener(new OnCompleteListener<Void>() {
////                            @Override
////                            public void onComplete(@NonNull Task<Void> task) {
////                                dialog.dismiss();
////                            }
////                        });
//
//
//                    }
//                });
//                dialog.show();
//            }
//        });
//
//        holder.iDelete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return longLatList.size();
//    }
//
//    public class Holder extends RecyclerView.ViewHolder {
//        private TextView tvName;
//        private ImageView iEdit;
//        private ImageView iDelete;
//
//        public Holder(@NonNull View itemView) {
//            super(itemView);
//            tvName = itemView.findViewById(R.id.tvName);
//            iEdit = itemView.findViewById(R.id.iEdit);
//            iDelete = itemView.findViewById(R.id.iDelete);
//        }
//    }
//}
