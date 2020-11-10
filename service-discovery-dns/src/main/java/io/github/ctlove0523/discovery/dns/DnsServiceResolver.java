package io.github.ctlove0523.discovery.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import io.github.ctlove0523.discovery.api.Instance;
import io.github.ctlove0523.discovery.api.InstanceAddress;
import io.github.ctlove0523.discovery.api.ServiceDiscoveryException;
import io.github.ctlove0523.discovery.api.ServiceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DnsServiceResolver implements ServiceResolver {
	private static final Logger log = LoggerFactory.getLogger(DnsServiceResolver.class);
	private final DirContext dirContext;

	public DnsServiceResolver() {
		this.dirContext = createDirContext();
	}

	/**
	 * 返回一个域名对应的实例信息，返回的列表不允许修改
	 * @param name 域名
	 * @return 域名解析的实例列表
	 */
	@Override
	public List<Instance> resolve(String name) {
		try {
			return lookup(name);
		}
		catch (NamingException e) {
			log.error("resolve {} encounter name exception {}", name, e);
		}
		catch (UnknownHostException e) {
			log.error("resolve {} encounter unknown host exception", name, e);
		}
		return Collections.emptyList();
	}

	private static DirContext createDirContext() {
		Hashtable<String, String> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
		env.put(Context.PROVIDER_URL, "dns:");
		env.put("com.sun.jndi.dns.timeout.initial", String.valueOf(5 * 1000L));
		try {
			return new InitialDirContext(env);
		}
		catch (NamingException e) {
			throw new ServiceDiscoveryException("Error while initializing DirContext", e);
		}
	}

	private List<Instance> lookup(String name)
			throws NamingException, UnknownHostException {
		Set<String> addresses = new HashSet<>();
		Attributes attributes = dirContext.getAttributes(name, new String[] {"SRV"});
		Attribute srvAttribute = attributes.get("srv");
		if (srvAttribute != null) {
			NamingEnumeration<?> servers = srvAttribute.getAll();
			while (servers.hasMore()) {
				String server = (String) servers.next();
				String serverHost = extractHost(server);
				InetAddress address = InetAddress.getByName(serverHost);
				addresses.add(address.getHostAddress());
			}
		}

		if (addresses.isEmpty()) {
			log.warn("Could not find any service instance for name {}", name);
			return Collections.emptyList();
		}

		List<Instance> result = new ArrayList<>(addresses.size());
		for (String address : addresses) {
			result.add(new Instance(new InstanceAddress(address)));
		}
		return result;
	}

	/**
	 * 从DNS的记录中获取host
	 * 样例: "10 25 0 6235386366386436.gateway.default.svc.cluster.local".
	 */
	private static String extractHost(String server) {
		String host = server.split(" ")[3];
		return host.replaceAll("\\\\.$", "");
	}

	@Override
	public int getOrder() {
		return 0;
	}
}
