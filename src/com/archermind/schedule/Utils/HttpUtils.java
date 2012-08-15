package com.archermind.schedule.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class HttpUtils implements Runnable{
    public static Map<String,String> mparmas=null;
    public static String httphead = "";
    public static String murl ="";
	public static String doPost(Map<String,String> parmas, String url) {
		DefaultHttpClient client = new DefaultHttpClient();
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000); 
		client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);  
		HttpPost httpPost = new HttpPost(url);

		ArrayList<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
		if (parmas != null) {
			Set<String> keys = parmas.keySet();
			for (Iterator<String> i = keys.iterator(); i.hasNext();) {
				String key = (String) i.next();
				pairs.add(new BasicNameValuePair(key,  parmas.get(key)));
			}
		}

		try {
			UrlEncodedFormEntity p_entity = new UrlEncodedFormEntity(pairs,
					HTTP.UTF_8);

			httpPost.setEntity(p_entity);
			if(httphead!=null && !httphead.equals("")){
				httphead= httphead.replace("\r\n", "");
			}
			httpPost.setHeader("Cookie", "sid=" + httphead);
			HttpResponse response = client.execute(httpPost);
			
			if(response.getStatusLine().getStatusCode() == 200){
				if(url.indexOf("login") >0 ||url.indexOf("BinInfo") >0){
					Header[] head = null;
					head = response.getHeaders("Set-Cookie");
					for (int i=0; i<head.length; i++) {						
//						if (head == null) {
//							break;
//						}
						httphead = httphead + "header:" + head[i] + "\r\n";
					}
					//System.out.println("xiaopashu-------:"+httphead);
					httphead =java.net.URLDecoder.decode(httphead ,"utf8");
//					Pattern p = Pattern.compile("s:32:\"([^\"]+)\"");
//					Matcher m = p.matcher(httphead);
//					if(m.find()){
//						httphead =m.group();
//						httphead =httphead.replace("s:32:", "");
//						httphead =httphead.replace("\"", "");
//					}
					
					if(httphead.indexOf("sid=")>0)
						httphead =httphead.substring(httphead.indexOf("sid=")+4);
					else
						httphead ="";
					System.out.println("xiaopashu:"+httphead);
				}
				String strResult = EntityUtils.toString(response.getEntity(),HTTP.UTF_8);
				//strResult = strResult.replace("\"", "");
				Log.e("HttpUtils", "strResult:"+strResult);
				return strResult;
			}else{
				for(int i=0;i<2;i++){
					response = client.execute(httpPost);
					if(response.getStatusLine().getStatusCode() == 200){
						String strResult = EntityUtils.toString(response.getEntity(),HTTP.UTF_8);
						return strResult;
					}
				}
			}
			return Integer.toString(response.getStatusLine().getStatusCode());
			
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e){
			e.printStackTrace();
		}
		return Integer.toString(ServerInterface.ERROR_SERVER_INTERNAL);
	}
	
	
	
	public void SetMap(Map<String,String> parmas){
		mparmas = parmas;
	}
	public void Seturl(String url){
		murl =url;
	}
	public void SetCookie(String cookie){
		httphead =cookie;
	}
	public String GetCookie(){
		return httphead;
	}
	public void run(){
		doPost(mparmas,murl);
	}
}