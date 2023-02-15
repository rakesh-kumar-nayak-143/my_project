
package com.te.flinko.audit;
import java.util.Optional;

import org.springframework.data.domain.AuditorAware;

public class EntityAuditorAware extends BaseConfigController implements AuditorAware<Long> {

	@Override
	public Optional<Long> getCurrentAuditor() {
		System.out.println(getUserId());
		return Optional.of(getUserId());
	}

}
