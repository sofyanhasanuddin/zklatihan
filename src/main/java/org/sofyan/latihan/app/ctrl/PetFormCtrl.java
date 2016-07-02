package org.sofyan.latihan.app.ctrl;

import org.sofyan.latihan.app.model.Pet;
import org.sofyan.latihan.app.model.PetType;
import org.sofyan.latihan.app.service.BaseService;
import org.sofyan.latihan.app.service.PetService;
import org.sofyan.latihan.app.service.PetTypeService;
import org.sofyan.latihan.app.spring.util.UIUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Window;

public class PetFormCtrl extends BaseForm<Pet> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Autowired
	private PetService petServiceImpl;
	
	@Autowired
	private PetTypeService petTypeServiceImpl;
	
	@Wire
	private Listbox petTypes;
	
	private Pet pet = new Pet();
	
	@SuppressWarnings("deprecation")
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		
		if( UIUtil.getArg("param") != null ) {
			this.pet = (Pet) UIUtil.getArg("param");
		}
		
		this.petTypes.setItemRenderer(new ListitemRenderer<PetType>() {

			@Override
			public void render(Listitem item, PetType data, int index)
					throws Exception {
				
				item.setValue( data );

				new Listcell( data.getName() ).setParent(item);
				
				if( index == 0 || pet.getType() != null && pet.getType().getId().equals( data.getId() ) )
					item.setSelected(true);
			}
		});
		
		binder = new AnnotateDataBinder( this.getSelf() );
		binder.bindBean("petType", this.petTypeServiceImpl.findAll() );
		binder.bindBean("pet", this.pet );
		binder.loadAll();
		
	}
	
	@Override
	public void save() {

	}

	@Override
	protected void beforeSave() {

		this.pet.setType( ( (Listbox) this.getSelf().getFellow("petTypes") ).getSelectedItem().getValue() );
		
	}

	@Override
	protected BaseService<Pet, Long> getService() {
		return this.petServiceImpl;
	}

	@Override
	protected Pet getObjectForm() {
		return this.pet;
	}

	@Override
	protected String getSuccessSaveMessage() {
		return null;
	}

}
