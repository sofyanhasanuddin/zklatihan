package org.sofyan.latihan.app.service;

import java.util.List;

import org.sofyan.latihan.app.model.Pet;
import org.sofyan.latihan.app.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Scope(value="singleton",proxyMode=ScopedProxyMode.TARGET_CLASS)
public class PetServiceImpl extends BaseServiceImpl<Pet, Long> implements PetService {

	private PetRepository petRepository;
	
	@Autowired
	public PetServiceImpl(PetRepository petRepository) {
		super(petRepository);
		this.petRepository = petRepository;
	}
	
	@Override
	public List<Pet> findAllActivePetByOwnerId(Long id) {
		return this.petRepository.findAllByOwnerIdAndDeletedIsFalse( id );
	}

}
