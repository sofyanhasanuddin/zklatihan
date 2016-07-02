package org.sofyan.latihan.app.ctrl;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.function.Consumer;

import org.sofyan.latihan.app.model.Pet;
import org.sofyan.latihan.app.model.TreatmentType;
import org.sofyan.latihan.app.model.VisitDetail;
import org.sofyan.latihan.app.model.VisitDetailTreatmentType;
import org.sofyan.latihan.app.service.BaseService;
import org.sofyan.latihan.app.service.PetService;
import org.sofyan.latihan.app.service.TreatmentTypeService;
import org.sofyan.latihan.app.spring.util.UIUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.google.common.collect.Lists;

public class VisitPetFormCtrl extends BaseForm<VisitDetail> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Autowired
	private PetService petServiceImpl;
	@Autowired
	private TreatmentTypeService treatmentTypeServiceImpl;
	
	@Wire
	private Hbox treatmentTypeBox;
	@Wire
	private Listbox pets;
	@Wire
	private Label txtType;
	@Wire
	private Label txtBod;
	
	private VisitDetail vd = new VisitDetail();
	
	private Consumer<TreatmentType> createCheckBox = t -> {
			
		Checkbox ch = new Checkbox( t.getName() );
		ch.setValue( t );
		
		if( !CollectionUtils.isEmpty( this.vd.getListTreatmentType() )) {
			this.vd.getListTreatmentType()
				.forEach( ty -> {
					if( ty.getTreatmentType().getId().equals( t.getId() ) )
						ch.setChecked( true );
				});
		}
			
		treatmentTypeBox.appendChild( ch );
		
	};
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		
		if( UIUtil.getArg("visitdetail") != null )
			this.vd = (VisitDetail) UIUtil.getArg("visitdetail");
		
		//either new or update should have pets
		List<Pet> pets =  (List<Pet>) UIUtil.getArg("pets");
		if( CollectionUtils.isEmpty(pets)  ) 
			throw new Exception("Creating pets visit should have pets, Pets not found as parameter.");
		
		//set renderer for pet types selectbox
		this.pets.setItemRenderer( new ListitemRenderer<Pet>() {

			@Override
			public void render(Listitem item, Pet data, int index)
					throws Exception {
				
				item.setValue( data );
				
				new Listcell( data.getName() ).setParent( item );
				
				if( index == 0 || 
						( vd.getPet() != null && vd.getPet().getId().equals( data.getId() )) ) {
					item.setSelected(true);
					changePetInfo( data );
				}
				
			}
			
		});
		
		//Create the checkbox based on treatment type
		this.treatmentTypeServiceImpl
			.findAll()
			.forEach( createCheckBox );
		
		binder = new AnnotateDataBinder( this.getSelf() );
		binder.bindBean("visitdetail", this.vd );
		binder.bindBean("petsModel", pets );
		binder.loadAll();
		
		this.pets.addEventListener( Events.ON_SELECT, new EventListener<Event>() {
			@SuppressWarnings("rawtypes")
			@Override
			public void onEvent(Event event) throws Exception {
				changePetInfo( (Pet) ( (SelectEvent) event ).getSelectedObjects().iterator().next() );
			}
		});
		
	}
	
	private void changePetInfo(Pet data) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
		
		txtType.setValue( data.getType().getName() );
		txtBod.setValue( sdf.format( data.getBirthDate() ) );
	}
	
	@Override
	public void save() {
		//Override for doing nothing
	}

	@Override
	protected void beforeSave() {
		
		this.vd.setPet( pets.getSelectedItem().getValue() );
		this.vd.setDeleted( new Boolean(false) );
		
		//Get treatment type
		List<VisitDetailTreatmentType> listTreatment = Lists.newArrayList();
		this.treatmentTypeBox.getChildren()
			.forEach( c -> { 
				if( ( (Checkbox) c ).isChecked() ) {
					
					VisitDetailTreatmentType v = new VisitDetailTreatmentType();
					v.setTreatmentType( ( (Checkbox) c ).getValue() );
					v.setVisitDetail( vd );
					listTreatment.add( v );
				}
			});
		
		//Throw error if not select any treatment
		if( CollectionUtils.isEmpty(listTreatment) )
			throw new WrongValueException("Please select any treatment type");
		
		this.vd.setListTreatmentType( listTreatment );
		
	}

	@Override
	protected BaseService<VisitDetail, Long> getService() {
		return null;
	}

	@Override
	protected VisitDetail getObjectForm() {
		return this.vd;
	}

	@Override
	protected String getSuccessSaveMessage() {
		return null;
	}

}
