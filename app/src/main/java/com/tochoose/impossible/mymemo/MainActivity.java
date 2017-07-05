package com.tochoose.impossible.mymemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private static FirebaseDatabase mFirebaseDatabase;

    static {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabase.setPersistenceEnabled(true);

    }

    private NavigationView mNavigationView;

    private EditText etContent;
    private TextView tvName, tvEmail;
    private String selectedMemoKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            startActivity(new Intent(MainActivity.this, AuthActivity.class));
            finish();
            return;
        }

        etContent = (EditText) findViewById(R.id.et_content);

        FloatingActionButton fabSave = (FloatingActionButton) findViewById(R.id.save_memo);
        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedMemoKey == null) {
                    saveMemo();
                } else {
                    updateMemo();
                }
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });

        FloatingActionButton fabNew = (FloatingActionButton) findViewById(R.id.new_memo);
        fabNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initMemo();
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        View view = mNavigationView.getHeaderView(0);
        tvName = (TextView) view.findViewById(R.id.user_name);
        tvEmail = (TextView) view.findViewById(R.id.user_email);
        mNavigationView.setNavigationItemSelectedListener(this);
        profileUpdate();
        displayMemos();
    }


    private void profileUpdate() {
        tvName.setText(mFirebaseUser.getDisplayName());
        tvEmail.setText(mFirebaseUser.getEmail());
    }

    private void displayMemos() {
        mFirebaseDatabase.getReference("memos/" + mFirebaseUser.getUid())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Memo memo = dataSnapshot.getValue(Memo.class);
                        memo.setKey(dataSnapshot.getKey());
                        displayMemoList(memo);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        Memo memo = dataSnapshot.getValue(Memo.class);
                        memo.setKey(dataSnapshot.getKey());

                        for (int i = 0; i < mNavigationView.getMenu().size(); i++) {
                            MenuItem menuItem = mNavigationView.getMenu().getItem(i);
                            if (memo.getKey().equals(((Memo) menuItem.getActionView().getTag()).getKey())) {
                                menuItem.getActionView().setTag(memo);
                                menuItem.setTitle(memo.getTitle());
                                return;
                            }
                        }
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void displayMemoList(Memo memo) {
        Menu menu = mNavigationView.getMenu();
        MenuItem menuItem = menu.add(memo.getTitle());
        View view = new View(getApplication());
        view.setTag(memo);
        menuItem.setActionView(view);
    }

    private void logout() {
        Snackbar.make(etContent, "로그아웃 할래?", Snackbar.LENGTH_LONG)
                .setAction("로그아웃", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mFirebaseAuth.signOut();
                        finish();
                        startActivity(new Intent(MainActivity.this, AuthActivity.class));
                    }
                }).show();

    }

    private void initMemo() {
        etContent.setText("");
        selectedMemoKey = null;
    }

    private void saveMemo() {
        String text = etContent.getText().toString();
        if (text.isEmpty()) return;
        Memo memo = new Memo();
        memo.setTxt(etContent.getText().toString());
        memo.setCreateDate(new Date());
        mFirebaseDatabase.getReference("memos/" + mFirebaseUser.getUid())
                .push()
                .setValue(memo)
                .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar.make(etContent, "메모가 저장완료 ", Snackbar.LENGTH_LONG).show();
                        initMemo();
                    }
                });
    }

    private void updateMemo() {
        String text = etContent.getText().toString();
        if (text.isEmpty())
            return;
        Memo memo = new Memo();
        memo.setTxt(etContent.getText().toString());
        memo.setCreateDate(new Date());
        mFirebaseDatabase.getReference("memos/" + mFirebaseUser.getUid() + "/" + selectedMemoKey)
                .setValue(memo)
                .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar.make(etContent, "수정완료", Snackbar.LENGTH_LONG).show();
                        initMemo();
                    }
                });


    }

    private void deleteMemo() {
        if (selectedMemoKey == null)
            return;
        Snackbar.make(etContent, "삭제할래?", Snackbar.LENGTH_LONG)
                .setAction("삭제", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mFirebaseDatabase.getReference("memos/" + mFirebaseUser.getUid() + "/" + selectedMemoKey)
                                .removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        Snackbar.make(etContent, "삭제 완료", Snackbar.LENGTH_SHORT).show();
                                        initMemo();
                                    }
                                });

                    }
                }).show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {
            deleteMemo();
        } else if (id == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Memo selectedMemo = (Memo) item.getActionView().getTag();
        selectedMemoKey = selectedMemo.getKey();
        etContent.setText(selectedMemo.getTxt());

        /*if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
