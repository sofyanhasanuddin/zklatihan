package org.sofyan.latihan.app.service;

import org.sofyan.latihan.app.model.PetType;
import org.sofyan.latihan.app.repository.PetTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Scope(value="singleton",proxyMode=ScopedProxyMode.TARGET_CLASS)
public class PetTypeServiceImpl extends BaseServiceImpl<PetType, Long> implements PetTypeService {

	private PetTypeRepository petTypeRepository;
	
	@Autowired
	public PetTypeServiceImpl(PetTypeRepository petTypeRepository) {
		super(petTypeRepository);
		this.petTypeRepository = petTypeRepository;
	}

}
