package org.sofyan.latihan.app.ctrl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.sofyan.latihan.app.bean.CustomerSearchBean;
import org.sofyan.latihan.app.model.Owner;
import org.sofyan.latihan.app.model.Visit;
import org.sofyan.latihan.app.repository.specification.CustomerSpecification;
import org.sofyan.latihan.app.service.BaseService;
import org.sofyan.latihan.app.service.OwnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.zkoss.zhtml.Div;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class CustomerCtrl extends PageCtrl<Owner> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Autowired
	private OwnerService ownerServiceImpl;

	private class ListRender implements ListitemRenderer<Owner> {

		public void render(Listitem listItem, Owner ow, int index)
				throws Exception {

			listItem.setValue( ow );
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
			String memberSince = sdf.format( ow.getCreatedDate() );
			
			Listcell cellName = new Listcell( ow.getName() );
			cellName.setStyle("text-align:left");
			cellName.setParent(listItem);
			
			new Listcell( ow.getTelephone() ).setParent( listItem );
			new Listcell( ow.getCity() ).setParent( listItem );
			new Listcell( memberSince ).setParent( listItem );
			
			Div div = new Div();
			Listcell cell = new Listcell();
			
			Button btnEdit = new Button("edit");
			btnEdit.addEventListener( Events.ON_CLICK, 
				evt -> edit( ow, index )
			);
			
			Button btnDelete = new Button("delete");
			btnDelete.addEventListener( Events.ON_CLICK, new EventListener<Event>() {

				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public void onEvent(Event event) throws Exception {
					
					Messagebox.show("Are you sure want to delete?", 
							"Question", Messagebox.OK | Messagebox.CANCEL,
							Messagebox.QUESTION,
							 	new EventListener(){
							 		public void onEvent(Event e){
							 			if(Messagebox.ON_OK.equals(e.getName()))
							 				delete( ow );
							 		}
							 	}
							);
					
					
				}
				
			});
			
			Button btnVisit = new Button("new visit");
			btnVisit.addEventListener( Events.ON_CLICK, new EventListener<Event>() {

				@Override
				public void onEvent(Event event) throws Exception {
					Visit v = new Visit();
					v.setOwner( ow );
					showVisitForm( v );
				}
				
			});
			
			div.appendChild( btnEdit );
			div.appendChild( btnDelete );
			div.appendChild( btnVisit );

			cell.appendChild( div );
			cell.setParent( listItem );
			
		}
		
	}
	
	@Override
	void fillListData(int offset, int max) {
		
		PageRequest pr = buildPageRequest(offset, 10, null);
		
		Page<Owner> pageOwner = this.ownerServiceImpl.findAll( getSpecification(), pr );
		
		if( !CollectionUtils.isEmpty( pageOwner.getContent()) )			
			this.listModel = new ListModelList<Owner>( pageOwner.getContent() );
		else
			this.listModel = new ListModelList<Owner>();
				
		this.mainList.setItemRenderer( new ListRender());
		this.mainList.setModel( this.listModel );
		
		this.mainPaging.setPageSize( 10 );
		this.mainPaging.setTotalSize( new BigDecimal( pageOwner.getTotalElements() ).intValueExact() );
		
	}
	
	private void showVisitForm(Visit v) {
		
		Map<String, Object> mapParam = new HashMap<String, Object>();
		mapParam.put("param", v );
		
		Window window = (Window) Executions.createComponents( "/page/visitform.zul", null, mapParam);
        window.doModal();
		
	}

	@Override
	String getZulFormName() {
		return "/page/customerform.zul";
	}

	@Override
	Specification<Owner> getSpecification() {
		
		CustomerSearchBean cs = new CustomerSearchBean();
		cs.setName( ( (Textbox) this.getSelf().getFellow("txtCustomerName")).getValue() );
		
		return CustomerSpecification.findByCriteria( cs );
	}

	@Override
	BaseService<Owner, Long> getService() {
		return this.ownerServiceImpl;
	}

}
