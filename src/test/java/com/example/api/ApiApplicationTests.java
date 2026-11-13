package com.example.api;

import com.example.api.controller.AccesoController;
import com.example.api.controller.CotizacionController;
import com.example.api.controller.EventoController;
import com.example.api.controller.LugarController;
import com.example.api.controller.PagoController;
import com.example.api.controller.SectorController;
import com.example.api.controller.WebhookController;
import com.example.api.repository.LugarRepository;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ApiApplicationTests {

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@Autowired
	private RequestMappingHandlerMapping handlerMapping;

	@Autowired
	private LugarRepository lugarRepository;

	@Test
	void contextLoads() {
		assertEquals(6, entityManagerFactory.getMetamodel().getEntities().size());
		assertNotNull(lugarRepository);

		assertControllerIsRegistered(AccesoController.class);
		assertControllerIsRegistered(CotizacionController.class);
		assertControllerIsRegistered(EventoController.class);
		assertControllerIsRegistered(LugarController.class);
		assertControllerIsRegistered(PagoController.class);
		assertControllerIsRegistered(SectorController.class);
		assertControllerIsRegistered(WebhookController.class);
	}

	private void assertControllerIsRegistered(Class<?> controllerType) {
		assertTrue(
				handlerMapping.getHandlerMethods().values().stream()
						.anyMatch(handler -> handler.getBeanType().equals(controllerType)),
				() -> controllerType.getSimpleName() + " should be registered"
		);
	}

}
