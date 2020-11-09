package io.github.ctlove0523.discovery.consul;

import java.util.List;

import io.github.ctlove0523.discovery.api.Instance;
import io.github.ctlove0523.discovery.api.ServiceResolver;

public class ConsulServiceResolver implements ServiceResolver {
	@Override
	public List<Instance> resolve(String name) {
		return null;
	}
}
