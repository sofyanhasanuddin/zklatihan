package org.sofyan.latihan.app.spring.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

public class SecurityUtil {

	public static boolean isAllGranted(String authorities) {
       
		if ( StringUtils.isEmpty(authorities) ) {
            return false;
        }
        
        final Collection<? extends GrantedAuthority> granted = getPrincipalAuthorities();
        
        return granted.stream().anyMatch( s -> s.toString().equals( authorities ) );
        
    }
 
    private static Collection<? extends GrantedAuthority> getPrincipalAuthorities() {
        
    	Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
    
        if (null == currentUser) {
            return Collections.emptyList();
        }
      
        if ((null == currentUser.getAuthorities()) || (currentUser.getAuthorities().isEmpty())) {
            return Collections.emptyList();
        }
        
        List<String> list = new ArrayList<String>();
        list.contains("");
        
        Collection<? extends GrantedAuthority> granted = currentUser.getAuthorities();
        
        return granted;
    }
	
}
