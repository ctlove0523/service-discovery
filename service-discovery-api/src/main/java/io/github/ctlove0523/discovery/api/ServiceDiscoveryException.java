package io.github.ctlove0523.discovery.api;

public class ServiceDiscoveryException extends RuntimeException {
	public ServiceDiscoveryException(String errorMessage, Throwable e) {
		super(errorMessage, e);
	}
}
