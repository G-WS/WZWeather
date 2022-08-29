package com.example.wzweather;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.example.wzweather.db.City;
import com.example.wzweather.db.County;
import com.example.wzweather.db.Province;
import com.example.wzweather.util.HttpUtil;
import com.example.wzweather.util.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;

    private TextView titleText;

    private Button backButton;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selectedCity;

    /**
     * 当前选中的级别
     */
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        requireActivity().getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event.getTargetState() == Lifecycle.State.CREATED) {
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                            if (currentLevel == LEVEL_PROVINCE) {
                                selectedProvince = provinceList.get(position);
                                queryCities();
                            } else if (currentLevel == LEVEL_CITY) {
                                selectedCity = cityList.get(position);
                                queryCounties();
                            }
                        }
                    });
                    backButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (currentLevel == LEVEL_COUNTY) {
                                queryCities();
                            } else if (currentLevel == LEVEL_CITY) {
                                queryProvinces();
                            }
                        }
                    });
                    queryProvinces();
                    getLifecycle().removeObserver(this);
                }
            }
        });
    }

    /**
     * 查询全国所有的省份，优先从数据库中查询，如果没有查询到再去服务器上查询
     */
    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = LitePal.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    /**
     * 查询选中省份中的所有市,有限从数据库中区查询，如果没有查询到再去服务器上去查询
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = LitePal.where("provinceid=?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询选中室内所有的县，有限从数据库查询，如果没有查询到再去服务器上查询
     */
//    private void queryCounties() {
//        titleText.setText(selectedCity.getCityName());
//        backButton.setVisibility(View.VISIBLE);
//        countyList = LitePal.where("cityid=?", String.valueOf(selectedCity.getId())).find(County.class);
//        if (countyList.size() > 0) {
//            dataList.clear();
//            for (County county : countyList) {
//                dataList.add(county.getCountyName());
//
//            }
//            adapter.notifyDataSetChanged();
//            listView.setSelection(0);
//            currentLevel = LEVEL_COUNTY;
//        } else {
//            int provinceCode = selectedProvince.getProvinceCode();
//            int cityCode = selectedCity.getCityCode();
//            Log.d("here", String.valueOf(cityCode));
//            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
//            queryFromServer(address, "county");
//        }
//
//    }
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = LitePal.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     *
     * @param address url地址
     * @param type    查询的省市乡类型
     */
//    private void queryFromServer(String address, final String type) {
//        showProgressDialog();
//        HttpUtil.sendOkHttpRequest(address, new Callback() {
//            @SuppressLint("UseRequireInsteadOfGet")
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        closeProgressDialog();
//                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//
//            @SuppressLint("UseRequireInsteadOfGet")
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                String responseText = Objects.requireNonNull(response.body()).string();
//                boolean result = false;
//                if ("province".equals(type)) {
//                    result = Utility.handleProvinceResponse(responseText);
//                } else if ("city".equals(type)) {
//                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
//                } else if ("county".equals(type)) {
//                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
//                }
//                if (result){
//                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            closeProgressDialog();
//                            if ("province".equals(type)){
//                                queryProvinces();
//                            }else if ("city".equals(type)){
//                                queryCities();
//                            }else if ("county".equals(type)){
//                                queryCounties();
//                            }
//                        }
//                    });
//                }
//            }
//        });
//    }
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // 通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }



    /**
     * 显示进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /**
     * 关闭进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
}
