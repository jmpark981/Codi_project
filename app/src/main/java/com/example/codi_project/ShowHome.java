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
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ShowHome extends Fragment {
    private Context context;
    private View view;
    private TextView test_category, test_profile_ID, test_profile_like;
    private ImageView test_image, test_profile_img;
    private Uri imageUri;
    private FirebaseFirestore db;
    private StorageReference mStorageReference;
    private String image_String="jmp_sports_jaylee";
    private String ID, Clothes_Type, Designer_ID;
    //private String ID="jmp", Clothes_Type="sports", Designer_ID ="jaylee";


    private List<String> sports_list= new ArrayList<>();
    private List<String> casual_list= new ArrayList<>();
    private List<String> office_list= new ArrayList<>();
    private String full_path1="디자이너_코디/jmp/sports";     //나중에 intent로 전달 받으면 ID부분 수정
    private String full_path2="디자이너_코디/jmp/casual";
    private String full_path3="디자이너_코디/jmp/office";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.show_home, container, false);
        context=container.getContext();

        String[] temp=image_String.split("_");
        ID=temp[0];
        Clothes_Type=temp[1];
        Designer_ID=temp[2];

        Log.d("PPPTAG", ID+"/"+Clothes_Type+"/"+Designer_ID);

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
        ListUp(full_path1, sports_list);
        //ListUp(full_path2, casual_list);
        //ListUp(full_path3, office_list);
    }



    private void ListUp(String full_path, List<String> list) {          //안에 있는 이미지 싹 다 긁어오기
        mStorageReference.child(full_path).listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                Log.d("PPPTAG99999", "hahahaaha");
                for(StorageReference item: listResult.getItems()){
                    //Log.d("PPPTAG12345", item.getName());
                    list.add(item.getName());
                }
                CheckList(list);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Get List Failed", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void CheckList(List<String> list) {     //잘 출력 하는 지 테스트용
        Log.d("PPPTAG", Integer.toString(sports_list.size()));
        for(String item: sports_list){
            Log.d("PPPTAG", item);
        }
    }




    private void getClothesImage(String ID, String Clothes_Type, String Designer_ID) {      //사진 가져와서 imageview에 넣는 부분
        String path=ID+"_"+Clothes_Type+"_"+Designer_ID+".jpg";     //나중에 path를 부여받으면 윗부분 수정하면 될 듯
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

    private void getDesignerProfile(String Designer_ID) {       //디자이너 프로필 가져오기 
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

    private void getDesignerImage(String Designer_ID) {     //디자이너 이미지 가져오기
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

    private static String formatNumber(long count) {        //좋아요 숫자 표현
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f %c", count / Math.pow(1000, exp),"kMGTPE".charAt(exp-1));
    }

}
