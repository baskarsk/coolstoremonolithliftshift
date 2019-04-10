package com.redhat.coolstore.service;

import java.util.Hashtable;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import com.redhat.coolstore.model.Order;
import com.redhat.coolstore.utils.Transformers;


/*
 * 
 *  Below mentioned code is added to remove weblogic server related reference 
 *  from resource/webapp/WEB-INF/weblogic-ejb-jar.xml ,where timeout for message handling is defined
 *  While app migration to  JBOSS EAP , one can make use of Apache ActiveMQ Artemis.
 * 
 * 	@MessageDriven is added to migrate the app from weblogic to JBOSS EAP 7.0
 * 
 */
@MessageDriven(name = "InventoryNotificationMDB", activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "topic/orders"),
		  @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
	        @ActivationConfigProperty(propertyName = "transactionTimeout", propertyValue = "30"),
	        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")
	})
public class InventoryNotificationMDB implements MessageListener {

    private static final int LOW_THRESHOLD = 50;

    @Inject
    private CatalogService catalogService;

    @Inject
    private Logger log;

//    private final static String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";
//    private final static String JMS_FACTORY = "TCF";
//    private final static String TOPIC = "topic/orders";
//    private TopicConnection tcon;
//    private TopicSession tsession;
//    private TopicSubscriber tsubscriber;

    public void onMessage(Message rcvMessage) {
        TextMessage msg;
        {
            try {
                if (rcvMessage instanceof TextMessage) {
                    msg = (TextMessage) rcvMessage;
                    String orderStr = msg.getBody(String.class);
                    Order order = Transformers.jsonToOrder(orderStr);
                    order.getItemList().forEach(orderItem -> {
                        int old_quantity = catalogService.getCatalogItemById(orderItem.getProductId()).getInventory().getQuantity();
                        int new_quantity = old_quantity - orderItem.getQuantity();
                        if (new_quantity < LOW_THRESHOLD) {
                            log.warning("Inventory for item " + orderItem.getProductId() + " is below threshold (" + LOW_THRESHOLD + "), contact supplier!");
                        } else {
                            orderItem.setQuantity(new_quantity);
                        }
                    });
                }


            } catch (JMSException jmse) {
                System.err.println("An exception occurred: " + jmse.getMessage());
            }
        }
    }

    /*
     *  Below mentioned code is commented out for  migrating apps to JBOSS EAP with Apache ActiveMQ
     *  and below code is handled as annotation
     * 
     */
//    public void init() throws NamingException, JMSException {
//        Context ctx = getInitialContext();
//        TopicConnectionFactory tconFactory = (TopicConnectionFactory) PortableRemoteObject.narrow(ctx.lookup(JMS_FACTORY), TopicConnectionFactory.class);
//        tcon = tconFactory.createTopicConnection();
//        tsession = tcon.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
//        Topic topic = (Topic) PortableRemoteObject.narrow(ctx.lookup(TOPIC), Topic.class);
//        tsubscriber = tsession.createSubscriber(topic);
//        tsubscriber.setMessageListener(this);
//        tcon.start();
//    }
//
//    public void close() throws JMSException {
//        tsubscriber.close();
//        tsession.close();
//        tcon.close();
//    }
//
//    private static InitialContext getInitialContext() throws NamingException {
//        Hashtable<String, String> env = new Hashtable<>();
//        env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
//        env.put(Context.PROVIDER_URL, "t3://localhost:7001");
//        env.put("weblogic.jndi.createIntermediateContexts", "true");
//        return new InitialContext(env);
//    }
}