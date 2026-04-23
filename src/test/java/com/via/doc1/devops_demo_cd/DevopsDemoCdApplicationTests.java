package com.via.doc1.devops_demo_cd;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class DevopsDemoCdApplicationTests {

	@Test
	void main_delegatesToSpringApplicationRun() {
		try (MockedStatic<SpringApplication> springApplicationMock = org.mockito.Mockito.mockStatic(SpringApplication.class)) {
			DevopsDemoCdApplication.main(new String[]{});

			springApplicationMock.verify(() -> SpringApplication.run(DevopsDemoCdApplication.class, new String[]{}));
		}
	}

}
