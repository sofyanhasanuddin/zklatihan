package org.sofyan.latihan.app.ctrl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sofyan.latihan.app.bean.VisitSearchBean;
import org.sofyan.latihan.app.model.Visit;
import org.sofyan.latihan.app.model.VisitDetail;
import org.sofyan.latihan.app.repository.specification.VisitSpecification;
import org.sofyan.latihan.app.service.BaseService;
import org.sofyan.latihan.app.service.VisitDetailService;
import org.sofyan.latihan.app.service.VisitService;
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
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Window;

public class VisitCtrl extends PageCtrl<Visit> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Autowired
	private VisitService visitServiceImpl;
	
	@Autowired
	private VisitDetailService visitDetailServiceImpl;
	
	private VisitSearchBean vsb = new VisitSearchBean();
	
	private AnnotateDataBinder binder;
	
	private class ListRender implements ListitemRenderer<Visit> {

		public void render(Listitem listItem, Visit v, int index)
				throws Exception {

			listItem.setValue( v );
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
			
			new Listcell( v.getOwner().getName() ).setParent( listItem );
			new Listcell( sdf.format( v.getEntryDate()) ).setParent( listItem );
			new Listcell( sdf.format( v.getLeaveDate()) ).setParent( listItem );
			new Listcell( v.getListVisitDetail()
						   .stream()
						   .map( vd -> vd.getPet().getName())
						   .collect( Collectors.joining(", ") ) ).setParent( listItem );
			
			Div div = new Div();
			Listcell cell = new Listcell();
			
			Button btnEdit = new Button("edit");
			btnEdit.addEventListener( Events.ON_CLICK, 
				evt -> edit( v, index )
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
							 				delete( v );
							 		}
							 	}
							);
				}
				
			});
			
			Button btnVisit = new Button("new visit");
			btnVisit.addEventListener( Events.ON_CLICK, new EventListener<Event>() {

				@Override
				public void onEvent(Event event) throws Exception {
					Visit vNew = new Visit();
					vNew.setOwner( v.getOwner() );
					showVisitForm( vNew );
				}
				
			});
			
			div.appendChild( btnEdit );
			div.appendChild( btnDelete );
			div.appendChild( btnVisit );

			cell.appendChild( div );
			cell.setParent( listItem );
			
		}
		
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		
		binder = new AnnotateDataBinder( this.getSelf() );
		binder.bindBean("vsb", this.vsb );
		binder.loadAll();
		
	}

	@Override
	void fillListData(int offset, int max) {

		PageRequest pr = buildPageRequest(offset, 10, null);
		
		Page<Visit> pageVisit = this.visitServiceImpl.readAll( getSpecification(), pr );
		List<Long> listIds = pageVisit.getContent()
									.stream()
									.filter( v->v.getId() != null && !v.getId().equals( 0L ) )
									.map( v->v.getId() )
									.collect( Collectors.toList() );
		
		//Get one to many value
		if( !CollectionUtils.isEmpty( listIds ) ) {
			Map<Long, List<VisitDetail>> mapVisit = this.visitDetailServiceImpl.findAllByVisitIdIn( listIds )
									.stream()
									.collect( Collectors.groupingBy( vd -> vd.getVisit().getId() ) );
			
			pageVisit.getContent()
					 .forEach( vd -> vd.setListVisitDetail( mapVisit.get( vd.getId() )) );
		}
		
		if( !CollectionUtils.isEmpty( pageVisit.getContent()) )			
			this.listModel = new ListModelList<Visit>( pageVisit.getContent() );
		else
			this.listModel = new ListModelList<Visit>();
				
		this.mainList.setItemRenderer( new ListRender());
		this.mainList.setModel( this.listModel );
		
		this.mainPaging.setPageSize( 10 );
		this.mainPaging.setTotalSize( new BigDecimal( pageVisit.getTotalElements() ).intValueExact() );
		
	}
	
	private void showVisitForm(Visit vNew) {
		
		Map<String, Object> mapParam = new HashMap<String, Object>();
		mapParam.put("param", vNew );
		
		Window window = (Window) Executions.createComponents( getZulFormName(), null, mapParam);
        window.doModal();
		
	}

	@Override
	String getZulFormName() {
		return "/page/visitform.zul";
	}

	@Override
	Specification<Visit> getSpecification() {
		return VisitSpecification.findByCriteria( this.vsb );
	}

	@Override
	BaseService<Visit, Long> getService() {
		return this.visitServiceImpl;
	}

}
