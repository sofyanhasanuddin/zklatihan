package org.sofyan.latihan.app.service;

import org.sofyan.latihan.app.model.Owner;
import org.sofyan.latihan.app.repository.OwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Scope(value="singleton",proxyMode=ScopedProxyMode.TARGET_CLASS)
public class OwnerServiceImpl extends BaseServiceImpl<Owner, Long> implements OwnerService {

	private OwnerRepository ownerRepository;
	
	@Autowired
	public OwnerServiceImpl(OwnerRepository ownerRepository) {
		super(ownerRepository);
		this.ownerRepository = ownerRepository;
	}

}
