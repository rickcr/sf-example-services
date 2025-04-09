package net.reumann.sf;
 
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import net.reumann.sf.service.SfDataServicesConfig;
import net.reumann.sf.service.SfDataSourceProperties;


@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = {SfDataServicesConfig.class, SfDataSourceProperties.class},
		initializers = ConfigDataApplicationContextInitializer.class)
public class BaseIT {

	//so maven doesn't complain about this class not having any tests
	@Test
	public void nothing() {
		Assertions.assertTrue(true);
	}

}
