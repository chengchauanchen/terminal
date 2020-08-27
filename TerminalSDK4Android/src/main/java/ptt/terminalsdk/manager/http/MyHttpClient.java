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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.http.HttpClientBaseImpl;

public class MyHttpClient extends HttpClientBaseImpl{

	private final Logger logger = Logger.getLogger(getClass());
	private final int timeOut = 30 * 1000;
	private static final String DEFAULT_ENCODING = "UTF-8";
	@Override
	public String postJson(String url, String jsonMessage) {
		try {
			url = TerminalFactory.getSDK().getServiceBusManager().getUrl(url);
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
				logger.info("发送了一个post请求：url="+url+",收到的信息为："+responseString);
				return responseString;
			}
		} catch (Exception e) {
			logger.error("命令发送失败，url=" + url + ", jsonMessage=" + jsonMessage,
					e);
			TerminalFactory.getSDK().getServiceBusManager().addErrorCount();
		}
		return null;
	}

	@Override
	public String postJson2(String url, String jsonMessage) {
		try {
			url = TerminalFactory.getSDK().getServiceBusManager().getUrl(url);
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
				logger.info("发送了一个post请求：url="+url+",收到的信息为："+responseString);
				return responseString;
			}
		} catch (Exception e) {
			logger.error("命令发送失败，url=" + url + ", jsonMessage=" + jsonMessage,
					e);
			TerminalFactory.getSDK().getServiceBusManager().addErrorCount();
		}
		return null;
	}


	@Override
	public String post2(String url, Map<String, String> paramsMap) {
		try {
			url = TerminalFactory.getSDK().getServiceBusManager().getUrl(url);
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
			TerminalFactory.getSDK().getServiceBusManager().addErrorCount();
		}
		return null;
	}

	@Override
	public String post(String url, Map<String, String> paramsMap) {
		try {
			url = TerminalFactory.getSDK().getServiceBusManager().getUrl(url);
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
			TerminalFactory.getSDK().getServiceBusManager().addErrorCount();
		}
		return null;
	}
	@Override
	public String get(String url, Map<String,String> paramsMap) {
		try {
			url = TerminalFactory.getSDK().getServiceBusManager().getUrl(url);
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
			TerminalFactory.getSDK().getServiceBusManager().addErrorCount();
		}
		return null;
	}

	@Override
	public String postFile(String url, File file, Map<String, String> paramsMap) {
		try{
//			url = TerminalFactory.getSDK().getServiceBusManager().getUrl(url);
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
//			TerminalFactory.getSDK().getServiceBusManager().addErrorCount();
		}
		return null;
	}

	private DefaultHttpClient getHttpClient(int timeOut) {
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setSoTimeout(httpParams, timeOut);
		HttpConnectionParams.setConnectionTimeout(httpParams, timeOut);
		return new DefaultHttpClient(httpParams);
	}

	@Override
	public String sendGet(String url) {
		String result = "";
		BufferedReader in = null;
		try {
			url = TerminalFactory.getSDK().getServiceBusManager().getUrl(url);
//			logger.info("发送get请求" + url);
			String urlNameString = url;
			URL realUrl = new URL(urlNameString);
			// 打开和URL之间的连接
			URLConnection connection = realUrl.openConnection();
			// 设置通用的请求属性
			connection.setDoOutput(false);
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf8");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 建立实际的连接
			connection.connect();
			// 定义 BufferedReader输入流来读取URL的响应
			InputStream inputStream = connection.getInputStream();
			if (inputStream != null){
				in = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
				String line;
				if (in != null){
					while ((line = in.readLine()) != null) {
						result += line;
					}
				}
			}
			logger.info("发送了一个get请求：url="+url+"\n 收到的信息为："+result);
		} catch (Exception e) {
			logger.error("发送GET请求出现异常！",e);
			TerminalFactory.getSDK().getServiceBusManager().addErrorCount();
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				logger.error("",e2);
			}
		}
		return result;
	}

	@Override
	public String sendGet(String url, Map<String, String> paramsMap) {
		return sendGet(getUrl(url, paramsMap));
	}
}
