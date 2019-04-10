package com.redhat.coolstore.utils;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;


/**** 
 * This code is basically to extend Weblogic server application lifecycle listener
 * and listen to WEBLOGIC server start and stop events. 
 *  
 *  when this application is migrated to JBOSS EAP ,below mentioned class has to replaced
 *  with JBOSS EAP server events and J2EE servlet context container events.
 */
//public class StartupListener extends ApplicationLifecycleListener {
//
//    @Inject
//    Logger log;
//
//    @Override
//    public void postStart(ApplicationLifecycleEvent evt) {
//        log.info("AppListener(postStart)");
//    }
//
//    @Override
//    public void preStop(ApplicationLifecycleEvent evt) {
//        log.info("AppListener(preStop)");
//    }
//
//}

	/*
	 *  when this application is migrated to JBOSS EAP ,below mentioned class has to replaced
	 * 	with JBOSS EAP server events and J2EE servlet context container events.
	 * 
	 */
	@Singleton
	@Startup
	public class StartupListener {
	
		@Inject
		Logger log;
		
		@PostConstruct
		public void postStart() {
			
			log.info("AppListener(postStart)");
		}
		
		@PreDestroy
		public void preDestroy() {
			log.info("AppListener(preDestroy)");
		}
		
	}
