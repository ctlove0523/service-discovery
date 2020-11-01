package io.github.ctlove0523.kubernetes;

import java.util.List;

import io.github.ctlove0523.discovery.api.Instance;
import io.github.ctlove0523.discovery.api.ServiceResolver;

public class KubernetesApiServiceResolver implements ServiceResolver {
	@Override
	public List<Instance> resolve(String name) {
		return null;
	}
}
