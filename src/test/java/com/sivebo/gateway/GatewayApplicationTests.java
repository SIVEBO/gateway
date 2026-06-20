package com.sivebo.gateway;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.gateway.server.mvc.config.GatewayMvcProperties;
import org.springframework.cloud.gateway.server.mvc.config.RouteProperties;

@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
    }
)
class GatewayApplicationTests {

	@Autowired
	GatewayMvcProperties gatewayMvcProperties;

	@Test
	void contextLoads() {}

	@Test
	void allTenRoutesConfigured() {
		List<RouteProperties> routes = gatewayMvcProperties.getRoutes();

		assertNotNull(routes);
		assertEquals(10, routes.size());

		List<String> ids = routes.stream().map(RouteProperties::getId).toList();
		assertTrue(ids.containsAll(List.of(
			"ms-sucursales", "ms-embalaje", "ms-finanzas", "ms-paquetes",
			"ms-auth", "ms-admision", "ms-clientes", "ms-portal", "ms-ventas", "ms-tracking"
		)));
	}

	@Test
	void routeUris_useLoadBalancedDiscovery() {
		List<RouteProperties> routes = gatewayMvcProperties.getRoutes();

		assertNotNull(routes);
		assertTrue(routes.stream().allMatch(r -> r.getUri().getScheme().equals("lb")));
	}

	@Test
	void sucursalesRoute_hasCorrectUriAndPredicate() {
		RouteProperties route = gatewayMvcProperties.getRoutes().stream()
			.filter(r -> r.getId().equals("ms-sucursales"))
			.findFirst()
			.orElseThrow();

		assertEquals(URI.create("lb://MS_SUCURSALES"), route.getUri());
		String predicateArgs = route.getPredicates().get(0).getArgs().toString();
		assertTrue(predicateArgs.contains("/api/v1/sucursales/**"));
	}
}
