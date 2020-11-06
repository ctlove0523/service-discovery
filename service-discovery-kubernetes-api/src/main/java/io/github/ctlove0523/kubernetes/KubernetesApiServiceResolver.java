package io.github.ctlove0523.kubernetes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.ctlove0523.commons.UrlUtil;
import io.github.ctlove0523.discovery.api.Instance;
import io.github.ctlove0523.discovery.api.ServiceResolver;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1Service;
import okhttp3.Request;

public class KubernetesApiServiceResolver implements ServiceResolver {
	private static final String DEFAULT_NAME_SPACE = "default";
	private String apiServerHost;
	private String token;
	private static final String QUERY_SERVICE_URL = "/api/v1/namespaces/{namespace}/services/{name}";
	private static final String LIST_POD_URL = "api/v1/namespaces/{namespace}/pods";

	@Override
	public List<Instance> resolve(String name) {
		return null;
	}

	private V1Service showService(String namespace, String name) {
		// todo check paras
		if (namespace == null || namespace.isEmpty()) {
			namespace = DEFAULT_NAME_SPACE;
		}

		String url = UrlUtil.urlPathFormat(QUERY_SERVICE_URL, namespace, name);
		Map<String, String> queryParas = new HashMap<>();
		queryParas.put("pretty", "true");
		url = UrlUtil.urlQueryParametersFormat(url, queryParas);
		Request request = new Request.Builder()
				.get()
				.url(apiServerHost + url)
				.addHeader("Authorization", "Bearer " + token)
				.build();

		return HttpsClient.syncCall(request, V1Service.class);

	}

	private V1PodList listPodsBySelector(String namespace, String selector) {
		if (namespace == null || namespace.isEmpty()) {
			namespace = DEFAULT_NAME_SPACE;
		}

		String url = UrlUtil.urlPathFormat(LIST_POD_URL, namespace);
		Map<String, String> queryParas = new HashMap<>();
		queryParas.put("pretty", "true");
		queryParas.put("labelSelector", selector);
		url = UrlUtil.urlQueryParametersFormat(url, queryParas);

		Request request = new Request.Builder()
				.get()
				.url(apiServerHost + url)
				.addHeader("Authorization", "Bearer " + token)
				.build();

		return HttpsClient.syncCall(request, V1PodList.class);
	}
}
