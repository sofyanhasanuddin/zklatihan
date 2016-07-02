package org.sofyan.latihan.app.ctrl;

import java.util.ArrayList;
import java.util.List;

import org.sofyan.latihan.app.model.BaseEntity;
import org.sofyan.latihan.app.service.BaseService;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.WrongValuesException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.InputElement;

public abstract class BaseForm<T extends BaseEntity> extends SelectorComposer<Window> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Wire
	protected Component form;
	
	protected AnnotateDataBinder binder;
	
	protected abstract void beforeSave();
	
	protected abstract BaseService<T, Long> getService();
	
	protected abstract T getObjectForm();
	
	protected abstract String getSuccessSaveMessage();
	
	private List<WrongValueException> listWrongs = new ArrayList<WrongValueException>();
	
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		
		super.doAfterCompose(comp);
		
		SpringUtil.getApplicationContext().getAutowireCapableBeanFactory().autowireBean( this );
		
	}
	
	/**
	 * Override this method for another behaviour
	 */
	protected void afterSave() {
		Events.postEvent(new Event("onClose", this.getSelf(), getObjectForm() ));
	};
	
	protected void save() {
		getService().save( getObjectForm() );
	}
	
	@Listen("onClick=#btnSave")
	public void saveAction() {
		
		check( form );
		if( !CollectionUtils.isEmpty( this.listWrongs )) {
			WrongValueException[] ws = new WrongValueException[ this.listWrongs.size() ];
			for(int i=0;i<this.listWrongs.size();i++) {
				ws[i] = this.listWrongs.get(i);
			}
			
			this.listWrongs.clear();
			throw new WrongValuesException( ws );
		}
		
		String msg = getSuccessSaveMessage();
		
		beforeSave();
		save();
		afterSave();
		
		if( !StringUtils.isEmpty( msg ) )
			Clients.showNotification( msg, false );
		
	}
	
	@Listen("onClick=#btnCancel")
	public void cancelAction() {
		this.getSelf().detach();
	}
	
	private void check(Component component) {
		
		checkIsValid(component);
          
		component.getChildren().forEach( this::check );
		
	}
    
	private void checkIsValid(Component component) {
		if (component instanceof InputElement) {
            if (!((InputElement) component).isValid()) {
            	try {
                  // Force show errorMessage
                  ((InputElement) component).getText();
            	}catch(Exception e) {
            		listWrongs.add( new WrongValueException(((InputElement) component), e.getMessage().toString() ));
            	}
            }
		}
	}
	
	protected void hideValidation(Component component) {
		
		hideValidationMessage( component );
        
        component.getChildren().forEach( this::hideValidation );
		
	}
	
	private void hideValidationMessage(Component component) {
		
		if (component instanceof InputElement) {
            if (!((InputElement) component).isValid()) {
            	Clients.clearWrongValue( (InputElement) component );
            }
		}
	}

}
