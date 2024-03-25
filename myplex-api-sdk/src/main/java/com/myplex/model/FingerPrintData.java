package com.myplex.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class FingerPrintData implements Serializable {

	@SerializedName("active")
	@Expose
	private String active;
	@SerializedName("textColor")
	@Expose
	private String textColor;
	@SerializedName("textSize")
	@Expose
	private String textSize;
	@SerializedName("textBackground")
	@Expose
	private String textBackground;
	@SerializedName("type")
	@Expose
	private String type;
	@SerializedName("text")
	@Expose
	private String text;
	@SerializedName("player")
	@Expose
	private String player;
	@SerializedName("imageUrl")
	@Expose
	private String imageUrl;
	@SerializedName("frequencyInSeconds")
	@Expose
	private Integer frequencyInSeconds;
	@SerializedName("displayDurationInSeconds")
	@Expose
	private Integer displayDurationInSeconds;
	@SerializedName("position")
	@Expose
	private String position;

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getPlayer() {
		return player;
	}

	public void setPlayer(String player) {
		this.player = player;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public Integer getFrequencyInSeconds() {
		return frequencyInSeconds;
	}

	public void setFrequencyInSeconds(Integer frequencyInSeconds) {
		this.frequencyInSeconds = frequencyInSeconds;
	}

	public Integer getDisplayDurationInSeconds() {
		return displayDurationInSeconds;
	}

	public void setDisplayDurationInSeconds(Integer displayDurationInSeconds) {
		this.displayDurationInSeconds = displayDurationInSeconds;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public void setTextBackground(String textBackground) {
		this.textBackground = textBackground;
	}

	public String getTextBackground() {
		return textBackground;
	}

	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}

	public String getTextColor() {
		return textColor;
	}

	public void setTextSize(String textSize) {
		this.textSize = textSize;
	}

	public String getTextSize() {
		return textSize;
	}
}