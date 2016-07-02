package org.sofyan.latihan.app.ctrl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.sofyan.latihan.app.model.Pet;
import org.sofyan.latihan.app.model.Visit;
import org.sofyan.latihan.app.model.VisitDetail;
import org.sofyan.latihan.app.model.VisitDetailTreatmentType;
import org.sofyan.latihan.app.service.BaseService;
import org.sofyan.latihan.app.service.PetService;
import org.sofyan.latihan.app.service.VisitDetailService;
import org.sofyan.latihan.app.service.VisitService;
import org.sofyan.latihan.app.spring.util.UIUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.zkoss.zhtml.Div;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.Button;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class VisitFormCtrl extends BaseForm<Visit> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Autowired
	private VisitService visitServiceImpl;
	
	@Autowired
	private PetService petServiceImpl;
	
	@Autowired
	private VisitDetailService visitDetailServiceImpl;
	
	private Visit visit = new Visit();
	
	private class RowRender implements RowRenderer<VisitDetail> {

		@Override
		public void render(org.zkoss.zul.Row row, VisitDetail data, int index)
				throws Exception {

			row.setValue( data );
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
			String birth = sdf.format( data.getPet().getBirthDate() );
			
			row.appendChild( new Label( data.getPet().getName() ) );
			row.appendChild( new Label( data.getPet().getType().getName() ) );
			row.appendChild( new Label( birth ));
			
			Div div = new Div();
			
			Button btnEdit = new Button("edit");
			btnEdit.addEventListener( Events.ON_CLICK, 
				e -> edit( data, index ) 
			);
			
			Button btnDelete = new Button("delete");
			btnDelete.addEventListener( Events.ON_CLICK, 
				e -> deletePet( data, index )
			);
			
			div.appendChild( btnEdit );
			div.appendChild( btnDelete );

			row.appendChild( div );
			
		}

	}
	
	@Wire
	private Grid gridPetVisitDetail;
	private ListModelList<VisitDetail> modelVisitPet;
	private List<VisitDetail> deletedVisitDetail = new ArrayList<VisitDetail>();
	
	@SuppressWarnings("deprecation")
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		
		if( UIUtil.getArg("param") != null )
			this.visit = (Visit) UIUtil.getArg("param");
		
		if( this.visit.getOwner() == null || 
				this.visit.getOwner().getId() == null || 
				this.visit.getOwner().getId().equals(0L) )
			throw new Exception("Creating visit should have owner, owner not found as param");
		
		loadPetTreatmentType();
		
		binder = new AnnotateDataBinder( this.getSelf() );
		binder.bindBean("visit", visit);
		binder.loadAll();
		
		loadPetTreatmentType();
		
		fillVisitDetailGrid();
		
	}
	
	private void loadPetTreatmentType() {

		//load one to many if not empty
		if( !CollectionUtils.isEmpty( this.visit.getListVisitDetail()) ) {
			
			List<Long> vDids = this.visit.getListVisitDetail()
									.stream()
									.map( vd -> vd.getId() ).collect( Collectors.toList() );

			Map<Long,List<VisitDetailTreatmentType>> map = this.visitDetailServiceImpl
					.findAllVisitDetailTreatmentTypeBasedOnVisitDetailIds( vDids )
					.stream()
					.collect( Collectors.groupingBy( v-> v.getVisitDetail().getId() ));
			
			this.visit.getListVisitDetail()
				.forEach( v -> v.setListTreatmentType( map.get( v.getId() )) );
			
		}
		
	}

	@Listen("onClick=#btnAddPet")
	public void addPet() {
		
		List<Pet> pets = this.petServiceImpl.findAllActivePetByOwnerId( this.visit.getOwner().getId() );
		
		Map<String, Object> param = Maps.newHashMap();
		param.put("pets", pets );
		
		Window window = (Window) Executions.createComponents( "/visitPetForm.zul", null, param);
        window.doModal();
        
        window.addEventListener( Events.ON_CLOSE, 
        	evt -> {
        		
        		if( CollectionUtils.isEmpty( visit.getListVisitDetail() ) )
					visit.setListVisitDetail( Lists.newArrayList() );
    			
        		visit.getListVisitDetail().add( (VisitDetail) evt.getData() );
				
				fillVisitDetailGrid();

    		}
        );
        
        hideValidation( form );
        
	}
	
	private void edit(VisitDetail data, int index) {
		
		List<Pet> pets = this.petServiceImpl.findAllActivePetByOwnerId( this.visit.getOwner().getId() );
		
		Map<String, Object> param = Maps.newHashMap();
		param.put("pets", pets );
		param.put("visitdetail", data );
		
		Window window = (Window) Executions.createComponents( "/visitPetForm.zul", null, param);
        window.doModal();
        
        window.addEventListener( Events.ON_CLOSE, 
        	evt -> modelVisitPet.set(index, (VisitDetail) evt.getData() )
        );
        
        hideValidation( form );
        
	}
	
	void fillVisitDetailGrid() {
		
		if( CollectionUtils.isEmpty( this.visit.getListVisitDetail() )) {
			this.modelVisitPet = new ListModelList<VisitDetail>();
		} else {
			this.modelVisitPet = new ListModelList<VisitDetail>( this.visit.getListVisitDetail() );
		}
		
		this.gridPetVisitDetail.setModel( this.modelVisitPet );
		this.gridPetVisitDetail.setRowRenderer( new RowRender() );
		
	}

	@Override
	protected void beforeSave() {
		
		if( CollectionUtils.isEmpty( this.modelVisitPet) )
			throw new RuntimeException("Customer should have visited pet");
		
		Predicate<VisitDetail> hasId = vd -> vd.getId() != null;
		Consumer<VisitDetail> setVisit = vd -> vd.setVisit( visit );
		
		//Set pet
		this.visit.setListVisitDetail( this.modelVisitPet );
		
		//Set deleted pet
		this.visit.getListVisitDetail().addAll( this.deletedVisitDetail
											 .stream()
											 .filter( hasId )
											 .collect( Collectors.toList() ));
		
		//set owner for each pet
		this.visit.getListVisitDetail()
				  .forEach( setVisit );
		
	}
	
	private void deletePet(VisitDetail vd, int index) {
		
		vd.setDeleted( true );
		
		this.deletedVisitDetail.add( vd );
		
		this.modelVisitPet.remove( index );
		
		this.visit.setListVisitDetail( this.modelVisitPet );
		
	}

	@Override
	protected BaseService<Visit, Long> getService() {
		return this.visitServiceImpl;
	}

	@Override
	protected Visit getObjectForm() {
		return visit;
	}

	@Override
	protected String getSuccessSaveMessage() {
		if( visit.getId() == null || visit.getId().equals( 0L ) )
			return "Successfully save visit";
		else
			return "Successfully edit visit";
	}

}
