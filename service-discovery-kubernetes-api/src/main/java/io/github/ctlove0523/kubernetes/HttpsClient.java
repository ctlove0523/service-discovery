package io.github.ctlove0523.kubernetes;

import java.io.IOException;

import io.github.ctlove0523.commons.httpclient.HttpClientFactory;
import io.github.ctlove0523.commons.serialization.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Slf4j
public class HttpsClient {
	private static final OkHttpClient client = HttpClientFactory.getHttpsClient();

	public static <T> T syncCall(Request request, Class<T> type) {
		try (Response response = client.newCall(request).execute();
			 ResponseBody responseBody = response.body()) {
			if (response.isSuccessful() && responseBody != null && type != null) {
				return JacksonUtil.json2Object(responseBody.string(), type);
			}

		}
		catch (IOException e) {
			log.error("sync call failed ", e);
		}

		return null;
	}
}
