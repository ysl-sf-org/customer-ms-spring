package application.model;

public class About {
	
	private String name;
	private String parentRepo;
	private String description;
	
	public About() {
		
	}
	
	public About(String name, String parentRepo, String description) {
		super();
		this.name = name;
		this.parentRepo = parentRepo;
		this.description = description;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getParentRepo() {
		return parentRepo;
	}
	
	public void setParentRepo(String parentRepo) {
		this.parentRepo = parentRepo;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	

}
