package org.sofyan.latihan.app.ctrl;

import java.util.Date;

import org.sofyan.latihan.app.model.Employee;
import org.sofyan.latihan.app.service.EmployeeService;
import org.sofyan.latihan.app.spring.util.UIUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

@SuppressWarnings("deprecation")
public class EmployeeFormCtrl extends SelectorComposer<Window> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Autowired
	private EmployeeService employeeServiceImpl;

	private Employee employee = new Employee();
	
	private AnnotateDataBinder binder;
	
	@Wire
	private Intbox empSal;
	
	@Wire
	private Textbox empName;
	
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		
		SpringUtil.getApplicationContext().getAutowireCapableBeanFactory().autowireBean( this );
		
		if (UIUtil.getArg("param") != null) {
			this.employee =  (Employee) UIUtil.getArg("param");
		} else {
			this.employee.setId( 0L );
		}
		
		binder = new AnnotateDataBinder(this.getSelf());
		binder.bindBean("employee", this.employee);
		binder.loadAll();
	}
	
	@Listen("onClick=#btnCancel")
	public void cancelAction() {
		this.getSelf().detach();
	}

	@Listen("onClick=#btnSave")
	public void saveAction() {
		
		Integer temp = this.empSal.getValue();
		
		if( temp == null ) 
			this.empSal.setErrorMessage("Employee Sallary cannot empty");
		
		
		if( ((Textbox) this.getSelf().getFellow("empName")).getValue().trim().equals("") ) 
			this.empName.setErrorMessage("Employee Name cannot empty");
		
		//It has error
		if( !StringUtils.isEmpty( this.empName.getErrorMessage()) || 
				!StringUtils.isEmpty( this.empSal.getErrorMessage()) ) {
			return;
		}
		
		if( this.employee.getId().equals( 0L )) {
			this.employee.setCreatedDate( new Date() );
		}
		
		this.employeeServiceImpl.save( this.employee );
		
		Events.postEvent(new Event("onClose", this.getSelf(), this.employee ));
	}
	
}
