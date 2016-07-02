package org.sofyan.latihan.app.repository.specification;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.sofyan.latihan.app.bean.CustomerSearchBean;
import org.sofyan.latihan.app.bean.SearchBean;
import org.sofyan.latihan.app.model.Owner;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class CustomerSpecification {
	
	public static Specification<Owner> findByCriteria(SearchBean sb) {
		
		return new Specification<Owner>() {

			@Override
			public Predicate toPredicate(Root<Owner> root,
					CriteriaQuery<?> query, CriteriaBuilder cb) {
				
				List<Predicate> predicates = new ArrayList<Predicate>();
				
				CustomerSearchBean cs = (CustomerSearchBean) sb;

                if ( !StringUtils.isEmpty( cs.getName() )) {
                    predicates.add( cb.like( cb.lower( root.get("name") ), cs.getName() + "%"));
                }

                return cb.and(predicates.toArray(new Predicate[] {}));
				
			}
		};
		
	}

}
