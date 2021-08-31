package application.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "All details about the Customer ")
public class Customer {
	
	@ApiModelProperty(notes = "The database generated customer ID")
    private String _id;

    @ApiModelProperty(notes = "Revision represents an opaque hash value over the contents of a document.")
    @SuppressWarnings("unused")
    private String _rev;

    @ApiModelProperty(notes = "The customer username")
    private String username;

    @ApiModelProperty(notes = "The customer password")
    private String password;

    @ApiModelProperty(notes = "The customer first name")
    private String firstName;

    @ApiModelProperty(notes = "The customer last name")
    private String lastName;

    @ApiModelProperty(notes = "The customer email id")
    private String email;

    @ApiModelProperty(notes = "The customer image url")
    private String imageUrl;

    public Customer(String _id, String _rev, String username, String password, String firstName, String lastName, String email, String imageUrl) {
        this._id = _id;
        this._rev = _rev;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.imageUrl = imageUrl;
    }

    public Customer() {

    }

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String get_rev() {
		return _rev;
	}

	public void set_rev(String _rev) {
		this._rev = _rev;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

}
