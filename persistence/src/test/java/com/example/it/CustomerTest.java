package com.example.it;

import com.example.customer.*;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(ArquillianExtension.class)
public class CustomerTest {

    private final static Logger LOGGER = Logger.getLogger(CustomerTest.class.getName());

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "CustomerTest.war")
                .addPackage(Customer.class.getPackage())
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, war.toString(true));
        return war;
    }

    @Inject
    private EntityManager em;

    @Inject
    UserTransaction ux;

    private void startTx() throws Exception {
        ux.begin();
        em.joinTransaction();
    }

    @AfterEach
    public void after() throws Exception {
        endTx();
    }

    private void endTx() throws Exception {
        LOGGER.log(Level.INFO, "Transaction status: {0}", ux.getStatus());
        try {
            if (ux.getStatus() == Status.STATUS_ACTIVE) {
                ux.commit();
            }
        } catch (Exception e) {
            ux.rollback();
        }
    }

    @Test
    public void testCustomerAndOrderCrud() throws Exception {
        // --- Test Product Persistence ---
        var product1 = new Product();
        product1.setName("Laptop");
        product1.setPrice(new BigDecimal("1200.00"));

        var product2 = new Product();
        product2.setName("Mouse");
        product2.setPrice(new BigDecimal("25.00"));

        startTx();
        em.persist(product1);
        em.persist(product2);
        em.flush();
        endTx();

        assertNotNull(product1.getId());
        assertNotNull(product2.getId());

        // --- Test Customer Persistence ---
        var customer = new Customer();
        customer.setName("John Doe");
        customer.setEmail("john.doe@example.com");
        // Initialize OrderHistory

        startTx();
        em.persist(customer);
        em.flush();
        endTx();

        assertNotNull(customer.getId());

        // Retrieve customer and verify
        startTx();
        var savedCustomer = em.find(Customer.class, customer.getId());
        assertNotNull(savedCustomer);
        assertEquals("John Doe", savedCustomer.getName());
        assertEquals("john.doe@example.com", savedCustomer.getEmail());

        // --- Test Order Persistence ---
        var order = new Order(savedCustomer); // Associate order with savedCustomer
        order.addItem(product1, 1);
        order.addItem(product2, 2);
        order.calculateAmount(); // Ensure amount is calculated

        em.persist(order);
        savedCustomer.getOrderHistory().orders().add(order);
        em.flush();
        endTx();

        assertNotNull(order.getId());

        // Retrieve order and verify
        startTx();
        var savedOrder = em.find(Order.class, order.getId());
        assertNotNull(savedOrder);
        assertEquals(new BigDecimal("1250.00"), savedOrder.getAmount()); // 1200 + 2*25 = 1250
        assertEquals(2, savedOrder.getOrderItems().size());

        var verifiedCustomer = em.find(Customer.class, customer.getId());
        assertNotNull(verifiedCustomer);
        assertNotNull(verifiedCustomer.getOrderHistory());
        assertNotNull(verifiedCustomer.getOrderHistory().orders());
        assertFalse(verifiedCustomer.getOrderHistory().orders().isEmpty());
        assertEquals(1, verifiedCustomer.getOrderHistory().orders().size());

        var customerOrder = verifiedCustomer.getOrderHistory().orders().getFirst();
        assertNotNull(customerOrder);
        assertEquals(savedOrder.getId(), customerOrder.getId());
        assertEquals(savedOrder.getAmount(), customerOrder.getAmount());
        assertEquals(2, customerOrder.getOrderItems().size());

        // Verify order items
        var orderItem1 = customerOrder.getOrderItems().get(0);
        assertEquals(product1.getId(), orderItem1.product().getId());
        assertEquals(1, orderItem1.quantity());

        var orderItem2 = customerOrder.getOrderItems().get(1);
        assertEquals(product2.getId(), orderItem2.product().getId());
        assertEquals(2, orderItem2.quantity());
        endTx();
    }
}
