package io.github.ctlove0523.kubernetes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.github.ctlove0523.commons.UrlUtil;
import io.github.ctlove0523.discovery.api.Instance;
import io.github.ctlove0523.discovery.api.InstanceAddress;
import io.github.ctlove0523.discovery.api.ServiceResolver;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodStatus;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceSpec;
import okhttp3.Request;

public class KubernetesApiServiceResolver implements ServiceResolver {
	private static final String DEFAULT_NAME_SPACE = "default";
	private String apiServerHost;
	private String namespace;
	private String token;
	private static final String QUERY_SERVICE_URL = "/api/v1/namespaces/{namespace}/services/{name}";
	private static final String LIST_POD_URL = "api/v1/namespaces/{namespace}/pods";

	@Override
	public List<Instance> resolve(String name) {
		return lookup(name);
	}

	private List<Instance> lookup(String name) {
		V1Service service = showService(namespace, name);
		if (service == null) {
			return new ArrayList<>();
		}

		V1ServiceSpec v1ServiceSpec = service.getSpec();
		if (v1ServiceSpec == null) {
			return new ArrayList<>();
		}

		// todo map selector to string format
		Map<String, String> selector = v1ServiceSpec.getSelector();
		String stringFormatSelector = selector.entrySet()
			.stream()
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.joining(","));;

		V1PodList v1PodList = listPodsBySelector(namespace, stringFormatSelector);
		if (v1PodList == null || v1PodList.getItems() == null || v1PodList.getItems().isEmpty()) {
			return new ArrayList<>();
		}

		return v1PodList.getItems().stream()
				.filter(Objects::nonNull)
				.map(V1Pod::getStatus)
				.filter(Objects::nonNull)
				.map(V1PodStatus::getPodIP)
				.filter(Objects::nonNull)
				.map(s -> (new Instance(new InstanceAddress(s))))
				.collect(Collectors.toList());
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

	@Override
	public int getOrder() {
		return 2;
	}
}
