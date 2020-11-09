package io.github.ctlove0523.discovery.consul;

import java.util.List;

import com.ecwid.consul.v1.ConsulClient;
import io.github.ctlove0523.discovery.api.Instance;
import io.github.ctlove0523.discovery.api.ServiceResolver;

public class ConsulServiceResolver implements ServiceResolver {
	private ConsulClient client;

	@Override
	public List<Instance> resolve(String name) {
		return null;
	}
}
