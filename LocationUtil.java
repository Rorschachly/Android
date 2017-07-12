package com.ruipai.location;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ruipai.util.FakeX509TrustManger;

public class LocationUtil implements AMapLocationListener {
	private final String TAG = "LocationUtil";
	private static LocationUtil mLocationUtil = null;
	private Context mContext;
	private MHandler mHandler;
	private ArrayList<UpdateLocationText> callbacks = new ArrayList<UpdateLocationText>();
	private ArrayList<UpdateNearList> callViewListbacks = new ArrayList<UpdateNearList>();
	private ArrayList<UpdateRecamContent> recamContentCallbacks = new ArrayList<UpdateRecamContent>();
	private final int LOCATION_START = 0;
	private final int LOCATION_SUCCESS = 1;
	private final int GD_LOCATION_START = 2;
	private final int SAVE_DATA = 3;
	private final int UPDATE_ADDR = 4;
	private final int GET_TIANQI = 5;
	private final String SPF = "lastVisit";
	private final String DS_PA_LO = "displayPRLO";
	private AMapLocationClient locationClient = null;
	private AMapLocationClientOption locationOption = null;
	public static RequestQueue mQueue = null;
	private boolean TEST = false;// 是否测试环境
	private String mLong = "";
	private String mLat = "";
	private final String txKey = "JRPBZ-WUCKF-VHVJ7-JDWRD-3MXDJ-2MBPL";
	// c26c93203ca56a020440dcc2e6d383f5
	private final String gdKey = "202c02e4bc3a2d71fc421600e2a3464";
	private String userid = "";
	private String weixin = "";
	private String weibo = "";
	private String app = "2";// 安卓端
	// map parameters
	// 高德参数
	private String a0_g = "";
	private String a1_g = "";
	private String a2_g = "";
	private String a3_g = "";
	private String a4_g = "";
	private String a5_1_g = "";
	private String a5_2_g = "";
	private String keywords_g = "";
	private String category_g = "";
	// 腾讯参数
	private String a0_t = "";
	private String a1_t = "";
	private String a2_t = "";
	private String a3_t = "";
	private String a5_1_t = "";
	private String a5_2_t = "";
	private String keywords_t = "";
	private String category_t = "";
	// 其他参数
	private String x_xz = "0";
	private String act_from = "1";

	// 20170118 add
	private String fangxiang = "0";// 方向
	private String haiba = "0";// 海拔
	private String fenbei = "0";// 分贝
	private String tianqi = "";// 天气
	private String shebei = "";// 设备
	private final String gdtxUrl ="https://app.recam.cn/api/new/api_content.php?";
	private String gd_tx_Url = "https://app.recam.cn/api/new/api_content.php?";
//	private final String gdtxUrl = "http://192.168.1.100:8086/api/new/api_content.php?";
//	private String gd_tx_Url = "http://192.168.1.100:8086/api/new/api_content.php?";
	// P接口
	private final String recamUrl = "http://192.168.1.100:8086/api/new/api_postinfo.php?";
	private String recam_Url = "http://192.168.1.100:8086/api/new/api_postinfo.php?";
	private String near_marks = "";
	private String lastaddr = "";
	//是否有缓存的经纬度
	private String preLang;
	private String preLat;
	private String xingzheng = "";
	private String addressDetail = "";
	private String x_keywords = "";
	private String timestamp = String.valueOf(System.currentTimeMillis());
	private String x_category = "";
	private String x_map = "0";
	private double x_distance = 0.1;
	private String photoid = "";
	private String old_photoid = "0";
	private String templet_id = "0";
	private String contentid = "0";
	private String pre_con_unionid = "0";
	private String place_unionid = "0";
	private String list_unionid = "0";
	private String pic_date= "0";
	private final String keyAll = "12345_e3ol" + timestamp;
	private String key_All = "12345_e3ol" + timestamp;
	private String keyMd5 = "";
	private String setup_upshow = "1";// 显示上级内容
	private int cacheN = 1;
	//修正坐标后多久不连地图
	private int fixtime = 180000;

	static List<ResultGt> poiList1 = new ArrayList<ResultGt>();
	static List<ResultGt> poiList2 = new ArrayList<ResultGt>();

	private LocationUtil(Context context) {
		mContext = context;
	}

	public static LocationUtil getInstance(Context context) {
		if (mLocationUtil == null) {
			mLocationUtil = new LocationUtil(context.getApplicationContext());
			mQueue = Volley.newRequestQueue(context);
		}
		return mLocationUtil;
	}

	public synchronized void init() {
		mHandler = new MHandler();
		userid = Installation.id(mContext.getApplicationContext());
		SharedPrefsUtil.putValue(mContext, "myConfig", "userid", userid);// 设置文件
		if (TEST) {
			test();
		} else {
			initLocationOption();
			initLocationClient(mContext);
			// 启动定位
			startLocation();
			initSharePre();
		}
	}

	private void startLocation() {
		// 开始定位
		locationClient.startLocation();
		// mHandler.sendEmptyMessage(LOCATION_START);

	}

	private void initLocationClient(Context context) {
		locationClient = new AMapLocationClient(context);
		// 设置定位监听
		locationClient.setLocationListener(this);
		// 设置定位参数
		locationClient.setLocationOption(locationOption);
	}

	// 根据控件的选择，重新设置定位参数
	private void initLocationOption() {
		if (locationOption == null) {
			locationOption = new AMapLocationClientOption();
		}
		// 设置定位模式为高精度模式
		locationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
		// 设置一次定位还是多次定位
		locationOption.setOnceLocation(false);
		// 设置是否需要显示逆地理编码
		locationOption.setNeedAddress(true);
		/**
		 * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位 注意：只有在高精度模式下的单次定位有效，其他方式无效
		 */
		locationOption.setGpsFirst(false);
		// 设置是否开启缓存
		locationOption.setLocationCacheEnable(false);
		// 设置是否等待设备wifi刷新，如果设置为true,会自动变为单次定位，持续定位时不要使用
		locationOption.setOnceLocationLatest(false);
		// 设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
		locationOption.setInterval(30000);
	}

	// 定位监听
	@Override
	public synchronized void onLocationChanged(AMapLocation loc) {
		Log.i(TAG, "onLocationChanged start");
		if (loc.getErrorCode() == 0) {
			mLong = loc.getLongitude() + "";
			mLat = loc.getLatitude() + "";
			if (iskong(preLat) || iskong(preLang)) {
				// 缓存经纬为空
				Log.i(TAG, "app first");
				// 第一次启动
				queryLocationinfo(1);
				mHandler.sendEmptyMessage(SAVE_DATA);
			} else {
				// 是否有修正?
				if (!iskong(x_keywords)) {
					//3分钟不进行地图连接
					//queryLocationinfo(2);
					Log.i(TAG, "stopLocation");
					locationClient.stopLocation();// 停止定位
					mHandler.postDelayed(runnable, fixtime);
					mHandler.sendEmptyMessage(SAVE_DATA);
				} else {
					// check有效定位
					if (isValuable(mLong, mLat)) {
						mHandler.sendEmptyMessage(SAVE_DATA);
						// 判断是否离开30米
						if (isFatherThan30(preLang, preLat, mLong, mLat)) {
							queryLocationinfo(1);
						} else {
							Log.i(TAG, "use cache");
							// 使用最后缓存
							lastaddr = SharedPrefsUtil.getValue(mContext, "lastVisit", "cache_jingdian_1", "");
							xingzheng = SharedPrefsUtil.getValue(mContext, "lastVisit", "cache_xingzheng_1", "");
							if (!iskong(lastaddr) && !iskong(xingzheng)) {
								for (UpdateLocationText updateLocationText : callbacks) {
									updateLocationText.updateLocationViews(lastaddr, xingzheng);
								}
							}
						}
					}
				}
			}
			//实时缓存
			queryLocationinfo(3);
		} else {
			// 定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
			Log.e(TAG, "location Error, ErrCode:" + loc.getErrorCode() + ", errInfo:" + loc.getErrorInfo());
		}
		Log.i(TAG, "onLocationChanged end");
	}
	
	Runnable runnable = new Runnable() {  
	    @Override  
	    public void run() {  
	        fixtime-=1000;
	        mHandler.postDelayed(this, 1000);
	        if(fixtime<0){
	        	Log.i(TAG, "3分到重新定位");
	        	startLocation();
	        	mHandler.removeCallbacks(runnable);
	        }
	    }  
	};  

	// QQ地图请求
	private synchronized void txRequest() {
		Log.i(TAG, "腾讯开始查询");
		poiList2.clear();
		StringRequest txRequest = new StringRequest(
				"http://apis.map.qq.com/ws/geocoder/v1/?location=" + mLat + "," + mLong + "&key=" + txKey
						+ "&get_poi=1&output=json&poi_options=radius=1000;page_size=20;page_index=1",
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						// Log.e(TAG, response);
						try {
							// 使用JSONObject给response转换编码
							JSONObject jsonObject = new JSONObject(response);
							int resultCode = jsonObject.getInt("status");
							if (resultCode == 0) {
								JSONObject rss = new JSONObject(jsonObject.getString("result"));
								JSONObject rs = rss.getJSONObject("address_component");
								a0_t = rs.getString("nation");
								a1_t = rs.getString("province");
								a2_t = rs.getString("city");
								a3_t = rs.getString("district");
								a5_1_t = rs.getString("street");
								a5_2_t = rs.getString("street_number");

								JSONArray jsonArray = rss.getJSONArray("pois");
								JSONObject jsonObjectSon = (JSONObject) jsonArray.opt(0);
								keywords_t = jsonObjectSon.getString("title");
								String category = jsonObjectSon.getString("category");
								if (category_t.contains(";")) {
									category_t = category.substring(0, category.indexOf(";"));
								} else {
									category_t = category;
								}
								// 腾讯定位点
								if (!iskong(x_keywords)) {
									// 修正地标处理
									x_keywords = x_keywords.replaceAll("\\[([^\\[\\]]+)\\]", "");
									keywords_t = x_keywords;
									// if (x_map=="2"){
									// set.add(new
									// ResultGt("2",keywords_t,keywords_t,6,"地名地址",x_distance));
									// }else{
									// set.add(new
									// ResultGt("1",keywords_t,keywords_t,6,"地名地址信息",x_distance));
									// }
								} else {
									poiList2.add(new ResultGt("2", keywords_t, keywords_t, 6, "地名地址", 0.2));
								}

								for (int i = 1; i < jsonArray.length(); i++) {
									JSONObject jsonObjectSons = (JSONObject) jsonArray.opt(i);
									String cata = jsonObjectSon.getString("category");
									String category_tx = cata;
									if (category_tx.contains(";")) {
										category_tx = cata.substring(0, cata.indexOf(";"));
									} else {
										category_tx = cata;
									}
									poiList2.add(new ResultGt("2", jsonObjectSons.getString("title"),
											jsonObjectSons.getString("address"), 6, category_tx,
											jsonObjectSons.getInt("_distance")));
									// near_marks+="[腾]"+jsonObjectSons.getString("title")+"("+jsonObjectSons.getString("_distance")+"米)"+"\n";
									// Log.d("QQ",
									// jsonObjectSons.getString("title")+""+jsonObjectSons.getInt("_distance"));
								}
								Log.i(TAG, "腾讯結束查询");
								// 腾讯查询完
								gdRequest();
							}
						} catch (JSONException e) {
							Log.i(TAG, "腾讯获取PIO失败");
							e.printStackTrace();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "腾讯地图错误");
						Log.e("TAG", error.getMessage(), error);
					}

				});
		mQueue.add(txRequest);
	}

	private synchronized void gdRequest() {
		Log.i(TAG, "高德开始查询");
		poiList1.clear();
		StringRequest gdRequest = new StringRequest(
				"http://restapi.amap.com/v3/geocode/regeo?output=json&location=" + mLong + "," + mLat
						+ "&key=202c02e4bc3a2d71fc421600e2a3464d&radius=1000&extensions=all",
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						try {
							// 使用JSONObject给response转换编码
							JSONObject jsonObject = new JSONObject(response);
							int resultCode = jsonObject.getInt("status");
							if (resultCode == 1) {
								JSONObject rss = new JSONObject(jsonObject.getString("regeocode"));
								JSONObject rs = rss.getJSONObject("addressComponent");
								a0_g = rs.getString("country");
								a1_g = rs.getString("province");
								a2_g = rs.getString("city");
								a3_g = rs.getString("district");
								a4_g = rs.getString("township");
								// 高德定位点
								poiList1.add(new ResultGt("1", a2_g, a2_g, 2, "地名地址信息", 0.3));// 洛阳市
								poiList1.add(new ResultGt("1", a3_g, a3_g, 3, "地名地址信息", 0.4));// 涧西区
								poiList1.add(new ResultGt("1", a4_g, a4_g, 4, "地名地址信息", 0.6));// 街道

								JSONObject a5 = new JSONObject(rs.getString("streetNumber"));
								a5_1_g = a5.getString("street");
								a5_2_g = a5.getString("number");

								poiList1.add(new ResultGt("1", a5_1_g, a5_1_g, 5, "地名地址信息", 0.7));// 路
								// 区，街道用两者
								if (!a3_g.equals(a3_t)) {
									poiList2.add(new ResultGt("2", a3_t, a3_t, 3, "地名地址", 0.5));
								}
								if (!a5_1_g.equals(a5_1_t)) {
									poiList2.add(new ResultGt("2", a5_1_t, a5_1_t, 4, "地名地址", 0.8));
								}

								JSONArray jsonArray = rss.getJSONArray("pois");
								JSONObject jsonObjectSon = (JSONObject) jsonArray.opt(0);

								keywords_g = jsonObjectSon.getString("name");
								String type = jsonObjectSon.getString("type");
								if (type.contains(";")) {
									category_g = type.substring(0, type.indexOf(";"));
								} else {
									category_g = type;
								}
								for (int j = 0; j < jsonArray.length(); j++) {
									JSONObject jsonObjectSons = (JSONObject) jsonArray.opt(j);
									String cata = jsonObjectSon.getString("type");
									String category_gx = "";
									if (cata.contains(";")) {
										category_gx = cata.substring(0, cata.indexOf(";"));
									} else {
										category_gx = cata;
									}
									poiList1.add(new ResultGt("1", jsonObjectSons.getString("name"),
											jsonObjectSons.getString("address"), 6, category_gx,
											jsonObjectSons.getInt("distance")));
									// near_marks+="[高]"+jsonObjectSons.getString("name")+"("+jsonObjectSons.getString("distance")+"米)"+"\n";
									// Log.d("GD",
									// jsonObjectSons.getString("name")+""+jsonObjectSons.getInt("distance"));
								}
								// 高德定位完后排序
								Log.i(TAG, "高德结束查询");
								if (callViewListbacks.size() > 0) {
									poisList();
								}
								// 天气请求开始
								String lastCity = SharedPrefsUtil.getValue(mContext, "lastVisit", "lastTianqiCity", "");
								String curCity = "";
								if (!iskong(a2_t)) {
									curCity = a2_t;
								} else {
									curCity = a2_g;
								}
								// curCity = curCity.replace("市", "");
								if (lastCity.compareTo(curCity) != 0) {
									// Log.d("CITY_DIFF","不同城市");
									tianqiRequest(curCity);
								} else {
									// Log.d("CITY_DIFF","同一城市");
									// 是否同一小时
									long lastCityDate = SharedPrefsUtil.getValue(mContext, "lastVisit", "lastCityDate",
											System.currentTimeMillis());
									if (System.currentTimeMillis() - lastCityDate > 3600000) {
										// Log.d("CITY_DIFF_HOUR","大于1小时");
										tianqiRequest(curCity);
									} else {
										// 获取本地天气字符串
										// Log.d("CITY_DIFF_HOUR","小于1小时");
										tianqi = SharedPrefsUtil.getValue(mContext, "lastVisit", "lastTianqi", "");
										recamRequest();
									}
								}
							}
						} catch (JSONException e) {
							Log.i(TAG, "高德获取PIO失败");
							e.printStackTrace();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "腾讯地图错误");
						Log.e(TAG, "error=" + error.getMessage());
					}
				});
		mQueue.add(gdRequest);
	}

	// 天气请求
	private synchronized void tianqiRequest(String city) {
		// "东北风，微风级，高温 11℃，小雨，低温 8℃";
		String tqURL = "";
		try {
			tqURL = "http://wthrcdn.etouch.cn/weather_mini?city=" + URLEncoder.encode(city, "utf-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO 自动生成的 catch 块
			e1.printStackTrace();
		}
		// Log.d("tianqiRequest",tqURL+"");
		StringRequest stringRequest = new StringRequest(tqURL, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				// Log.d("TAG", response);
				try {
					JSONObject jsonObject = new JSONObject(response);
					int status = jsonObject.getInt("status");
					if (status == 1000) {
						JSONObject rss = new JSONObject(jsonObject.getString("data"));
						JSONArray jsonArray = rss.getJSONArray("forecast");
						JSONObject jsonObjectSon = (JSONObject) jsonArray.opt(0);
						// 当天天气
						String fengxiang = jsonObjectSon.getString("fengxiang");
						String fengli = jsonObjectSon.getString("fengli");
						String gaowen = jsonObjectSon.getString("high");
						String tianqis = jsonObjectSon.getString("type");
						String diwen = jsonObjectSon.getString("low");
						tianqi = fengxiang + "，" + fengli + "，" + gaowen + "，" + tianqis + "，" + diwen;
						tianqi = tianqi.replaceAll(" ", "");
						SharedPrefsUtil.putValue(mContext, "lastVisit", "lastTianqi", tianqi);// 最后的天气
						if (!iskong(a2_t)) {
							SharedPrefsUtil.putValue(mContext, "lastVisit", "lastTianqiCity", a2_t);// 最后的城市
						} else {
							SharedPrefsUtil.putValue(mContext, "lastVisit", "lastTianqiCity", a2_g);// 最后的天气
						}
						SharedPrefsUtil.putValue(mContext, "lastVisit", "lastTianqiDate", System.currentTimeMillis());// 最后的日期
						// if (recamContentCallbacks.size() > 0) {
						recamRequest();
						// }
					}
				} catch (JSONException e) {
					Log.i(TAG, "天气查询");
					// e.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				// Log.e("TAG", error.getMessage(), error);
			}
		}) {
			// volley解决中文乱码问题
			@Override
			protected Response<String> parseNetworkResponse(NetworkResponse response) {
				try {
					String jsonString = new String(response.data, "UTF-8");
					return Response.success(jsonString, HttpHeaderParser.parseCacheHeaders(response));
				} catch (UnsupportedEncodingException e) {
					return Response.error(new ParseError(e));
				} catch (Exception je) {
					return Response.error(new ParseError(je));
				}
			}
		};
		mQueue.add(stringRequest);
	}

	// 服务器请求
	public synchronized void recamRequest() {
		Log.i(TAG, "睿拍开始查询" + gd_tx_Url);
		prePareRecamParameters();
		FakeX509TrustManger.allowAllSSL(); // it is dangerous!但是有的时候我们需要这样做！！
		StringRequest stringRequest = new StringRequest(gd_tx_Url, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d(TAG, "ruipai response=" + response);
				try {
					// 使用JSONObject给response转换编码
					JSONObject jsonObject = new JSONObject(response);
					HashMap<String, String> map = new HashMap<String, String>();
					if (!iskong(x_keywords)) {
						lastaddr = x_keywords;
					} else {
						SharedPrefsUtil.putValue(mContext, SPF, "addItemPos", 0);
						if (!iskong(keywords_t)) {
							lastaddr = keywords_t;
						} else {
							lastaddr = jsonObject.getString("name_fu");
						}
					}
					xingzheng = jsonObject.getString("name_xingzheng");
					addressDetail = jsonObject.getString("content");
					place_unionid = jsonObject.getString("place_unionid");
					list_unionid = jsonObject.getString("list_unionid");
					contentid = jsonObject.getString("con_unionid");
					String con_decorate = jsonObject.getString("con_decorate");
					map.put("title", jsonObject.getString("name_jingdian"));
					map.put("xingzheng", xingzheng);
					map.put("content", addressDetail);
					map.put("fu_title", jsonObject.getString("name_fu"));
					map.put("con_decorate", jsonObject.getString("con_decorate"));
					// 保存缓存信息到sharepreference
					SharedPrefsUtil.putValue(mContext, "lastVisit", "cache_contentid_" + cacheN, contentid);
					SharedPrefsUtil.putValue(mContext, "lastVisit", "cache_jingdian_" + cacheN, lastaddr);
					SharedPrefsUtil.putValue(mContext, "lastVisit", "cache_xingzheng_" + cacheN, xingzheng);
					SharedPrefsUtil.putValue(mContext, "lastVisit", "cache_content_" + cacheN, addressDetail);
					SharedPrefsUtil.putValue(mContext, "lastVisit", "cache_con_decorate_" + cacheN, con_decorate);

					mHandler.sendEmptyMessage(UPDATE_ADDR);
					if (recamContentCallbacks.size() > 0) {
						for (UpdateRecamContent updateRecamContent : recamContentCallbacks) {
							updateRecamContent.updateModuleContent(map);
						}
					}

				} catch (JSONException e) {
					Log.e(TAG, "recam 服务器数据失败" + gd_tx_Url);
					e.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, error.getMessage(), error);
			}
		});
		mQueue.add(stringRequest);
	}

	/*
	 * Handler mHandler = new Handler() { public void
	 * dispatchMessage(android.os.Message msg) { switch (msg.what) { // 开始定位
	 * case LOCATION_START: // cur_jing.setText("正在定位..."); //
	 * loading1.setVisibility(View.VISIBLE); Log.i(TAG, "正在定位"); break; // 定位完成
	 * case LOCATION_SUCCESS:
	 * 
	 * break; case GD_LOCATION_START:
	 * 
	 * break; case 3: // 停止定位 break; default: break; } }; };
	 */
	// 键排序
	private void prePareRecamParameters() {
		// 初始化
		gd_tx_Url = gdtxUrl;
		key_All = keyAll;
		Map<String, String> map = new TreeMap<String, String>();
		map.put("a0_g", a0_g);
		map.put("a1_g", a1_g);
		map.put("a2_g", a2_g);
		map.put("a3_g", a3_g);
		map.put("a4_g", a4_g);
		map.put("a5_1_g", a5_1_g);
		map.put("a5_2_g", a5_2_g);
		map.put("keywords_g", keywords_g);
		map.put("category_g", category_g);
		map.put("a0_t", a0_t);
		map.put("a1_t", a1_t);
		map.put("a2_t", a2_t);
		map.put("a3_t", a3_t);
		map.put("a5_1_t", a5_1_t);
		map.put("a5_2_t", a5_2_t);
		map.put("keywords_t", keywords_t);
		map.put("category_t", category_t);
		map.put("x_xz", x_xz);
		map.put("act_from", act_from);
		map.put("jing", mLong);
		map.put("wei", mLat);
		map.put("userid", userid);
		map.put("weixin", weixin);
		map.put("weibo", weibo);
		map.put("app", app);
		map.put("x_keywords", x_keywords);
		map.put("x_category", x_category);
		map.put("x_map", x_map);
		if (SharedPrefsUtil.getValue(mContext, SPF, DS_PA_LO, true)) {
			setup_upshow = "1";
		} else {
			setup_upshow = "0";
		}
		map.put("setup_upshow", setup_upshow);
		map.put("fangxiang", fangxiang);
		map.put("haiba", haiba);
		map.put("fenbei", fenbei);
		map.put("tianqi", tianqi);
		map.put("shebei", shebei);
		map.put("old_photoid", old_photoid);
		map.put("templet_id", templet_id);
		map.put("pre_con_unionid", pre_con_unionid);
		map.put("pic_date", pic_date);
		Map<String, String> resultMap = sortMapByKey(map); // 按Key进行排序
		for (Map.Entry<String, String> entry : resultMap.entrySet()) {
			gd_tx_Url += entry.getKey() + "=" + URLEncoder.encode(entry.getValue()) + "&";
			key_All += entry.getKey() + entry.getValue();
		}
		keyMd5 = Md5Util.getMd5(URLEncoder.encode(key_All)).toUpperCase();
		Log.d(TAG, "MD5URL：" + URLEncoder.encode(key_All));
		// 请求的URL
		gd_tx_Url += "key=" + keyMd5 + "&timestamp=" + timestamp;
	}

	private void prePareRecamParametersP(String act) {
		// 初始化
		recam_Url = recamUrl;
		key_All = keyAll;
		photoid = SharedPrefsUtil.getValue(mContext, "lastVisit", "photoid", "");
		Map<String, String> map = new TreeMap<String, String>();
		map.put("act_from", act);
		map.put("app", app);
		map.put("contentid", contentid);
		map.put("photoid", photoid);
		map.put("old_photoid", old_photoid);
		map.put("userid", userid);
		map.put("weixin", weixin);
		map.put("weibo", weibo);
		map.put("templet_id", templet_id);
		map.put("place_unionid", place_unionid);
		map.put("list_unionid", list_unionid);
		Map<String, String> resultMap = sortMapByKey(map); // 按Key进行排序
		for (Map.Entry<String, String> entry : resultMap.entrySet()) {
			recam_Url += entry.getKey() + "=" + URLEncoder.encode(entry.getValue()) + "&";
			key_All += entry.getKey() + entry.getValue();
		}
		keyMd5 = Md5Util.getMd5(URLEncoder.encode(key_All)).toUpperCase();
		Log.d(TAG, "MD5URL：" + URLEncoder.encode(key_All));
		// 请求的URL
		recam_Url += "key=" + keyMd5 + "&timestamp=" + timestamp;
	}

	public void updateRecamPostPra(String actfrom, String conunionid, String placeunionid, String listunionid) {
		act_from = actfrom;
		contentid = conunionid;
		place_unionid = placeunionid;
		list_unionid = listunionid;
		Log.d(TAG, "修正地标" + recam_Url);
		recamPost(actfrom);
	}

	// 拍照p接口
	public synchronized void recamPost(String act) {
		prePareRecamParametersP(act);
		FakeX509TrustManger.allowAllSSL(); // it is dangerous!但是有的时候我们需要这样做！！
		Log.d(TAG, "P接口 recam_Url=" + recam_Url);
		StringRequest stringRequest = new StringRequest(recam_Url, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d(TAG, "P接口 response=" + response);
				try {
					// 使用JSONObject给response转换编码
					JSONObject jsonObject = new JSONObject(response);
					int status = jsonObject.getInt("status");
					if (status == 1) {
						Log.i(TAG, "P接口正常");
					} else {
						Log.i(TAG, "P接口返回-1");
					}
				} catch (JSONException e) {
					Log.e(TAG, "recam 服务器数据失败" + recam_Url);
					e.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, error.getMessage(), error);
			}
		});
		mQueue.add(stringRequest);
	}

	/**
	 * 使用 Map按key进行排序
	 * 
	 * @param map
	 * @return
	 */
	public Map<String, String> sortMapByKey(Map<String, String> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		Map<String, String> sortMap = new TreeMap<String, String>(new MapKeyComparator());
		sortMap.putAll(map);
		return sortMap;
	}

	// 比较器类
	static class MapKeyComparator implements Comparator<String> {
		@Override
		public int compare(String str1, String str2) {
			return str1.compareTo(str2);
		}
	}

	// 地标点列表
	public void poisList() {
		// 重新去重排序
		for (int j = 0; j < poiList2.size(); j++) {
			ResultGt Obj = poiList2.get(j);
			for (int k = 0; k < poiList1.size(); k++) {
				int res = (poiList1.get(k).getName()).compareTo(Obj.getName());
				if (res == 0) {
					poiList1.remove(k);
				}
			}
		}
		poiList1.addAll(poiList2);
		Set<ResultGt> set = new HashSet<ResultGt>(poiList1);
		poiList1.clear();
		poiList1.addAll(set);
		Collections.sort(poiList1, new ResultGtCom1());
		ArrayList<HashMap<String, String>> myArrayList = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < poiList1.size(); i++) {
			ResultGt rs = poiList1.get(i);
			HashMap<String, String> map1 = new HashMap<String, String>();
			// near_marks += sNext1;
			map1.put("itemGt", rs.getGt());
			// map1.put("itemName", (sNext1.getLevel() < 6 ? ""
			// : sNext1.getGt() == "1" ? "[高]" : "[腾]") + sNext1.getName());
			map1.put("itemName", rs.getName());
			// Log.e(TAG, "itemName=" + sNext1.getName());
			map1.put("itemAddress", rs.getAddress());
			map1.put("itemLevel", rs.getLevel() + "");
			map1.put("itemType", rs.getType());
			map1.put("itemDistance", rs.getDistance() > 1 ? "(" + rs.getDistance() + "米)" : "");
			myArrayList.add(map1);
		}
		if (myArrayList.size() > 0) {
			for (UpdateNearList updateList : callViewListbacks) {
				updateList.updateViews(myArrayList);
			}
		}
		// 设置SharedPreferences对象
		mHandler.sendEmptyMessage(SAVE_DATA);
	}

	private void test() {
		// 调试用经纬度
		mLong = "112.415668";
		mLat = "34.65726";
		txRequest();
	}

	public void onDestroy() {
		if (null != locationClient) {
			locationClient.stopLocation();
			locationClient.onDestroy();
			locationClient = null;
			locationOption = null;
			preLat = "";
			preLang = "";
		}
	}

	public boolean iskong(String str) {
		if (str == null || str.equals("")) {
			return true;
		}
		return false;
	}

	private boolean isValuable(String lang, String lat) {
		return !iskong(lang) && !iskong(lat);
	}

	public String getRealTime() {
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = format.format(date);
		return time;
	}

	private void initSharePre() {
		// 使用SharedPreferences读取数据
		preLang = SharedPrefsUtil.getValue(mContext, "lastVisit", "Longitude", "");
		preLat = SharedPrefsUtil.getValue(mContext, "lastVisit", "Latitude", "");
		String lasttime = SharedPrefsUtil.getValue(mContext, "lastVisit", "lasttime", "");
		lastaddr = SharedPrefsUtil.getValue(mContext, "lastVisit", "lastaddr", "");
		xingzheng = SharedPrefsUtil.getValue(mContext, "lastVisit", "xingzheng", "");
		String pois = SharedPrefsUtil.getValue(mContext, "lastVisit", "pois", "");
	}

	private boolean isEqualPre(String longti, String latti) {
		return preLang.equals(longti) && preLat.equals(latti);
	}

	private void updateAddr() {
		if (!iskong(xingzheng) && !iskong(keywords_t)) {
			Log.i(TAG, "updateAddr() begain");
			SharedPrefsUtil.putValue(mContext, "lastVisit", "xingzheng", xingzheng);
			SharedPrefsUtil.putValue(mContext, "lastVisit", "lastaddr", keywords_t);
			for (UpdateLocationText updateLocationText : callbacks) {
				updateLocationText.updateLocationViews(keywords_t, xingzheng);
			}
		} else if (!iskong(x_keywords)) {
			// 有修正的情况
			SharedPrefsUtil.putValue(mContext, "lastVisit", "xingzheng", xingzheng);
			SharedPrefsUtil.putValue(mContext, "lastVisit", "lastaddr", x_keywords);
			for (UpdateLocationText updateLocationText : callbacks) {
				updateLocationText.updateLocationViews(x_keywords, xingzheng);
			}
		}

	}

	private void getData() {
		preLang = SharedPrefsUtil.getValue(mContext, "lastVisit", "Longitude", "");
		preLat = SharedPrefsUtil.getValue(mContext, "lastVisit", "Latitude", "");
	}

	private void saveData() {
		SharedPrefsUtil.putValue(mContext, "lastVisit", "Longitude", mLong);
		SharedPrefsUtil.putValue(mContext, "lastVisit", "Latitude", mLat);
		SharedPrefsUtil.putValue(mContext, "lastVisit", "lasttime", getRealTime());
	}

	public void testReuestNearList() {
		mLong = "112.415668";
		mLat = "34.65726";
		txRequest();
	}

	public interface UpdateLocationText {
		public void updateLocationViews(String loName, String xingZheng);
	}

	public interface UpdateNearList {
		public void updateViews(ArrayList<HashMap<String, String>> arraylist);
	}

	public interface UpdateRecamContent {
		public void updateModuleContent(HashMap<String, String> map);
	}

	public void addCallback(UpdateLocationText updateLocationText) {
		callbacks.add(updateLocationText);
	}

	public void removeCallback(UpdateLocationText updateLocationText) {
		callbacks.remove(updateLocationText);
	}

	public void addViewListCallback(UpdateNearList updateNearList) {
		callViewListbacks.add(updateNearList);
	}

	public void removeListCallback(UpdateNearList updateNearList) {
		callViewListbacks.remove(updateNearList);
	}

	public void addRecamContentList(UpdateRecamContent updateRecamContent) {
		recamContentCallbacks.add(updateRecamContent);
	}

	public void removeRecamContent(UpdateRecamContent updateRecamContent) {
		recamContentCallbacks.remove(updateRecamContent);
	}

	public void updateRecamPra(String xkeywords, String xmap, String xxz, String category) {
		x_keywords = xkeywords.replaceAll("\\[([^\\[\\]]+)\\]", "");
		x_map = xmap;
		x_xz = xxz;
		x_category = category;
		Log.d(TAG, "修正地标" + gd_tx_Url);
		recamRequest();
	}

	class MHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case SAVE_DATA:
				saveData();
				break;
			case UPDATE_ADDR:
				updateAddr();
				break;
			default:
				;
			}
		}
	};

	public static String convertToSexagesimal(String numStr) {
		double num = Double.parseDouble(numStr);
		int du = (int) Math.floor(Math.abs(num)); // 获取整数部分
		double temp = getdPoint(Math.abs(num)) * 60;
		int fen = (int) Math.floor(temp); // 获取整数部分
		double miao = getdPoint(temp) * 60;
		miao *= 10000;
		int miaoInt = (int) (miao + 0.5);
		if (num < 0)
			return "-" + du + "/1," + fen + "/1," + miaoInt + "/10000";

		return du + "/1," + fen + "/1," + miaoInt + "/10000";

	}

	private static double getdPoint(double num) {
		double d = num;
		int fInt = (int) d;
		BigDecimal b1 = new BigDecimal(Double.toString(d));
		BigDecimal b2 = new BigDecimal(Integer.toString(fInt));
		double dPoint = b1.subtract(b2).floatValue();
		return dPoint;
	}

	public void setLangLat(String lang, String lat) {
		mLong = lang;
		mLat = lat;
	}

	public void queryLocationinfo(int c) {
		Log.i(TAG, "C接口缓存" + c);
		cacheN = c;
		txRequest();
	}

	private boolean isFatherThan30(String prelang, String prelat, String curlong, String curLat) {
		String juli = MapDistance.getDistance(preLang, preLat, mLong, mLat);
		int distance = Integer.valueOf(juli).intValue();
		Log.i(TAG, "juli=" + Integer.valueOf(juli));
		return Integer.valueOf(juli) > 30;
	}
}
