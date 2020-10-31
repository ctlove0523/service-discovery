package io.github.ctlove0523.discovery;

import java.util.List;

public interface ServiceResolver {
	List<Instance> resolve(String name);
}
