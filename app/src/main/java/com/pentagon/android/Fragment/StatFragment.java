package com.pentagon.android.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.pentagon.android.Object.Case;
import com.pentagon.android.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class StatFragment extends Fragment implements OnChartGestureListener, OnChartValueSelectedListener {

    private static final String TAG = "StatFragment";
    private RequestQueue mQueue;
    private ProgressBar mProgress;
    private List<Case> mCasesList;
    private LineChart mChart;
    private Spinner mState, mGender, mAge;
    private TextView mTotal, mDeceased;
    private Button mFilter;
    public String FileName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stat, container, false);
        mQueue = Volley.newRequestQueue(getActivity());
        mCasesList = new ArrayList<>();
        mProgress = v.findViewById(R.id.fs_progress);
        mChart = v.findViewById(R.id.fs_line_chart);
        mState = v.findViewById(R.id.fs_state);
        mGender = v.findViewById(R.id.fs_gender);
        mAge = v.findViewById(R.id.fs_age);
        mTotal = v.findViewById(R.id.fs_total);
        mDeceased = v.findViewById(R.id.fs_deceased);
        mFilter = v.findViewById(R.id.fs_filter);

        Button mDownload = v.findViewById(R.id.fs_download);
        ActivityCompat.requestPermissions((Activity) getContext(),new String[]{WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        mDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TakeScreenshot();

                Toast.makeText(getContext(),"Downloaded",Toast.LENGTH_SHORT).show();
            }
        });

        mFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterActivity();
            }
        });
        mTotal.setTextColor(Color.BLUE);
        mDeceased.setTextColor(Color.GRAY);
        mTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTotal.setTextColor(Color.BLUE);
                mDeceased.setTextColor(Color.GRAY);
                initChart(true);
                jsonParseCases("https://api.jsonbin.io/b/5f5e5a4b7243cd7e823b7764/1");
            }
        });
        mDeceased.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTotal.setTextColor(Color.GRAY);
                mDeceased.setTextColor(Color.BLUE);
                initChart(false);
                jsonParseCases("https://api.jsonbin.io/b/5f5f184dad23b57ef911d4de");
            }
        });
        jsonParseCases("https://api.jsonbin.io/b/5f5e5a4b7243cd7e823b7764/1");
        return v;



    }
    public void TakeScreenshot(){



        Date date = new Date();
        CharSequence now = android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss",date);
        String filename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)  + "/Covrutta/" + now + ".jpg";
        FileName = filename;

        View root = getActivity().getWindow().getDecorView().getRootView();
        root.setDrawingCacheEnabled(true);

        Bitmap bitmap = Bitmap.createBitmap(root.getDrawingCache());
        root.setDrawingCacheEnabled(false);
        File file = new File(filename);
        file.getParentFile().mkdirs();

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            Uri uri = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri,"image/jpeg");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //PDF
        //String new_filename = Directory + now + ".jpg";
        Bitmap bitmap1 = BitmapFactory.decodeFile(filename);
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(1080,2160,1).create();
        PdfDocument.Page page = pdfDocument.startPage(myPageInfo);

        page.getCanvas().drawBitmap(bitmap1,10,20,null);
        pdfDocument.finishPage(page);

        String pdfFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)  + "/Covrutta/" + now + ".pdf";
        File MyPdfFile = new File(pdfFile);
        try {
            pdfDocument.writeTo(new FileOutputStream(MyPdfFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        pdfDocument.close();




    }

    private void filterActivity() {
        String state = mState.getSelectedItem().toString();
        int age = (int) mAge.getSelectedItemId();
        age = age*10;
        String gender = mGender.getSelectedItem().toString();
        List<Case> mFilteredList = new ArrayList<>();
        for (int i=0; i<mCasesList.size(); i++){
            Case aCase = mCasesList.get(i);
            try {
                int ageEst = Integer.valueOf(aCase.getAgeEstimate());
                if ((aCase.getState().equals(state) || mState.getSelectedItem().toString().equals("State")) &&
                        ((age - ageEst > 0 && age - ageEst < 10) || mAge.getSelectedItem().toString().equals("Age")) &&
                        (aCase.getGender().equals(gender) || mGender.getSelectedItem().toString().equals("Gender"))){
                    mFilteredList.add(aCase);
                }
            }catch (Exception e){
                Log.d(TAG, "filterActivity: "+e.getMessage());
            }
        }
        updateChart(mFilteredList);
    }


    private void initChart(boolean isTotal) {

        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);

        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines();

        if (isTotal){
            leftAxis.setAxisMaximum(200f);
        }else {
            leftAxis.setAxisMaximum(10f);
        }
        leftAxis.setAxisMinimum(0f);
        leftAxis.enableGridDashedLine(10f,10f,20f);
        leftAxis.setDrawLimitLinesBehindData(true);

        mChart.getAxisRight().setEnabled(false);
    }

    private void jsonParseCases(String url) {
        mProgress.setVisibility(View.VISIBLE);
        mCasesList.clear();
        final JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i=0; i<response.length(); i++){
                    try {
                        JSONObject object = response.getJSONObject(i);
                        Case newCase = new Case(object.getString("patientId"), object.getString("reportedOn"), object.getString("ageEstimate"), object.getString("gender"), object.getString("state"), object.getString("status"));
                        mCasesList.add(newCase);
                    } catch (JSONException e) {
                        Log.d(TAG, "onResponse: "+e.getMessage());
                    }
                    loadSpinner();
                }
                updateChart(mCasesList);
                mProgress.setVisibility(View.INVISIBLE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: "+error.toString());
                mProgress.setVisibility(View.INVISIBLE);
            }
        });
        mQueue.add(request);
    }

    private void loadSpinner() {
        List<String> stateArray = new ArrayList<>();
        List<String> genderArray = new ArrayList<>();
        List<String> ageArray = new ArrayList<>();
        stateArray.add("State");
        genderArray.add("Gender");
        ageArray.add("Age");
        ageArray.add("0-9");
        ageArray.add("10-19");
        ageArray.add("20-29");
        ageArray.add("30-39");
        ageArray.add("40-49");
        ageArray.add("50-59");
        ageArray.add("60-69");
        ageArray.add("70 and above");
        for (int i=0; i<mCasesList.size(); i++){
            Case aCase = mCasesList.get(i);
            if (!stateArray.contains(aCase.getState())){
                stateArray.add(aCase.getState());
            }
            if (!genderArray.contains(aCase.getGender())){
                genderArray.add(aCase.getGender());
            }
        }
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, stateArray);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mState.setAdapter(stateAdapter);
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, genderArray);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGender.setAdapter(genderAdapter);
        ArrayAdapter<String> ageAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, ageArray);
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAge.setAdapter(ageAdapter);
    }


    private void updateChart(List<Case> mList) {
        ArrayList<Entry> values = new ArrayList<>();
        final ArrayList<String> xLabel = new ArrayList<>();
        int x=0;
        for (int i=0; i<mList.size(); i++) {
            Case aCase = mList.get(i);
            String date = aCase.getReportedOn();
            if (!xLabel.contains(date)){
                xLabel.add(date);
                int y = count(date, mList);
                values.add(new Entry(x, y));
                xLabel.add(date);
                x++;
            }
        }

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(){
            @Override
            public String getFormattedValue(float value) {
                return xLabel.get((int) value);
            }
        });
        xAxis.setLabelRotationAngle(-45);

        LineDataSet set = new LineDataSet(values,"Covid 19 cases" );

        set.setFillAlpha(110);
        set.setColor(Color.BLUE);
        set.setLineWidth(3);
        set.setValueTextSize(10);
        set.setValueTextColor(Color.GRAY);

        ArrayList<ILineDataSet> dataSets = new ArrayList();
        dataSets.add(set);

        LineData data = new LineData(dataSets);
        mChart.setData(data);
        mChart.invalidate();
        mChart.refreshDrawableState();
    }

    private int count(String date, List<Case> mList) {
        int n = 0;
        for (int i=0; i<mList.size(); i++) {
            Case aCase = mList.get(i);
            if (date.equals(aCase.getReportedOn())) {
                n++;
            }
        }
        return n;
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

}
