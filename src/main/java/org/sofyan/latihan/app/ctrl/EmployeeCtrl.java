package org.sofyan.latihan.app.ctrl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.sofyan.latihan.app.model.Employee;
import org.sofyan.latihan.app.service.EmployeeService;
import org.sofyan.latihan.app.spring.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.util.CollectionUtils;
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
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

public class EmployeeCtrl extends SelectorComposer<Window> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Autowired
	private EmployeeService employeeServiceImpl;
	
	@Wire("#listEmp")
	private Listbox listEmp;
	private ListModelList<Employee> listModelEmp;
	
	@Wire
	private Listheader listHeaderName, 
					   listHeaderSalary;
	
	@Wire
	private Paging pagingListEmp;
	
	private class ListRender implements ListitemRenderer<Employee> {

		public void render(Listitem listItem, Employee em, int index)
				throws Exception {

			listItem.setValue(em);
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
			String empDate = sdf.format( em.getCreatedDate() );
			
			new Listcell( em.getName() ).setParent( listItem );
			new Listcell( em.getSalary().toString() ).setParent( listItem );
			new Listcell( empDate ).setParent( listItem );
			
		}
		
	}
	
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		
		super.doAfterCompose(comp);
		
		SpringUtil.getApplicationContext().getAutowireCapableBeanFactory().autowireBean( this );
		
		initListModel( 0, 10 );

		//Paging
		this.pagingListEmp.addEventListener("onPaging", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				
				PagingEvent pe = (PagingEvent) event;
                int pgno = pe.getActivePage();
                
                initListModel( pgno, 10 );
                
			}
		});
		
	}
	
	private void initListHeaderSorting() {
		
		this.listHeaderName.setSort("auto(name)");
		this.listHeaderName.addEventListener("onSort", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				event.stopPropagation();
				
				listHeaderSalary.setSortDirection("natural");

				String dire = ((Listheader) event.getTarget()).getSortDirection();
				
				if("natural".equalsIgnoreCase( dire ) || "descending".equalsIgnoreCase( dire) ) {
					((Listheader) event.getTarget()).setSortDirection("ascending");
					initListModel( 0, 10);
				}
				else {
					((Listheader) event.getTarget()).setSortDirection("descending");
					initListModel( 0, 10);
				}
				
			}
		});
		
		this.listHeaderSalary.setSort("auto(salary)");
		this.listHeaderSalary.addEventListener("onSort", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				event.stopPropagation();
				
				listHeaderName.setSortDirection("natural");

				String dire = ((Listheader) event.getTarget()).getSortDirection();
				
				if("natural".equalsIgnoreCase( dire ) || "descending".equalsIgnoreCase( dire ) ) {
					((Listheader) event.getTarget()).setSortDirection("ascending");
					initListModel( 0, 10);
				}
				else {
					((Listheader) event.getTarget()).setSortDirection("descending");
					initListModel( 0, 10);
				}
				
			}
		});
		
	}
	
	private void initListModel(int off, int max) {
		
		PageRequest pr = null;
		
		String dire = listHeaderName.getSortDirection();
		if("ascending".equalsIgnoreCase( dire) ) {
			pr = buildPageRequest( off , 10, new Order(Direction.ASC, "name"));
		} else if("descending".equalsIgnoreCase( dire)) {
			pr = buildPageRequest( off, 10, new Order(Direction.DESC, "name"));
		}
		
		Page<Employee> pageEmp = this.employeeServiceImpl.findAll( pr );
		
		if( !CollectionUtils.isEmpty( pageEmp.getContent()) ) {			
			this.listModelEmp = new ListModelList<Employee>( pageEmp.getContent() );
		} else {
			this.listModelEmp = new ListModelList<Employee>();
		}		
		
		this.listModelEmp.setMultiple( true );
		
		this.listEmp.setItemRenderer( new ListRender());
		this.listEmp.setModel( this.listModelEmp );
		
		this.pagingListEmp.setPageSize( 10 );
		this.pagingListEmp.setTotalSize( new BigDecimal( pageEmp.getTotalElements() ).intValueExact() );
		
	}
	
	@Listen("onClick=#btnEdit")
	public void edit() {
		
		final int index = this.listEmp.getSelectedIndex();
		if( index == -1 )
			return;
		
		Employee fm = this.listModelEmp.get(  this.listEmp.getSelectedIndex() );
		
		Map<String, Object> mapParam = new HashMap<String, Object>();
		mapParam.put("param", fm );
		
		Window window = (Window) Executions.createComponents("/employeeform.zul", null, mapParam);
        window.doModal();
        
        window.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {
			public void onEvent(Event evt) throws Exception {
				Employee em = (Employee) evt.getData();				
				listModelEmp.set(index,  em );
			}
		});
		
	}
	
	@Listen("onClick=#btnAdd")
	public void add() {
		
		Window window = (Window) Executions.createComponents("/employeeform.zul", null, null);
        window.doModal();
        
        window.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {
			public void onEvent(Event evt) throws Exception {
				initListModel( 0, 10 );
			}
			
		});
		
	}
	
	@Listen("onClick=#btnDelete")
	public void delete() {
		
		if( !SecurityUtil.isAllGranted( "ROLE_USER" ) )
			throw new RuntimeException("Not authorized");
		
		final int index = this.listEmp.getSelectedIndex();
		if( index == -1 )
			return;
		
		Employee em = this.listModelEmp.get(  this.listEmp.getSelectedIndex() );
		
		this.employeeServiceImpl.delete( em );
		
		initListModel( 0, 10 );
		
	}
	
	private PageRequest buildPageRequest(int start, int max, Order order) {
		
		if( order == null )
			return new PageRequest( start, max );
		else
			return new PageRequest( start, max, new Sort(order) );
		
	}

}
