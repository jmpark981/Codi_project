package com.example.codi_project;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ShowHome extends Fragment {
    private Context context;
    private View view;
    private TextView test_category, test_profile_ID, test_profile_like;
    private ImageView test_image, test_profile_img;
    private ImageButton test_profile_like_icon;
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
        test_profile_like_icon=view.findViewById(R.id.test_profile_like_icon);
        db=FirebaseFirestore.getInstance();
        mStorageReference= FirebaseStorage.getInstance().getReference(); //storage 경로
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getClothesImage(ID, Clothes_Type, Designer_ID);
        getDesignerProfile(ID, Designer_ID);
        ListUp(full_path1, sports_list);
        //ListUp(full_path2, casual_list);
        //ListUp(full_path3, office_list);

        test_profile_like_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(true){           //이 부분 고쳐야 되는데 모르겠음
                    Log.d("PPPP", "HSSHSSHSH");
                    DesignerLikeUpdate(ID, Designer_ID, 1);
                    test_profile_like_icon.setBackgroundResource(R.drawable.designer_like_icon2);
                }else if(false){        //빈 하트가 나왔을 시 (이것은 안하기로 결정)
                    Log.d("PPPP", "HSSHSSHSH12");
                    DesignerLikeUpdate(ID, Designer_ID, -1);
                    test_profile_like_icon.setBackgroundResource(R.drawable.designer_like_icon);
                }
            }
        });
    }

    private void DesignerLikeUpdate(String ID, String Designer_ID, int adder) {     //하트 누를 시 좋아요 수 업데이트
        String temp_like=test_profile_like.getText().toString();
        Log.d("PPPPP", temp_like);
        long update_count=formatNumberReverse(temp_like)+adder;
        Log.d("PPPPP", Long.toString(update_count));
        String update_like_format=formatNumber(update_count);
        test_profile_like.setText(update_like_format);      ///person/customer/id/jmp/like라는 문서가 없을 시 새로 생성한 후 진행
        db.collection("person")
                .document("customer")
                .collection("id")
                .document(ID).collection("like").document(Designer_ID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    if(adder==-1){      //좋아요 취소 시 like 폴더에서 삭제
                        db.collection("person")
                                .document("customer")
                                .collection("id")
                                .document(ID).collection("like").document(Designer_ID).delete();
                        UpdateDesignerLike(Designer_ID, Long.toString(update_count));
                    }
                }else{
                    db.collection("person")
                            .document("customer")
                            .collection("id")
                            .document(ID).collection("like").document(Designer_ID).set(new HashMap<>());
                    Log.d("PPPTAG", "Document made");
                    Log.d("PPPTAG", "Document exist"+Designer_ID);
                    UpdateDesignerLike(Designer_ID, Long.toString(update_count));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Document error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void UpdateDesignerLike(String Designer_ID, String update_like_format) {  //하트 누를 시 좋아요 수 DB에 업데이트
        Log.d("PPPP", update_like_format);
        db.collection("person")
                .document("designer")
                .collection("id")
                .document(Designer_ID).update("like", update_like_format).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "Like Update Success", Toast.LENGTH_SHORT).show();      //테스트 후 나중에 삭제!
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Like Update Failed", Toast.LENGTH_SHORT).show();
            }
        });
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
        String path=ID+"_"+Clothes_Type+"_"+Designer_ID+".jpg";     // 경로 이후의 이미지 이름
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

    private void getDesignerProfile(String ID, String Designer_ID) {       //디자이너 프로필 가져오기
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
                    getDesignerLikes(ID, Designer_ID);
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

    private void getDesignerLikes(String ID, String Designer_ID) {      //디자이너 게시물이 좋아요를 누른 디자이너꺼라면 꽉채워진 하트를 표시
        Log.d("PPPTAG123", ID+"hahaha getDesignerLikes"+Designer_ID);
        DocumentReference docRef=db.collection("person")
                .document("customer")
                .collection("id").document(ID).collection("like").document(Designer_ID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        test_profile_like_icon.setBackgroundResource(R.drawable.designer_like_icon2);
                        Log.d("PPPTAG123", "Document exists!");
                    } else {
                        test_profile_like_icon.setBackgroundResource(R.drawable.designer_like_icon);
                        Log.d("PPPTAG123", "Document does not exist!");
                    }
                } else {
                    Log.d("PPPTAG123", "Failed with: ", task.getException());
                }
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

    private static String formatNumber(long count) {        //좋아요 숫자 -> 읽기 쉬운 단위로
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f %c", count / Math.pow(1000, exp),"kMGTPE".charAt(exp-1));
    }

    private static Long formatNumberReverse(String count) {     //좋아요 읽기 쉬운 단위로 -> 숫자 표현
        String prefix=count.substring(0, count.length()-2);
        Double prefix_num=Double.parseDouble(prefix);
        Character suffix=count.charAt(count.length()-1);
        if(suffix=='k'){
            return (long)(prefix_num*1000);
        }else if(suffix=='M'){
            return (long)(prefix_num*10000);
        }else if(suffix=='G'){
            return (long)(prefix_num*100000);
        }else if(suffix=='T'){
            return (long)(prefix_num*1000000);
        }else if(suffix=='P'){
            return (long)(prefix_num*10000000);
        }else if(suffix=='E'){
            return (long)(prefix_num*100000000);
        }else{
            return Double.valueOf(prefix_num).longValue();
        }
    }
}
