package com.example.codi_project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowAccount extends Fragment {
    private Context context;
    private View view;
    private CircleImageView profile_img;
    private ImageButton plus_icon;
    private AppCompatButton delete_move_Btn;
    private TextView show_username, show_id, show_level, level;
    private ImageView trophy;
    private RelativeLayout membership_rectangle;
    private String ID;
    private Uri imageUri;
    private FirebaseFirestore db;
    private StorageReference mStorageReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.show_account, container, false);
        context=container.getContext();
        ID=getArguments().getString("us_name");     //????????? ID??? ?????? ??????
        profile_img=view.findViewById(R.id.profile_img);
        plus_icon=view.findViewById(R.id.plus_btn);
        show_username=view.findViewById(R.id.show_username);
        show_id=view.findViewById(R.id.show_id);
        show_level=view.findViewById(R.id.show_level);
        delete_move_Btn=view.findViewById(R.id.delete_move_Btn);
        trophy=view.findViewById(R.id.trophy);
        level=view.findViewById(R.id.level);
        membership_rectangle=view.findViewById(R.id.membership_rectangle);
        db=FirebaseFirestore.getInstance();
        mStorageReference= FirebaseStorage.getInstance().getReference();
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setUpProfile(ID);
        setUpImage(ID);
        plus_icon.setOnClickListener(new View.OnClickListener() {       //????????? ????????? ??????
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        delete_move_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                movetoDelete(ID);
            }
        });
    }

    private void movetoDelete(String id) {
        Intent intent = new Intent(getActivity(), DeleteAccountActivity.class);
        intent.putExtra("username", ID);
        startActivity(intent);
    }

    private void setUpProfile(String ID) {      //ID??? ???????????? Firestore ???????????? ?????? ????????????.
        db.collection("person")
                .document("customer")
                .collection("id")
                .document(ID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    String membership=documentSnapshot.getString("membership");
                    String name=documentSnapshot.getString("name");
                    show_username.setText(name);
                    show_id.setText("@"+ID);
                    show_level.setText("#"+membership);
                    setUpMemberShip(membership);
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

    private void setUpMemberShip(String membership) {       //membership??? ?????? ?????? ??????
        if(membership.equals("VIP")){
            trophy.setBackgroundResource(R.drawable.trophy_background2);
            level.setText(membership);
            membership_rectangle.setBackgroundResource(R.drawable.rectangle_7);
        }else{
            trophy.setBackgroundResource(R.drawable.trophy_background);
            level.setText(membership);
            membership_rectangle.setBackgroundResource(R.drawable.rectangle_5);
        }
    }

    private void setUpImage(String ID) {        //firebase storage?????? ????????? ????????????
        String path=ID+"_photo"+".jpg";
        mStorageReference.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (getActivity() != null) {
                    Glide.with(getActivity()).load(uri).into(profile_img);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Toast.makeText(context, "Picture Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectImage(){     //????????? ??????????????? ?????????
        Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100){
            if(resultCode==Activity.RESULT_OK){
                imageUri=data.getData();
                profile_img.setImageURI(imageUri);
                uploadImage(imageUri);
            }
        }
    }

    private void uploadImage(Uri imageUri) {        //????????? ????????? firestore??? ?????????
        String path=ID+"_photo"+".jpg";
        StorageReference fileRef=mStorageReference.child(path);
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                UpdateImageUrl(ID, path);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Upload Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void UpdateImageUrl(String ID, String path) {         //???????????? ????????? path firebase??? profile_img??? ????????????
        db.collection("person")
                .document("customer")
                .collection("id")
                .document(ID).update("profile_img", "gs://codiproject-5d37e.appspot.com/"+path).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "Update Success", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Update Path Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
