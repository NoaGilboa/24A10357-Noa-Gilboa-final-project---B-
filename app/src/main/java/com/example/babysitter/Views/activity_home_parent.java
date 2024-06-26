package com.example.babysitter.Views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.babysitter.Adpters.BabysitterAdapter;
import com.example.babysitter.Models.Babysitter;
import com.example.babysitter.Models.Parent;
import com.example.babysitter.R;
import com.example.babysitter.Utilities.FirebaseDataManager;
import com.example.babysitter.Utilities.FirebaseUserManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class activity_home_parent extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BabysitterAdapter adapter;
    private List<Babysitter> babysitters;

    private FirebaseDataManager dataManager;
    private FirebaseUserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_parent);

        userManager = new FirebaseUserManager();
        dataManager = new FirebaseDataManager();

        recyclerView = findViewById(R.id.rvBabysitters);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        babysitters = new ArrayList<>();
        adapter = new BabysitterAdapter(babysitters, this);
        recyclerView.setAdapter(adapter);

        loadBabysitters();

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            userManager.logOutUser();
            startActivity(new Intent(activity_home_parent.this, activity_login.class));
            finish();
        });
        findViewById(R.id.btnSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity_home_parent.this, activity_setting.class));
            }
        });

        findViewById(R.id.btnSortByExperience).setOnClickListener(v -> {
            Collections.sort(babysitters, (b1, b2) -> Double.compare(b2.getExperience(), b1.getExperience()));
            adapter.notifyDataSetChanged(); // Notify the adapter to refresh the UI.
        });

        findViewById(R.id.btnSortByHourlyWage).setOnClickListener(v -> {
            Collections.sort(babysitters, (b1, b2) -> Double.compare(b2.getHourlyWage(), b1.getHourlyWage()));
            adapter.notifyDataSetChanged(); // Notify the adapter to refresh the UI.
        });

        findViewById(R.id.btnSortByDistance).setOnClickListener(v -> {
            sortBabysittersByDistance();
            adapter.notifyDataSetChanged(); // Notify the adapter to refresh the UI.
        });

    }

    private void sortBabysittersByDistance() {
        if (userManager.isUserLoggedIn()) {
            String currentUserId = userManager.getCurrentUserId();
            dataManager.sortBabysittersByDistance(currentUserId, new ArrayList<>(babysitters), new FirebaseDataManager.OnBabysittersSortedListener() {
                @Override
                public void onSorted(List<Babysitter> sortedBabysitters) {
                    if (!sortedBabysitters.isEmpty()) {
                        babysitters.clear();
                        babysitters.addAll(sortedBabysitters);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(activity_home_parent.this, "Babysitters sorted by distance", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity_home_parent.this, "No babysitters available or error in sorting", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Exception exception) {
                    Toast.makeText(activity_home_parent.this, "Error sorting babysitters: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }
    private void loadBabysitters() {
        dataManager.loadAllBabysitters(new FirebaseDataManager.OnBabysittersLoadedListener() {
            @Override
            public void onBabysittersLoaded(List<Babysitter> loadedBabysitters) {
                babysitters.clear();
                babysitters.addAll(loadedBabysitters);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(activity_home_parent.this, "Failed to load babysitters: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

