package io.github.ctlove0523.discovery.api;

import java.util.List;

public interface ServiceResolver {
	List<Instance> resolve(String name);
}
