package com.example.vishaan.lotteryapp.api;

import android.util.Log;

import com.example.vishaan.lotteryapp.api.parser.IParsable;
import com.example.vishaan.lotteryapp.api.parser.IParser;
import com.example.vishaan.lotteryapp.util.Helper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vishaan on 2/4/2015.
 */
public abstract class AbstractLottery implements IParsable {

    private final static String LOG_TAG = AbstractLottery.class.getSimpleName();

    protected IParser parser;
    protected Map<Integer, Integer> map;
    protected Map<Integer, Integer> mUserInput = new HashMap<>();
    protected int mapSize;
    protected String lottoName;

    public Map<Integer, Integer> getmUserInput() {
        return mUserInput;
    }

    public void setmUserInput(Map<Integer, Integer> mUserInput) {
        this.mUserInput = mUserInput;
        this.performParse();
    }

    protected AbstractLottery(Map<Integer, Integer> userInputArray) {
        this.mUserInput = userInputArray;
    }

    protected abstract IParser createParser(String dataFormat, InputStream inputStream);

    public void performParse() {
        if(this.getParser() == null)
        {
            return;
        }
        Log.v(LOG_TAG, "RAW DATA");
        Integer[][] f = this.getParser().getRawData();
        for(Integer[] i : f) {
            Log.v(LOG_TAG, Arrays.toString(i));
        }

//        Helper.log("output", Arrays.toString(this.getmUserInput().values()));
        Map<Integer, Integer> map = Helper.getMap(LOG_TAG, new ArrayList<>(this.getmUserInput().values()), this.getParser().getRawData());
        this.setMap(map);
        Helper.printMap(LOG_TAG, this.getMap());

        this.setMapSize(this.getMap().size());
        Helper.log(LOG_TAG, String.valueOf(this.getMapSize()));
    }


    public IParser getParser() {
        return this.parser;
    }

    public void setParser(IParser parser) {
        this.parser = parser;
    }

    public Map<Integer, Integer> getMap() {
        return this.map;
    }

    public void setMap(Map<Integer, Integer> map) {
        this.map = map;
    }

    public int getMapSize() {
        return this.mapSize;
    }

    public void setMapSize(int mapSize) {
        this.mapSize = mapSize;
    }

    public String getLottoName() {
        return lottoName;
    }

    public void setLottoName(String lottoName) {
        this.lottoName = lottoName;
    }
}
