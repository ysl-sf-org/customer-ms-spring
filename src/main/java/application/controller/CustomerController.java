package application.controller;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.query.QueryResult;
import com.cloudant.client.org.lightcouch.NoDocumentException;
import com.google.common.base.Strings;

import application.config.CloudantPropertiesBean;
import application.model.Customer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.cloudant.client.api.model.Response;


@RestController
@RequestMapping("/customer")
@Api(value = "Customer Management System")
public class CustomerController {
	
	final private static Logger logger = LoggerFactory.getLogger(CustomerController.class);
	
	private Database cloudant;
	
	@Autowired
    private CloudantPropertiesBean cloudantProperties;
	
	@PostConstruct
    private void init() throws MalformedURLException {
        logger.debug(cloudantProperties.toString());
        
        try {
            logger.info("Connecting to cloudant at: " + cloudantProperties.getProtocol() + "://" + cloudantProperties.getHost() + ":" + cloudantProperties.getPort());
            final CloudantClient cloudantClient = ClientBuilder.url(new URL(cloudantProperties.getProtocol() + "://" + cloudantProperties.getHost() + ":" + cloudantProperties.getPort()))
                    .username(cloudantProperties.getUsername())
                    .password(cloudantProperties.getPassword())
                    .build();
            
            cloudant = cloudantClient.database(cloudantProperties.getDatabase(), true);
            
            
            // create the design document if it doesn't exist
            if (!cloudant.contains("_design/username_searchIndex")) {
                final Map<String, Object> names = new HashMap<String, Object>();
                names.put("index", "function(doc){index(\"usernames\", doc.username); }");

                final Map<String, Object> indexes = new HashMap<String, Object>();
                indexes.put("usernames", names);

                final Map<String, Object> view_ddoc = new HashMap<String, Object>();
                view_ddoc.put("_id", "_design/username_searchIndex");
                view_ddoc.put("indexes", indexes);

                cloudant.save(view_ddoc);        
            }
            
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
        

    }
	
	private Database getCloudantDatabase()  {
        return cloudant;
    }
	
	/**
     * check
     */
	@RequestMapping("/check")
    protected @ResponseBody ResponseEntity<String> check() {
    	// test the cloudant connection
    	try {
			getCloudantDatabase().info();
            return  ResponseEntity.ok("It works!");
    	} catch (Exception e) {
    		logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    	}
    }
	
	/**
     * @return customer by username
     */
	@CrossOrigin
    @ApiOperation(value = "Search a customer by id", response = Customer.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    protected @ResponseBody
    ResponseEntity<?> searchCustomerById(@PathVariable("id") String id) {
        try {
            if (id == null) {
                return ResponseEntity.badRequest().body("Missing username");
            }
            String query = "{ \"selector\": { \"_id\": \"" + id + "\" } }";
            System.out.println("id " + id + "  temp ");
            final QueryResult<Customer> customers = getCloudantDatabase().query(query, Customer.class);
            System.out.println("customers.toString" + customers.getDocs().toString());
            String customer = customers.getDocs().toString();
            return ResponseEntity.ok(customer);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
	
	/**
     * @return customer by username
     */
	@CrossOrigin
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    protected @ResponseBody ResponseEntity<?> searchCustomers(@RequestHeader Map<String, String> headers, @RequestParam(required=true) String username) {
        try {
        	
        	if (username == null) {
        		return ResponseEntity.badRequest().body("Missing username");
        	}
        	
        	String query = "{ \"selector\": { \"username\": \"" + username + "\" } }";
            System.out.println("username " + username);
            final QueryResult<Customer> customers = getCloudantDatabase().query(query, Customer.class);
            System.out.println("customers.toString" + customers.getDocs().toString());
            List<Customer> customer_list = customers.getDocs();
        	
        	//  query index
            return  ResponseEntity.ok(customer_list);
            
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
    }
    
    /**
     * Add customer
     *
     * @return transaction status
     */
    @ApiOperation(value = "Add a customer")
    @RequestMapping(value = "/add", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> addCustomer(@RequestHeader Map<String, String> headers, @RequestBody Customer payload) {
        System.out.println("this is being run");
        try {
            // TODO: no one should have access to do this, it's not exposed to APIC
            final Database cloudant = getCloudantDatabase();

            if (payload.get_id() != null && cloudant.contains(payload.get_id())) {
                return ResponseEntity.badRequest().body("Id " + payload.get_id() + " already exists");
            }
         
            String query = "{ \"selector\": { \"username\": \"" + payload.getUsername() + "\" } }";
            System.out.println("username " + payload.getUsername());
            final QueryResult<Customer> customers = getCloudantDatabase().query(query, Customer.class);
            System.out.println("customers.toString" + customers.getDocs().toString());
            String customer = customers.getDocs().toString();
            
            if (Strings.isNullOrEmpty(customer)) {
                return ResponseEntity.badRequest().body("Customer with name " + payload.getUsername() + " already exists");
            }

            final Response resp = cloudant.save(payload);
            if (resp.getError() == null) {
                // HTTP 201 CREATED
                final URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(resp.getId()).toUri();
                return ResponseEntity.created(location).build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp.getError());
            }

        } catch (Exception ex) {
            logger.error("Error creating customer: " + ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating customer: " + ex.toString());
        }

    }
    
    /**
     * Update customer
     *
     * @return transaction status
     */
    @ApiOperation(value = "Update customer by id")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST, consumes = "application/json")
    protected ResponseEntity<?>
    updateCustomerById(@RequestHeader Map<String, String> isAuthenticated, @PathVariable String id, @RequestBody Customer payload) {
        try {
            //final String customerId = customerRepository.getCustomerId();
            if (isAuthenticated == null) {
                // if no user passed in, this is a bad request
                return ResponseEntity.badRequest().body("Invalid Bearer Token: Missing customer ID");
            }
            if (isAuthenticated.get("securitycontext").equals("false")) {
                return ResponseEntity.badRequest().body("User does not have enough access to make such query");
            }

            logger.info("caller: " + payload.get_id());
            if (!payload.get_id().equals(id)) {
                // if i'm getting a customer ID that doesn't match my own ID, then return 401
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            final Database cloudant = getCloudantDatabase();

            // Find the customer with the old values
            Customer customer = getCloudantDatabase().find(Customer.class, id);

            // _rev is set to null from the test case, get the _rev and set it to the payload
            payload.set_rev(customer.get_rev());

            // set the payload to be the customer
            customer = payload;

            // update the database
            cloudant.update(customer);

        } catch (NoDocumentException e) {
            logger.error("Customer not found: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer with ID " + id + " not found");
        } catch (Exception ex) {
            logger.error("Error updating customer: " + ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating customer: " + ex.toString());
        }

        return ResponseEntity.ok().build();
    }
    
    /**
     * Delete customer
     *
     * @return transaction status
     */
    @ApiOperation(value = "Delete a customer by id")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    protected ResponseEntity<?> deleteCustomerById(@PathVariable String id) {
        // TODO: no one should have access to do this, it's not exposed to APIC
        try {
            final Database cloudant = getCloudantDatabase();
            final Customer cust = getCloudantDatabase().find(Customer.class, id);
            cloudant.remove(cust);
        } catch (NoDocumentException e) {
            logger.error("Customer not found: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer with ID " + id + " not found");
        } catch (Exception ex) {
            logger.error("Error deleting customer: " + ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting customer: " + ex.toString());
        }
        return ResponseEntity.ok().build();
    }
    
    /**
     * @return all customers
     * @throws Exception
     */

    @ApiOperation(value = "View a customer", response = Iterable.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    }
    )
    @RequestMapping(method = RequestMethod.GET)
    protected ResponseEntity<?> getCustomer() {
    	try {
    		final String customerId = getCustomerId();
    		if (customerId == null) {
        		// if no user passed in, this is a bad request
        		return ResponseEntity.badRequest().body("Invalid Bearer Token: Missing customer ID");
        	}
    		logger.debug("caller: " + customerId);
    		String query = "{ \"selector\": { \"username\": \"" + customerId + "\" } }";
            System.out.println("username " + customerId);
            final QueryResult<Customer> customers = getCloudantDatabase().query(query, Customer.class);
            System.out.println("customers.toString" + customers.getDocs().toString());
    		return ResponseEntity.ok(Arrays.asList(customers.getDocs()));
    	}catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }
    
    private String getCustomerId() {
    	final SecurityContext ctx = SecurityContextHolder.getContext();
    	if (ctx.getAuthentication() == null) {
    		return null;
    	};
    	
    	if (!ctx.getAuthentication().isAuthenticated()) {
    		return null;
    	}
    	
    	final OAuth2Authentication oauth = (OAuth2Authentication)ctx.getAuthentication();
    	
    	logger.debug("CustomerID: " + oauth.getName());
    	
    	return oauth.getName();
    }


}
