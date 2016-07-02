package org.sofyan.latihan.app.service;

import org.sofyan.latihan.app.model.TreatmentType;
import org.sofyan.latihan.app.repository.TreatmentTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Scope(value="singleton",proxyMode=ScopedProxyMode.TARGET_CLASS)
public class TreatmentTypeServiceImpl extends BaseServiceImpl<TreatmentType, Long> implements TreatmentTypeService {

	private TreatmentTypeRepository treatmentTypeRepository;
	
	@Autowired
	public TreatmentTypeServiceImpl(TreatmentTypeRepository treatmentTypeRepository) {
		super(treatmentTypeRepository);
		this.treatmentTypeRepository = treatmentTypeRepository;
	}

}
