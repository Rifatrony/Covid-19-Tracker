package com.example.covid19tracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.hbb20.CountryCodePicker;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    CountryCodePicker countryCodePicker;
    TextView mTodayTotal, mTotal, mActive, mTodayActive, mRecovered, mTodayRecovered, mDeaths, mTodayDeaths;
    String country;
    TextView mFilter;
    Spinner spinner;
    String[] types = {"cases", "deaths", "recovered", "active"};
    private List<ModelClass> modelClassList;
    private List<ModelClass> modelClassList2;

    ProgressDialog progressDialog;

    PieChart mPieChart;
    RecyclerView recyclerView;
    com.example.covid19tracker.Adapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        countryCodePicker = findViewById(R.id.ccp);
        mTodayActive = findViewById(R.id.todayactivecase);
        mActive = findViewById(R.id.activecase);
        mDeaths = findViewById(R.id.totaldeath);
        mTodayDeaths = findViewById(R.id.todaytotaldeath);
        mRecovered = findViewById(R.id.recovercase);
        mTodayRecovered = findViewById(R.id.todayrecovercase);
        mTotal = findViewById(R.id.totalcase);
        mTodayTotal = findViewById(R.id.todaytotalcase);

        mPieChart = findViewById(R.id.piechart);
        spinner = findViewById(R.id.spinner);
        mFilter = findViewById(R.id.filter);
        recyclerView = findViewById(R.id.recyclerView);

        modelClassList = new ArrayList<>();
        modelClassList2 = new ArrayList<>();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");


        spinner.setOnItemSelectedListener(this);
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(arrayAdapter);


        ApiUtilities.getApiInterface().getCountryData().enqueue(new Callback<List<ModelClass>>() {
            @Override
            public void onResponse(Call<List<ModelClass>> call, Response<List<ModelClass>> response) {
                modelClassList2.addAll(response.body());
                //adapter.notify();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<ModelClass>> call, Throwable t) {
                progressDialog.dismiss();
            }
        });

        adapter = new Adapter(getApplicationContext(), modelClassList2);
        //adapter = new Adapter(getApplicationContext(), modelClassList2);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        countryCodePicker.setAutoDetectedCountry(true);
        country = countryCodePicker.getSelectedCountryName();
        countryCodePicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                country = countryCodePicker.getSelectedCountryName();
                fetchData();
            }
        });

        fetchData();


    }

    private void fetchData() {

        progressDialog.show();
        ApiUtilities.getApiInterface().getCountryData().enqueue(new Callback<List<ModelClass>>() {
            @Override
            public void onResponse(Call<List<ModelClass>> call, Response<List<ModelClass>> response) {
                progressDialog.dismiss();
                modelClassList.addAll(response.body());

                for (int i = 0; i <modelClassList.size(); i++){
                    if (modelClassList.get(i).getCountry().equals(country)){
                        mActive.setText(String.valueOf(modelClassList.get(i).getActive()));
                        mTodayDeaths.setText(String.valueOf(modelClassList.get(i).getTodayDeaths()));
                        mTodayRecovered.setText(String.valueOf(modelClassList.get(i).getTodayRecovered()));
                        mTodayTotal.setText(String.valueOf(modelClassList.get(i).getTodayCases()));
                        mTotal.setText(String.valueOf(modelClassList.get(i).getCases()));
                        mDeaths.setText(String.valueOf(modelClassList.get(i).getDeaths()));
                        mRecovered.setText(String.valueOf(modelClassList.get(i).getRecovered()));

                        int active, total, recovered, deaths;
                        active = Integer.parseInt(modelClassList.get(i).getActive());
                        total = Integer.parseInt(modelClassList.get(i).getCases());
                        recovered = Integer.parseInt(modelClassList.get(i).getRecovered());
                        deaths = Integer.parseInt(modelClassList.get(i).getDeaths());

                        updateGraph(active, total, recovered, deaths);

                    }
                }

            }

            @Override
            public void onFailure(Call<List<ModelClass>> call, Throwable t) {
                progressDialog.dismiss();
            }
        });
    }

    private void updateGraph(int active, int total, int recovered, int deaths) {

        mPieChart.clearChart();

        mPieChart.addPieSlice(new PieModel("Confirm", total, Color.parseColor("#FFB701")));
        mPieChart.addPieSlice(new PieModel("Active", active, Color.parseColor("#FF4CAF50")));
        mPieChart.addPieSlice(new PieModel("Recovered", recovered, Color.parseColor("#38ACCD")));
        mPieChart.addPieSlice(new PieModel("Deaths", deaths, Color.parseColor("#F55c47")));

        mPieChart.startAnimation();

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String item = types[i];
        mFilter.setText(item);
        adapter.filter(item);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}