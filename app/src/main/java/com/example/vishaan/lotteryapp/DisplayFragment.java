package com.example.vishaan.lotteryapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;

import com.example.vishaan.lotteryapp.api.AbstractDrawing;
import com.example.vishaan.lotteryapp.api.AbstractLottery;
import com.example.vishaan.lotteryapp.api.PowerBallLottery;
import com.example.vishaan.lotteryapp.api.chart.AbstractChart;
import com.example.vishaan.lotteryapp.api.chart.LotteryChart;
import com.example.vishaan.lotteryapp.db.LotteryDataSource;
import com.example.vishaan.lotteryapp.services.FetchLottoData;

import org.achartengine.GraphicalView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DisplayFragment extends Fragment {

    private static final String LOG_TAG = DisplayFragment.class.getSimpleName();

    private AbstractLottery currentLotto;
    private ArrayList<AbstractLottery> arrLotteries = new ArrayList<>();
    private AbstractChart chart = new LotteryChart();
    private GraphicalView graphicalView;
    private ArrayList<NumberPicker> numberPickers;
    private Map<Integer, Integer> mUserInput = new HashMap();
    private int maxNumbers = 6;
    private Context mContext;
    private LotteryDataSource mDataSource;

    //Constants
    private static final String[] CHART_AXIS_LABELS = {"Choose your number", "Frequency"};

    public DisplayFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.displayfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.action_refresh:
                this.updateData(this.currentLotto);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_display, container, false);
        this.mContext = inflater.getContext();

        mDataSource = new LotteryDataSource(mContext);
        mDataSource.open();

        List<AbstractDrawing> drawings = mDataSource.getPowerBalls(null, null);

        AbstractLottery powerBall = new PowerBallLottery(this.mContext, mUserInput);
        powerBall.setDrawings(drawings);
        this.arrLotteries.add(powerBall);
        this.currentLotto = this.arrLotteries.get(0);

        //set up the lottery selection functionality and charts
        ArrayList<String> strLotteries = new ArrayList<>();
        Iterator<AbstractLottery> iterator = this.arrLotteries.iterator();
        while (iterator.hasNext()) {
            strLotteries.add(iterator.next().getLottoName());
        }

        Spinner spinner = (Spinner) rootView.findViewById(R.id.spnChooseLotto);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity().getApplicationContext(), R.layout.custom_spinner_item, strLotteries);
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DisplayFragment.this.currentLotto = DisplayFragment.this.arrLotteries.get(i);
                resetNumbers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        Button resetButton = (Button) rootView.findViewById(R.id.btnReset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetNumbers();
            }
        });

        return rootView;
    }

    private void resetNumbers() {
        mUserInput.clear();
        this.currentLotto.setUserInput(mUserInput);
        for(NumberPicker picker : this.numberPickers) {
            picker.setValue(0);
            picker.setEnabled(false);
        }

        NumberPicker firstPicker = this.numberPickers.get(0);
        firstPicker.setEnabled(true);
        firstPicker.requestFocus();
        this.addChartView(this.mContext);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //set up the number choosing UI
        maxNumbers = 6;
        this.numberPickers = new ArrayList<>(maxNumbers);
        LinearLayout linLayout = (LinearLayout) getActivity().findViewById(R.id.linLayout2_pickers);
        NumberPicker picker;
        for(int i=0; i<maxNumbers; i++) {
            picker = new NumberPicker(getActivity().getApplicationContext());
            picker.setOrientation(LinearLayout.HORIZONTAL);
            picker.setMinValue(1);
            picker.setMaxValue(69);
            picker.setEnabled(false);
            picker.setTag(i);

            linLayout.addView(picker);
            this.numberPickers.add(picker);

            picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    int offSet = (int) (picker.getTag());
                    mUserInput.put(offSet, newVal);

                    if (offSet < maxNumbers) {
                        DisplayFragment.this.numberPickers.get(++offSet).setEnabled(true);
                    }

                    DisplayFragment.this.currentLotto.setUserInput(mUserInput);
                    DisplayFragment.this.addChartView(getActivity().getApplicationContext());
                }
            });
        }
        this.numberPickers.get(0).setEnabled(true);

        this.updateData(this.currentLotto);
        this.addChartView(getActivity());
    }

    private void addChartView(Context context) {
        //get chart view
        LinearLayout linLayout = (LinearLayout) getActivity().findViewById(R.id.linLayout_chart);
        if (this.graphicalView != null) {
            linLayout.removeView(this.graphicalView);
        }
        String[] legends = {getResources().getString(R.string.app_name)};
        this.graphicalView = (GraphicalView) this.chart.buildChart(context, this.currentLotto.getMap(), this.currentLotto.getLottoName(), legends, CHART_AXIS_LABELS);

        linLayout.addView(this.graphicalView, new ViewGroup.LayoutParams
                (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

    }

    private void updateData(AbstractLottery lottery) {
        FetchLottoData.start(mContext);
        this.addChartView(getActivity());
    }

}
