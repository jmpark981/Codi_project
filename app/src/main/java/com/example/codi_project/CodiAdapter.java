package com.example.codi_project;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CodiAdapter extends RecyclerView.Adapter<CodiAdapter.ViewHolder> {

    private List<CodiItem> mDataList;    // item들의 데이터 저장 공간
    private StorageReference mStorageReference;
    private FirebaseFirestore db;                
    private Context context;
    private String ID;

    public CodiAdapter(List<CodiItem> dataList, String ID) {
        this.ID = ID;
        mDataList = dataList;
        mStorageReference = FirebaseStorage.getInstance().getReference(); //storage 경로
        db=FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public CodiAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {  // view holder 생성
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_frame, parent, false);  // itemframe가져오기

        return new CodiAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {  // 데이터 할당
        context = holder.itemView.getContext();
        CodiItem item = mDataList.get(position);
        holder.category.setText(item.getCategory());
        holder.desi_ID.setText(item.getDesi_ID());

        getLikeNum(item.getDesi_ID(), holder.like_num);
        getImage(item.getCodi_url(), holder.codi_img);  // hj_ver_2 함수 이름 변경해서 적용
        getImage(item.getDesi_url(), holder.desi_img);
    }

    @Override
    public int getItemCount() {  // 아이템 개수
        return mDataList.size();
    }

    // hj_ver_2 함수 이름 변경(원래 getClothesImage 함수)
    private void getImage(String path, ImageView image) {      //사진 가져와서 imageview에 넣는 부분

        mStorageReference.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (context != null) {
                    Log.d("TTT", uri.getPort() + uri.getHost());
                    Glide.with(context).load(uri).into(image);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Picture Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // hj_ver_2 추가 버튼 클릭 업데이트 기능
    private void DesignerLikeUpdate(){
        
    }
    
    // 좋아요 개수 가저오기
    private void getLikeNum(String designerId, TextView likeNum){
        db.collection("person")
                .document("designer")
                .collection("id")
                .document(designerId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    long num=Long.parseLong(documentSnapshot.getString("like"));
                    String num_format=formatNumber(num);
                    likeNum.setText(num_format);
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


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView desi_ID;
        TextView like_num;
        TextView category;
        CircleImageView desi_img;
        ImageView codi_img;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            desi_ID = itemView.findViewById(R.id.desi_profile_ID);
            like_num = itemView.findViewById(R.id.desi_profile_like);
            category = itemView.findViewById(R.id.codi_category);
            desi_img = itemView.findViewById(R.id.desi_profile_img);
            codi_img = itemView.findViewById(R.id.codi_image);
        }
    }

    // hj_ver_2 추가
    private static String formatNumber(long count) {        //좋아요 숫자 -> 읽기 쉬운 단위로
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f %c", count / Math.pow(1000, exp),"kMGTPE".charAt(exp-1));
    }

    // hj_ver_2 추가
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
