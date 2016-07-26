package org.sofyan.latihan.app.ctrl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.sofyan.latihan.app.model.Owner;
import org.sofyan.latihan.app.model.Pet;
import org.sofyan.latihan.app.service.BaseService;
import org.sofyan.latihan.app.service.OwnerService;
import org.sofyan.latihan.app.service.PetService;
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
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class CustomerFormCtrl extends BaseForm<Owner> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Autowired
	private OwnerService ownerServiceImpl;
	
	@Autowired
	private PetService petServiceImpl;
	
	private Owner owner = new Owner();
	
	@Wire
	private Textbox sampleTxt;
	
	@Wire
	private Grid gridPet;
	private ListModelList<Pet> modelPet;
	private List<Pet> deletedPets = new ArrayList<Pet>();
	
	private class RowRender implements RowRenderer<Pet> {

		@Override
		public void render(org.zkoss.zul.Row row, Pet data, int index)
				throws Exception {

			row.setValue( data );
			
			
			String birth = new SimpleDateFormat("dd-MMM-yyyy").format( data.getBirthDate() );
			
			row.appendChild( new Label( data.getName() ) );
			row.appendChild( new Label( data.getType().getName() ) );
			row.appendChild( new Label( birth ));
			
			Div div = new Div();
			
			Button btnEdit = new Button("edit");
			btnEdit.addEventListener( Events.ON_CLICK, 
				e -> editPet( data, index ) 
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
	
	@SuppressWarnings("deprecation")
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		
		if( UIUtil.getArg("param") != null ) {
			this.owner = (Owner) UIUtil.getArg("param");
		}
		
		this.binder = new AnnotateDataBinder( this.getSelf() );
		this.binder.bindBean("customer", owner);
		this.binder.loadAll();
		
		this.owner.setListPets( this.petServiceImpl.findAllActivePetByOwnerId( this.owner.getId() ) );
		
		fillPetGrid();
		
	}
	
	void fillPetGrid() {
		
		if( CollectionUtils.isEmpty( this.owner.getListPets() )) {
			this.modelPet = new ListModelList<Pet>();
		} else {
			this.modelPet = new ListModelList<Pet>( this.owner.getListPets() );
		}
		
		this.gridPet.setModel( modelPet );
		this.gridPet.setRowRenderer( new RowRender() );
		
	}
	
	
	void deletePet(Pet pet, int index) {
		
		pet.setDeleted( true );
		
		deletedPets.add( pet );
		
		this.modelPet.remove( index );
		
		this.owner.setListPets( this.modelPet );
		
	}
	
	@Listen("onClick=#btnAddPet")
	public void addPet() {
		
		Window window = (Window) Executions.createComponents( "/page/petform.zul", null, null);
        window.doModal();
        
        window.addEventListener( Events.ON_CLOSE, 
        	evt -> {
        		
    			if( CollectionUtils.isEmpty( owner.getListPets() ) )
					owner.setListPets(new ArrayList<Pet>());
    			
				owner.getListPets().add( (Pet) evt.getData() );
				
				fillPetGrid();
    		}
        );
        
        hideValidation( this.form );
        
	}
	
	void editPet(Pet pet, int index) {
		
		Map<String, Object> mapParam = new HashMap<String, Object>();
		mapParam.put("param", pet );
		
		Window window = (Window) Executions.createComponents( "/page/petform.zul", null, mapParam);
        window.doModal();
        
        window.addEventListener(Events.ON_CLOSE, 
        	evt -> modelPet.set(index,  (Pet) evt.getData() )
        );
        
        hideValidation( this.form );
		
	}

	@Override
	protected void beforeSave() {
		
		if( CollectionUtils.isEmpty( this.modelPet) )
			throw new RuntimeException("Customer should have pet");
		
		Predicate<Pet> hasId = p -> p.getId() != null;
		Consumer<Pet> setOwner = p -> p.setOwner( owner );
		
		//Set pet
		this.owner.setListPets( this.modelPet );
		
		//Set deleted pet
		this.owner.getListPets().addAll( this.deletedPets
											 .stream()
											 .filter( hasId )
											 .collect( Collectors.toList() ));
		//set owner for each pet
		this.owner.getListPets()
				  .forEach( setOwner );
		
	}

	@Override
	protected BaseService<Owner, Long> getService() {
		return this.ownerServiceImpl;
	}

	@Override
	protected Owner getObjectForm() {
		return owner;
	}

	@Override
	protected String getSuccessSaveMessage() {
		if( owner.getId() == null || owner.getId().equals( 0L ) )
			return "Successfully save customer";
		else
			return "Successfully edit customer";
	}

}
