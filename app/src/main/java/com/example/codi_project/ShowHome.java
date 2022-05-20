package com.example.codi_project;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ShowHome extends Fragment {
    private Context context;
    private View view;
    private TextView test_category, test_profile_ID, test_profile_like;
    private ImageView test_image, test_profile_img;
    private Uri imageUri;
    private FirebaseFirestore db;
    private StorageReference mStorageReference;
    private String ID="jmp", Clothes_Type="sports", Designer_ID ="jaylee";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.show_home, container, false);
        context=container.getContext();
        test_category=view.findViewById(R.id.test_category);
        test_image=view.findViewById(R.id.test_image);
        test_profile_img=view.findViewById(R.id.test_profile_img);
        test_profile_ID=view.findViewById(R.id.test_profile_ID);
        test_profile_like=view.findViewById(R.id.test_profile_like);
        db=FirebaseFirestore.getInstance();
        mStorageReference= FirebaseStorage.getInstance().getReference();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getClothesImage(ID, Clothes_Type, Designer_ID);
        getDesignerProfile(Designer_ID);
    }

    private void getClothesImage(String ID, String Clothes_Type, String Designer_ID) {
        String path=ID+"_"+Clothes_Type+"_"+Designer_ID+".jpg";
        Log.d("PPPTAG123", path);
        mStorageReference.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d("PPPTAG123", "hahaah123");
                if (getActivity() != null) {
                    Glide.with(getActivity()).load(uri).into(test_image);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Picture Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDesignerProfile(String Designer_ID) {
        db.collection("person")
                .document("designer")
                .collection("id")
                .document(Designer_ID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    long like=Long.parseLong(documentSnapshot.getString("like"));
                    String like_format=formatNumber(like);
                    test_profile_ID.setText(Designer_ID);
                    test_profile_like.setText(like_format);
                    getDesignerImage(Designer_ID);
                }else{
                    Toast.makeText(context, "Document not exists", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Document error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDesignerImage(String Designer_ID) {
        String path=Designer_ID+"_photo"+".jpg";
        mStorageReference.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (getActivity() != null) {
                    Glide.with(getActivity()).load(uri).into(test_profile_img);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Picture Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static String formatNumber(long count) {
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f %c", count / Math.pow(1000, exp),"kMGTPE".charAt(exp-1));
    }

}
