package com.renren.yourrenren;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;

public class MyFriendListActivity extends ListActivity {
	private String [] Names;
	private String [] Uids;
	private String [][] Birthdays;
	private String[] Headurls;
	int [] flag;
	String [] BNames;
	SimpleAdapter adapter;
	Bitmap [] bm;
	
	ArrayList<HashMap<String, Object>> users;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friendlist);
		UIActivity.dismissProgress();
		Intent intent = getIntent();
		Bundle data = intent.getExtras();
		
		String access_token = (String)data.getSerializable("access_token");
		String api_key = "14b42a4c65de42778677f50c6f9facc7";
		String secret_key = "133d903cd49d4245bd377e421e088030";
		String version = "1.0";
		String method = "friends.getFriends";
		//String method = "users.getLoggedInUser";
		String format = "JSON";
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("v", version);
		parameters.put("access_token", access_token);
		parameters.put("method", method);
		parameters.put("format", format);
		String sig = Signature.getSignature(parameters, secret_key);

		
		String url = "http://api.renren.com/restserver.do";
		HttpPost postmethod = new HttpPost(url);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  

		nameValuePairs.add(new BasicNameValuePair("v", version));  
		nameValuePairs.add(new BasicNameValuePair("access_token", access_token));  
		nameValuePairs.add(new BasicNameValuePair("method", method));  
		nameValuePairs.add(new BasicNameValuePair("format", format));  
		nameValuePairs.add(new BasicNameValuePair("sig", sig));
		
		try{
		postmethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
//		postmethod.addParameter("v", version);
//		postmethod.addParameter("access_token", access_token);
//		postmethod.addParameter("method", method);
//		postmethod.addParameter("format", format);
//		postmethod.addParameter("sig", sig);
		
		HttpClient client = new DefaultHttpClient();
		String result ="";
		try
		{
			HttpResponse response = client.execute(postmethod);
			result = EntityUtils.toString(response.getEntity());
			//JSONObject obj = new JSONObject(result);
			JSONArray friends = new JSONArray(result);
			Names = new String[friends.length()];
			Uids = new String[friends.length()];
			String uidparam = new String();
			int i;
			for (i = 0;i < friends.length();i++)
			{
				Names[i] = friends.getJSONObject(i).getString("name");
				Uids[i] = friends.getJSONObject(i).getString("id");
				uidparam = uidparam + Uids[i] + ",";
			}
			
			uidparam = uidparam.substring(0, uidparam.length()-1);
			
			HashMap<String, String> parameters2 = new HashMap<String, String>();
			method = "users.getInfo";
			parameters2.put("method", method);
			parameters2.put("fields", "name,birthday,tinyurl");
			parameters2.put("v", version);
			parameters2.put("access_token", access_token);
			parameters2.put("format", format);
			parameters2.put("uids", uidparam);
			sig = Signature.getSignature(parameters2, secret_key);
			
			
			List<NameValuePair> nameValuePairs2 = new ArrayList<NameValuePair>(2);  

			nameValuePairs2.add(new BasicNameValuePair("v", version));  
			nameValuePairs2.add(new BasicNameValuePair("access_token", access_token));  
			nameValuePairs2.add(new BasicNameValuePair("method", method));  
			nameValuePairs2.add(new BasicNameValuePair("format", format));  
			nameValuePairs2.add(new BasicNameValuePair("sig", sig));
			nameValuePairs2.add(new BasicNameValuePair("fields", "name,birthday,tinyurl"));
			nameValuePairs2.add(new BasicNameValuePair("uids", uidparam));
			
			HttpPost postmethod2 = new HttpPost(url);
			postmethod2.setEntity(new UrlEncodedFormEntity(nameValuePairs2));
			
			response = client.execute(postmethod2);
			result = EntityUtils.toString(response.getEntity());
			//JSONObject obj = new JSONObject(result);
			JSONArray userinfo = new JSONArray(result);
			Birthdays = new String[userinfo.length()][3];
			Headurls = new String[userinfo.length()];
			Calendar c = Calendar.getInstance(); 
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			flag = new int[userinfo.length()];
			int cnt = 0;

			for (i = 0;i < userinfo.length();i++)
			{
				if (userinfo.getJSONObject(i).has("birthday"))
				{
					
					Birthdays[i] = userinfo.getJSONObject(i).getString("birthday").split("-");
					Names[i] = userinfo.getJSONObject(i).getString("name");
					Headurls[i] = userinfo.getJSONObject(i).getString("tinyurl");
					int tmp = Integer.valueOf(Birthdays[i][1]).intValue();
					if ((Integer.valueOf(Birthdays[i][1]).intValue() == month + 1) && (Integer.valueOf(Birthdays[i][2]).intValue() > day)) 
					{
						flag[cnt] = i;
						cnt++;
					}
				}
			}
			
			BNames = new String[cnt];
			for (i = 0;i < cnt; i++)
			{
				BNames[i] = Names[flag[i]];
			}
			
			
			bm = new Bitmap[cnt];
			users = new ArrayList<HashMap<String, Object>>(); 
			adapter = new SimpleAdapter(this, users, R.layout.useritem, new String[]{"img", "username"}, new int[]{R.id.list_image, R.id.list_text});
			
			
			adapter.setViewBinder(new ViewBinder(){

				  public boolean setViewValue(View view, Object data,  
		                    String textRepresentation) {  
		                //判断是否为我们要处理的对象  
		                if(view instanceof ImageView  && data instanceof Bitmap){  
		                    ImageView iv = (ImageView) view;  
		                  
		                    iv.setImageBitmap((Bitmap) data);  
		                    return true;  
		                }else  
		                return false;  
		            }  
				});
			
			
			for (i = 0;i < cnt;i++)
			{	
				HashMap<String, Object> user = new HashMap<String, Object>();
				URL logourl = new URL(Headurls[flag[i]]);
		        URLConnection conn = logourl.openConnection(); 
		        conn.connect(); 
		        InputStream is = conn.getInputStream(); 
	            bm[i] = BitmapFactory.decodeStream(is); 
				user.put("img", bm[i]); 
				user.put("username",Names[flag[i]]); 
				users.add(user); 
			}
			

			setListAdapter(adapter);
	
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		
	}
	
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		
		
		Bundle data = new Bundle();
		data.putSerializable("name", Names[flag[position]]);
		data.putSerializable("birthday", Birthdays[flag[position]]);
		data.putSerializable("logourl", Headurls[flag[position]]);
		
		Intent intent = new Intent(this, BirthdayActivity.class);
		intent.putExtras(data);
		startActivity(intent);
	}
	
//	public class GetLogo extends AsyncTask<Integer, Integer, Void> 
//	{
//
//		int i;
//		
//		@Override
//		protected Void doInBackground(Integer... arg0) {
//			// TODO Auto-generated method stub
//		
//
//			try {
//				i = arg0[0];
//				logourl = new URL(Headurls[flag[arg0[0]]]);
//		        URLConnection conn = logourl.openConnection(); 
//		        conn.connect(); 
//		        InputStream is = conn.getInputStream(); 
//	            bm[arg0[0]] = BitmapFactory.decodeStream(is); 	
//
//			} catch (MalformedURLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//			return null;
//		}
//		
//		protected void onPostExecute(String result) {
//
//		}
//	
//	}

}
