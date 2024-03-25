package com.myplex.model;

import java.io.Serializable;
import java.util.List;

public class UserProfile implements Serializable {
	private static final long serialVersionUID = 1L;
	public boolean purchasesAvailable;
	public boolean showForm;
	public String last;
	public String dob;
	public String address;
	public String pincode;
	public String location;
	public String city;
	public String state;
	public String gender;
	public String profile_image;
	public List<String> locations;
	public List<Object> languages;
	public String mobile_no;
	public String smc_no;
	public int _id;
	public String serviceName;
	public String name;
	public String age;
	public String email;
	public List<EmailData> emails;
	public String first;
	public List<String> genres;
	public List<String> packageLanguages;
	public List<CardDataPackages> packages;
	public List<String> pendingSMCNumbers;

}
