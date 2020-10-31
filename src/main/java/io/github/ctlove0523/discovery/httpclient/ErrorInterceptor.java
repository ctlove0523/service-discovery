package io.github.ctlove0523.discovery.httpclient;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorInterceptor implements Interceptor {
	private static final Logger log = LoggerFactory.getLogger(ErrorInterceptor.class);

	@NotNull
	@Override
	public Response intercept(@NotNull Chain chain) throws IOException {
		Request request = chain.request();
		Response response = chain.proceed(request);
		if (!response.isSuccessful()) {
			ResponseBody responseBody = response.body();
			if (responseBody != null) {
				MediaType mediaType = responseBody.contentType();
				String responseContent = responseBody.string();
				log.error("Request {} failed,http status {},error message {}", request.url(),
						response.code(), responseBody);
				return response.newBuilder()
						.body(ResponseBody.create(responseContent, mediaType))
						.build();
			}
			else {
				log.error("Request {} failed", request.url());
			}
		}
		return response;
	}
}
