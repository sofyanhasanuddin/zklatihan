package org.sofyan.latihan.app.ctrl;

import java.util.HashMap;
import java.util.Map;

import org.sofyan.latihan.app.model.BaseEntity;
import org.sofyan.latihan.app.service.BaseService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

public abstract class PageCtrl<T extends BaseEntity> extends SelectorComposer<Window> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Wire
	protected Listbox mainList; //common use list
	protected ListModelList<T> listModel; //common use list model
	
	@Wire
	protected Paging mainPaging; //common use paging
	
	abstract void fillListData(int offset, int max);
	
	abstract String getZulFormName();
	
	abstract Specification<T> getSpecification();
	
	abstract BaseService<T, Long> getService();
	
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		
		SpringUtil.getApplicationContext().getAutowireCapableBeanFactory().autowireBean( this );
		
		initPaging();
		
	}
	
	protected void initPaging() {

		this.mainPaging.addEventListener("onPaging", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				
				PagingEvent pe = (PagingEvent) event;
                int pgno = pe.getActivePage();
                
                fillListData( pgno, 10 );
                
			}
		});
	}
	
	protected PageRequest buildPageRequest(int start, int max, Order... order) {
		
		if( order == null )
			return new PageRequest( start, max );
		else
			return new PageRequest( start, max, new Sort(order) );
		
	}
	
	void delete(T t) {
		
		getService().delete( t );

		fillListData( 0, 10 );
		
	}
	
	void edit(T t, int index) {
		
		
		Map<String, Object> mapParam = new HashMap<String, Object>();
		mapParam.put("param", t );
		
		Window window = (Window) Executions.createComponents( getZulFormName(), null, mapParam);
        window.doModal();
        
        window.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {
			public void onEvent(Event evt) throws Exception {
				fillListData( mainPaging.getActivePage(), 10);
			}
		});
		
	}
	
	@Listen("onClick=#btnAdd")
	public void add() {
		
		Window window = (Window) Executions.createComponents( getZulFormName(), null, null);
        window.doModal();
        
        window.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {
			public void onEvent(Event evt) throws Exception {
				fillListData( 0, 10 );
				mainPaging.setActivePage(0);
			}
			
		});
		
	}
	
	@Listen("onClick=#btnSearch")
	public void search() {
		fillListData( 0, 10 );
	}

}
