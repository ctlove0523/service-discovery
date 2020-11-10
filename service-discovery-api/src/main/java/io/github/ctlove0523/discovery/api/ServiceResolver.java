package io.github.ctlove0523.discovery.api;

import java.util.List;

public interface ServiceResolver extends Order {
	List<Instance> resolve(String name);
}
