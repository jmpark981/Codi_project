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
    private TextView show_username, show_id, show_level, cody_count;
    private String ID;
    private Uri imageUri;
    private FirebaseFirestore db;
    private StorageReference mStorageReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.show_account, container, false);
        context=container.getContext();
        ID=getArguments().getString("us_name");     //번들로 ID값 넘겨 받음
        profile_img=view.findViewById(R.id.profile_img);
        plus_icon=view.findViewById(R.id.plus_btn);
        show_username=view.findViewById(R.id.show_username);
        show_id=view.findViewById(R.id.show_id);
        show_level=view.findViewById(R.id.show_level);
        cody_count=view.findViewById(R.id.cody_count);      //코디 추천수 데이터 어디에..?
        delete_move_Btn=view.findViewById(R.id.delete_move_Btn);
        db=FirebaseFirestore.getInstance();
        mStorageReference= FirebaseStorage.getInstance().getReference();
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setUpProfile(ID);
        setUpImage(ID);
        plus_icon.setOnClickListener(new View.OnClickListener() {       //프로필 이미지 변경
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

    private void setUpProfile(String ID) {      //ID를 이용해서 Firestore 접근해서 정보 받아온다.
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

    private void setUpImage(String ID) {        //firebase storage에서 이미지 받아온다
        String path=ID+"_photo"+".jpg";
        mStorageReference.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getActivity()).load(uri).into(profile_img);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Toast.makeText(context, "Picture Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectImage(){     //이미지 갤러리에서 가져옴
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

    private void uploadImage(Uri imageUri) {        //가져온 이미지 firestore에 업로드
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

    private void UpdateImageUrl(String ID, String path) {         //업로드한 이미지 path firebase에 profile_img에 업데이트
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
