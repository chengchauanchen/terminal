package ptt.terminalsdk.manager.http;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.zectec.http.message.BasicNameValuePair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.vsx.hamster.terminalsdk.manager.http.HttpClientBaseImpl;

public class MyHttpClient extends HttpClientBaseImpl{

	private final Logger logger = Logger.getLogger(getClass());
	private final int timeOut = 30 * 1000;
	private static final String DEFAULT_ENCODING = "UTF-8";
	@Override
	public String postJson(String url, String jsonMessage) {
		try {
			HttpPost request = new HttpPost(url);
			request.setEntity(new ByteArrayEntity(jsonMessage.getBytes(DEFAULT_ENCODING)));
			request.setHeader("Accept", "application/json");
			request.setHeader("Content-type",
					"application/x-www-form-urlencoded");
			DefaultHttpClient client = getHttpClient(timeOut);
			client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
			logger.debug("发送的数据是：" + jsonMessage);
			HttpResponse response = client.execute(request);
			if (null != response) {
				String responseString = new String(EntityUtils.toByteArray(response.getEntity()), DEFAULT_ENCODING);
				logger.info("发送了一个get请求：url="+url+",收到的信息为："+responseString);
				return responseString;
			}
		} catch (Exception e) {
			logger.error("命令发送失败，url=" + url + ", jsonMessage=" + jsonMessage,
					e);
		}
		return null;
	}

	@Override
	public String postJson2(String url, String jsonMessage) {
		try {
			HttpPost request = new HttpPost(url);
			request.setEntity(new ByteArrayEntity(jsonMessage.getBytes(DEFAULT_ENCODING)));
			request.setHeader("Accept", "application/json");
			request.setHeader("Content-type","application/json;charset=UTF-8");
			DefaultHttpClient client = getHttpClient(timeOut);
			client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
			logger.debug("发送的数据是：" + jsonMessage);
			HttpResponse response = client.execute(request);
			if (null != response) {
				String responseString = new String(EntityUtils.toByteArray(response.getEntity()), DEFAULT_ENCODING);
				logger.info("发送了一个get请求：url="+url+",收到的信息为："+responseString);
				return responseString;
			}
		} catch (Exception e) {
			logger.error("命令发送失败，url=" + url + ", jsonMessage=" + jsonMessage,
					e);
		}
		return null;
	}


	@Override
	public String post2(String url, Map<String, String> paramsMap) {
		try {
			HttpPost request = new HttpPost(url);
			Set<String> keys = paramsMap.keySet();
			List<NameValuePair> formparams = new ArrayList<>();
			for (String key : keys) {
				formparams.add(new BasicNameValuePair(key, paramsMap.get(key)));
				logger.info("发送了一个post请求：key="+key+",vaule："+paramsMap.get(key));
			}
			request.setEntity(new UrlEncodedFormEntity( formparams , HTTP.UTF_8 ));
			request.setHeader("Accept", "application/json");
			request.setHeader("Content-type", "application/json;charset=UTF-8");
			DefaultHttpClient client = getHttpClient(timeOut);
			client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
			HttpResponse response = client.execute(request);
			if (null != response) {
				String responseString = new String(EntityUtils.toByteArray(response.getEntity()), DEFAULT_ENCODING);
				logger.info("发送了一个post请求：url="+url+",收到的信息为："+responseString);
				return responseString;
			}
		} catch (Exception e) {
			logger.error("命令发送失败，url=" + url  , e);
		}
		return null;
	}

	@Override
	public String post(String url, Map<String, String> paramsMap) {
		try {
			HttpPost request = new HttpPost(url);
			Set<String> keys = paramsMap.keySet();
			List<NameValuePair> formparams = new ArrayList<>();
			for (String key : keys) {
				formparams.add(new BasicNameValuePair(key, paramsMap.get(key)));
				logger.info("发送了一个post请求：key="+key+",vaule："+paramsMap.get(key));
			}
			request.setEntity(new UrlEncodedFormEntity( formparams , HTTP.UTF_8 ));
			request.setHeader("Accept", "application/json");
			request.setHeader("Content-type", "application/x-www-form-urlencoded");
			DefaultHttpClient client = getHttpClient(timeOut);
			client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
			HttpResponse response = client.execute(request);
			if (null != response) {
				String responseString = new String(EntityUtils.toByteArray(response.getEntity()), DEFAULT_ENCODING);
				logger.info("发送了一个post请求：url="+url+",收到的信息为："+responseString);
				return responseString;
			}
		} catch (Exception e) {
			logger.error("命令发送失败，url=" + url  , e);
		}
		return null;
	}
	@Override
	public String get(String url, Map<String,String> paramsMap) {
		try {
			url = getUrl(url, paramsMap);
			HttpGet request = new HttpGet(url);
			request.setHeader("Accept", "application/json");
			request.setHeader("Content-type", "application/x-www-form-urlencoded;charset=utf8");
			DefaultHttpClient client = getHttpClient(timeOut);
			client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
			HttpResponse response = client.execute(request);
			if (null != response) {
				String responseString = new String(EntityUtils.toByteArray(response.getEntity()), DEFAULT_ENCODING);
				logger.info("发送了一个get请求：url="+url+",收到的信息为："+responseString);
//				logger.info("responseString" + responseString);
				return responseString;
			}
		} catch (Exception e) {
			logger.error("命令发送失败，url=" + url, e);
		}
		return null;
	}

	@Override
	public String postFile(String url, File file, Map<String, String> paramsMap) {
		try{
			HttpPost httppost = new HttpPost(url);
			logger.info("文件服务器地址为" + url);
			FileBody bin = new FileBody(file);
			MultipartEntityBuilder builder = MultipartEntityBuilder.create().addPart("fileStream", bin);
			for(String str : paramsMap.keySet()){
				builder.addPart(str, new StringBody(paramsMap.get(str), ContentType.TEXT_PLAIN));
			}
			DefaultHttpClient client = getHttpClient(timeOut);
			httppost.setEntity(builder.build());
			HttpResponse response = client.execute(httppost);
			if (null != response) {
				byte[] responseArray = EntityUtils.toByteArray(response
						.getEntity());
				return new String(responseArray, DEFAULT_ENCODING);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	private DefaultHttpClient getHttpClient(int timeOut) {
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setSoTimeout(httpParams, timeOut);
		HttpConnectionParams.setConnectionTimeout(httpParams, timeOut);
		return new DefaultHttpClient(httpParams);
	}
}
