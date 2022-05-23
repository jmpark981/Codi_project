package com.example.codi_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainFrame extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private ShowHome showHome;
    private Bundle bundle;
    private long backpressedTime = 0;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Intent intent=getIntent();
        String us_name=intent.getStringExtra("username");       //ID 이름 넘기기
        bundle = new Bundle();
        bundle.putString("us_name", us_name);
        bottomNavigationView=findViewById(R.id.bottom_Nav);          //하단바
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        bottomNavigationView.setSelectedItemId(R.id.action_home);
        //showHome=new ShowHome();
        //showHome.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new ShowHome()).commit();
    }

    @Override
    public void onBackPressed() {       //뒤로 가기 두번 누르면 종료
        //super.onBackPressed();
        if (System.currentTimeMillis() > backpressedTime + 2000) {
            backpressedTime = System.currentTimeMillis();
            Toast.makeText(MainFrame.this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        } else if (System.currentTimeMillis() <= backpressedTime + 2000) {
            finish();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {       //fragment간 이동
                    Fragment seletedFragment=null;
                    switch(menuItem.getItemId()){
                        case R.id.action_union:
                            seletedFragment = new ShowUnion();
                            seletedFragment.setArguments(bundle);
                            break;
                        case R.id.action_home:
                            seletedFragment = new ShowHome();
                            seletedFragment.setArguments(bundle);
                            break;
                        case R.id.action_person:
                            seletedFragment = new ShowAccount();
                            seletedFragment.setArguments(bundle);
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().
                            replace(R.id.main_frame, seletedFragment).commit();
                    return true;
                }
            };

}
