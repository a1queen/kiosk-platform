package com.example.KioskService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class KioskServiceApplicationTest {

    @Test
    @DisplayName("Should invoke SpringApplication.run when main is called")
    void shouldInvokeSpringApplicationRun() {
        try (MockedStatic<SpringApplication> springAppMock = mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
            springAppMock.when(() -> SpringApplication.run(KioskServiceApplication.class, new String[]{}))
                    .thenReturn(mockContext);

            KioskServiceApplication.main(new String[]{});

            springAppMock.verify(() -> SpringApplication.run(KioskServiceApplication.class, new String[]{}));
        }
    }

    @Test
    @DisplayName("Should instantiate KioskServiceApplication")
    void shouldInstantiateApplicationClass() {
        KioskServiceApplication application = new KioskServiceApplication();
        assertThat(application).isNotNull();
    }
}
