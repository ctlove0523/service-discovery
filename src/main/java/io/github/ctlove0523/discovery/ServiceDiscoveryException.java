package io.github.ctlove0523.discovery;

public class ServiceDiscoveryException extends RuntimeException {
	public ServiceDiscoveryException(String errorMessage, Throwable e) {
		super(errorMessage, e);
	}
}
